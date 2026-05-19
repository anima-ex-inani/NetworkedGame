package io.github.animaexinani.game;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.assets.AssetKey;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.input.GameAction;
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
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.ServerNetworkEntity;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameServer;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.game.network.GameConnection;
import io.github.animaexinani.game.network.GameConnectionFactory;

/**
 * The main application class for the Networked Game.
 */
public final class NetworkedGame extends Application {

    /**
     * Determines whether this instance runs as a dedicated server, a pure client,
     * or both on the same machine (LOCAL).
     */
    public enum Mode {
        SERVER,
        CLIENT,
        LOCAL
    }

    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private static final ApplicationOptions OPTIONS = new ApplicationOptions(
            "Networked Game", "0.1.0-alpha.5", "io.github.animaexinani.networkedgame");

    private static final double TIME_STEP = 1.0 / 60.0;

    private final Mode mode;
    private final Window mainWindow;

    // State management for UI modes
    private final GameStateManager stateManager;
    private final SettingsManager settingsManager;

    private final GameInputListener inputListener;
    private final RebindingController rebindingController;

    // Only for headless server mode
    private final CombinedWorld serverWorld;
    private final GameServer gameServer;

    private long lastTime = 0;
    private double accumulator = 0.0;

    static void main(String[] args) {
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

    public static void main() {
        main(new String[0]);
    }

    public NetworkedGame(Mode mode, String host, int port) {
        super(OPTIONS);
        this.mode = mode;

        if (mode == Mode.SERVER) {
            this.mainWindow = null;
            this.stateManager = null;
            this.settingsManager = null;
            this.inputListener = null;
            this.rebindingController = null;

            SizeF sizeF = new SizeF(1920f, 1080f);
            List<Entity> serverEntities = new ArrayList<>();
            Random rand = new Random();
            for (int i = 0; i < 5; i++) {
                float x = rand.nextFloat() * sizeF.width();
                float y = rand.nextFloat() * sizeF.height();
                double vx = rand.nextDouble() * 100 - 50;
                double vy = rand.nextDouble() * 100 - 50;
                serverEntities.add(new Asteroid(EntityType.ASTEROID, x, y, vx, vy));
            }

            UUID serverId = new UUID(0L, 0L);
            ServerNetworkEntity serverDummy = new ServerNetworkEntity(serverId, EntityType.PLAYER);
            serverEntities.add(serverDummy);

            this.serverWorld = new CombinedWorld.Builder()
                    .withEntities(serverEntities)
                    .withLocalPlayerId(serverId)
                    .withSize(sizeF)
                    .build();

            this.gameServer = new GameServer(this.serverWorld, port);
            this.gameServer.start();
        } else {
            this.serverWorld = null;
            this.gameServer = null;

            var windowOptions = new WindowOptions("Networked Game [" + mode + "]", 1920, 1080);
            windowOptions.setResizable(true);
            this.mainWindow = super.windowFactory().createWindow(windowOptions);

            this.assetManager().registerLoader(new ResourceLoader());
            this.settingsManager = new SettingsManager();

            var settings = this.settingsManager.getSettings();
            InputBindings bindings = InputBindings.defaultBindings();
            settings.getKeybinds().forEach((actionName, scancode) -> {
                try {
                    bindings.bind(scancode, GameAction.valueOf(actionName));
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Unknown game action in settings: " + actionName);
                }
            });
            bindings.getAll().forEach((scancode, action) -> 
                settings.getKeybinds().put(action.name(), scancode));
            this.settingsManager.save();

            this.inputListener = new GameInputListener(bindings);
            this.rebindingController = new RebindingController(bindings);

            this.eventRegistry().register(KeyboardListener.class, this.inputListener);
            this.eventRegistry().register(KeyboardListener.class, this.rebindingController);

            this.stateManager = new GameStateManager();

            FontFace fontFace = null;
            try {
                fontFace = this.assetManager().load(new AssetKey<>(FontFace.class, "/Oxanium-Medium.ttf")).get();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load UI font", e);
            }

            if (mode == Mode.CLIENT) {
                float width = this.mainWindow.clientSize().width();
                float height = this.mainWindow.clientSize().height();
                GameConnection connection = GameConnectionFactory.createClientConnection(new SizeF(width, height));
                connection.gameClient().connect(host, port);
                this.stateManager.transitionTo(new PlayState(this.mainWindow, fontFace, this.stateManager, this.eventRegistry(), this.settingsManager, this.rebindingController, connection));
            } else {
                this.stateManager.transitionTo(new MainMenuState(this.mainWindow, this.stateManager, fontFace, this.eventRegistry(), this.settingsManager, this.rebindingController));
            }
        }

        this.lastTime = System.nanoTime();
    }

    @Override
    protected boolean iterate() {
        long currentTime = System.nanoTime();
        float frameTime = (currentTime - this.lastTime) / 1_000_000_000.0f;
        this.lastTime = currentTime;

        if (frameTime > 0.25f) frameTime = 0.25f;
        this.accumulator += frameTime;

        if (this.mode == Mode.SERVER) {
            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
            return true;
        }

        while (this.accumulator >= TIME_STEP) {
            Duration dt = Duration.ofNanos((long) (1_000_000_000L * TIME_STEP));
            this.stateManager.handleInput(this.inputListener, dt);
            this.stateManager.update(dt);
            this.accumulator -= TIME_STEP;
        }

        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);
        this.stateManager.render(renderer);
        renderer.present();

        return true;
    }

    @Override
    public void close() {
        if (this.stateManager != null) {
            this.stateManager.transitionTo(null);
        }
        if (this.gameServer != null) {
            this.gameServer.stop();
        }
        if (this.mainWindow != null) {
            try {
                this.mainWindow.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e, () -> "Unhandled exception when closing window");
            }
        }
        super.close();
    }
}
