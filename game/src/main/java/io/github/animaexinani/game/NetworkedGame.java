package io.github.animaexinani.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.input.InputBindings;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;
import io.github.animaexinani.game.assets.ResourceLoader;
import io.github.animaexinani.game.nentities.Asteroid;
import io.github.animaexinani.game.nentities.ClientNetworkEntity;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameClient;
import io.github.animaexinani.game.server.GameServer;
import io.github.animaexinani.game.util.UUIDGenerator;

public final class NetworkedGame extends Application {

    /**
     * Determines whether this instance runs as a dedicated server, a pure client,
     * or both on the same machine (LOCAL).
     *
     * Pass --server or --client on the command line to choose; omitting either flag
     * defaults to LOCAL mode, which is the original single-machine behaviour.
     */
    public enum Mode {
        /** Authoritative simulation only — no rendering, no GameClient. */
        SERVER,
        /** Rendering + input only — no GameServer, connects to a remote host. */
        CLIENT,
        /** Both server and client on the same machine (loopback). */
        LOCAL
    }

    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());

    private final Mode mode;
    private final Window mainWindow;

    // null when mode == SERVER
    private final CombinedWorld combinedWorld;
    private final GameClient gameClient;
    private final UUID myPlayerId;

    // null when mode == CLIENT
    private final GameServer gameServer;

    private final GameInputListener inputListener;
    private final RebindingController rebindingController;

    private static final ApplicationOptions OPTIONS = new ApplicationOptions(
            "Networked Game", "0.1.0-alpha.5", "io.github.animaexinani.networkedgame");

    private long lastTime = 0;
    private double accumulator = 0.0;
    private static final double TIME_STEP = 1.0 / 60.0;

    /**
     * Usage:
     *   (no args)              — LOCAL mode, server on :9000, client → 127.0.0.1:9000
     *   --server [--port P]    — dedicated server on port P (default 9000)
     *   --client [--host H] [--port P]  — client connecting to H:P
     */
    static void main(String[] args) {
        // default
        Mode mode = Mode.LOCAL;
        String host = "127.0.0.1";
        int port = 9000;

        for (int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--server" -> mode = Mode.SERVER;
                case "--client" -> mode = Mode.CLIENT;
                case "--host"   -> { if (i + 1 < args.length) host = args[++i]; }
                case "--port"   -> { if (i + 1 < args.length) port = Integer.parseInt(args[++i]); }
                default -> LOGGER.warning("Unknown argument: " + args[i]);
            }
        }

        LOGGER.info("Starting in mode: " + mode + "  host=" + host + "  port=" + port);
        try (var game = new NetworkedGame(mode, host, port)) {
            game.run();
        }
    }

    @Override
    protected boolean iterate() {
        long currentTime = System.nanoTime();
        float frameTime = (currentTime - this.lastTime) / 1_000_000_000.0f;
        this.lastTime = currentTime;

        if (frameTime > 0.25f) frameTime = 0.25f;
        this.accumulator += frameTime;

        if (this.mode == Mode.SERVER) {
            // The server's own runLoop thread does all the work.
            // iterate() is only called because Application requires it;
            // just yield so we don't busy-spin the render thread.
            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
            return true;
        }

        // send queued inputs at the fixed network tick rate
        while (this.accumulator >= TIME_STEP) {
            this.gameClient.sendInputs();
            this.accumulator -= TIME_STEP;
        }

        // flush spawn/despawn queues so newly received network entities appear
        this.combinedWorld.preUpdate(java.time.Duration.ZERO);

        // glide dummy visuals toward the latest server snapshot
        this.combinedWorld.interpolateVisuals(frameTime);
        
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);
        this.combinedWorld.render(renderer);

        if (this.gameClient != null && this.myPlayerId != null) {
            Entity localPlayer = this.combinedWorld.getEntity(this.myPlayerId);
            
            if (localPlayer instanceof ClientNetworkEntity cp) {
                float barWidth = 400f;
                float barHeight = 20f;
                float startX = (1920f - barWidth) / 2f;
                float startY = 1000f; // Bottom center

                // draw Hull with a red background
                float hpPercent = Math.max(0, cp.health()) / 100_000f;
                renderer.draw(createRect(startX, startY + 25, barWidth * hpPercent, barHeight, new Color(1f, 0f, 0f, 1f)));

                // Draw shield with a blue background
                float shieldPercent = Math.max(0, cp.shield()) / 100_000f;
                renderer.draw(createRect(startX, startY, barWidth * shieldPercent, barHeight, new Color(0f, 0.5f, 1f, 1f)));
            }
        }

        renderer.present();

        return true;
    }

    @Override
    public void close() {
        if (this.gameServer != null) {
            this.gameServer.stop();
        }
        if (this.gameClient != null) {
            this.gameClient.stop();
        }
        try {
            this.mainWindow.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Unhandled exception when closing window");
        }
        super.close();
    }

    public NetworkedGame(Mode mode, String serverHost, int serverPort) {
        super(OPTIONS);
        this.mode = mode;

        // A window is always created; in SERVER mode it is present but nothing
        // is drawn to it beyond the initial clear.
        var windowOptions = new WindowOptions("Networked Game [" + mode + "]", 1920, 1080);
        windowOptions.setResizable(true);
        this.mainWindow = super.windowFactory().createWindow(windowOptions);

        var clientSize = this.mainWindow.clientSize();
        var sizeF = new SizeF(clientSize.width(), clientSize.height());

        // Input system
        var bindings = InputBindings.defaultBindings();
        this.inputListener = new GameInputListener(bindings);
        this.rebindingController = new RebindingController(bindings);
        this.assetManager().registerLoader(new ResourceLoader());
        this.eventRegistry().register(KeyboardListener.class, this.inputListener);
        this.eventRegistry().register(KeyboardListener.class, this.rebindingController);

        // --- Server setup (SERVER and LOCAL modes) ---------------------------
        if (mode == Mode.SERVER || mode == Mode.LOCAL) {
            List<Entity> serverEntities = new ArrayList<>();

            // Seed the world with a handful of asteroids.
            // Player ships are NOT pre-created here; they are spawned by
            // GameServer the first time a client's UUID is seen (see GameServer).
            Random rand = new Random();
            for (int i = 0; i < 5; i++) {
                float x = rand.nextFloat() * clientSize.width();
                float y = rand.nextFloat() * clientSize.height();
                double vx = rand.nextDouble() * 100 - 50;
                double vy = rand.nextDouble() * 100 - 50;
                serverEntities.add(new Asteroid(EntityType.ASTEROID, x, y, vx, vy));
            }

            CombinedWorld serverWorld = new CombinedWorld.Builder()
                    .withEntities(serverEntities)
                    .withSize(sizeF)
                    .build();

            this.gameServer = new GameServer(serverWorld, serverPort);
            this.gameServer.start();
        } else {
            this.gameServer = null;
        }

        // --- Client setup (CLIENT and LOCAL modes) ---------------------------
        if (mode == Mode.CLIENT || mode == Mode.LOCAL) {
            // Generate a stable identity for this session. The server will
            // create a PlayerShip keyed to this UUID on first contact.
            this.myPlayerId = UUIDGenerator.generateV7Uuid();

            // The client world starts with a single dummy entity for the local
            // player so the renderer has something to track before the first
            // server snapshot arrives.
            ClientNetworkEntity localDummy =
                    new ClientNetworkEntity(this.myPlayerId, EntityType.PLAYER);

            this.combinedWorld = new CombinedWorld.Builder()
                    .withEntity(localDummy)
                    .withLocalPlayerId(this.myPlayerId)
                    .withSize(sizeF)
                    .withVisualFactory(EntityType.BULLET, entity -> new ConvexPolygon(
                            new PointF[]{
                                new PointF(5.0f, 0.0f),
                                new PointF(-2.0f, 2.5f),
                                new PointF(-2.0f, -2.5f)
                            }, Color.WHITE))
                    .withVisualFactory(EntityType.ASTEROID, entity -> new ConvexPolygon(
                            Asteroid.getAsteroidLocalPointsForType(EntityType.ASTEROID)
                                    .toArray(PointF[]::new),
                            new Color(0.6f, 0.6f, 0.6f, 1.0f)))
                    .withVisualFactory(EntityType.PLAYER, entity -> {
                        Color c = new Color(0.0f, 1.0f, 0.0f, 1.0f); // Base Green
                        if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                        return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                    })
                    .withVisualFactory(EntityType.SCOUT_DRONE, entity -> {
                        Color c = new Color(1.0f, 1.0f, 0.0f, 1.0f); // Base Yellow
                        if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                        return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                    })
                    .withVisualFactory(EntityType.STRIKE_FIGHTER, entity -> {
                        Color c = new Color(1.0f, 0.0f, 1.0f, 1.0f); // Base Magenta
                        if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                        return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                    })
                    .build();

            this.gameClient = new GameClient(this.combinedWorld, this.inputListener, myPlayerId);
            this.gameClient.connect(serverHost, serverPort);
        } else {
            this.combinedWorld = null;
            this.gameClient = null;
            this.myPlayerId = null;
        }

        this.lastTime = System.nanoTime();
    }

    private ConvexPolygon createRect(float x, float y, float width, float height, Color color) {
        return new ConvexPolygon(new PointF[] {
            new PointF(x, y), new PointF(x + width, y),
            new PointF(x + width, y + height), new PointF(x, y + height)
        }, color);
    }
}