package io.github.animaexinani.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameClient;
import io.github.animaexinani.game.server.GameServer;

public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;

    // master world manager
    private final CombinedWorld combinedWorld;
    private final GameClient gameClient;
    private final GameServer gameServer; // Authoritative network server instance
    
    // keep a direct reference to the player's ship specifically so we can route
    // keyboard inputs to it
    private final PlayerShip playerShip;

    // instantiate Input System
    private final GameInputListener inputListener;
    private final RebindingController rebindingController;

    private static final ApplicationOptions OPTIONS = new ApplicationOptions("Networked Game", "0.1.0-alpha.5",
            "io.github.animaexinani.networkedgame");

    private long lastTime = 0;
    private double accumulator = 0.0;

    // 60 for now for smoother gameplay
    private static final double TIME_STEP = 1.0 / 60.0;

    @Override
    protected boolean iterate() {
        long currentTime = System.nanoTime();
        float frameTime = (currentTime - this.lastTime) / 1_000_000_000.0f;
        this.lastTime = currentTime;

        if (frameTime > 0.25f) frameTime = 0.25f;
        this.accumulator += frameTime;

        // network Output Loop
        while (this.accumulator >= TIME_STEP) {
            this.gameClient.sendInputs();
            this.accumulator -= TIME_STEP;
        }

        // client Render Loop
        // flush the spawn/despawn queues so new network entities appear
        this.combinedWorld.preUpdate(java.time.Duration.ZERO); 
        
        // glide the dummy visuals toward the latest server snapshot
        this.combinedWorld.interpolateVisuals(frameTime);

        // draw everything
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);
        this.combinedWorld.render(renderer);
        renderer.present();
        
        return true;
    }

    @Override
    public void close() {
        // shutdown network hooks and background execution loops
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

    static void main() {
        try (var game = new NetworkedGame()) {
            game.run();
        }
    }

    public NetworkedGame() {
        super(NetworkedGame.OPTIONS);

        var windowOptions = new WindowOptions("Networked Game", 1920, 1080);
        windowOptions.setResizable(true);

        var windowFactory = super.windowFactory();
        this.mainWindow = windowFactory.createWindow(windowOptions);

        var clientSize = this.mainWindow.clientSize();
        var sizeF = new SizeF(clientSize.width(), clientSize.height());

        float centerX = clientSize.width() / 2.0f;
        float centerY = clientSize.height() / 2.0f;

        // create real world server
        this.playerShip = new PlayerShip();
        this.playerShip.physicsBody().translate(centerX, centerY);

        List<Entity> serverEntities = new ArrayList<>();
        serverEntities.add(this.playerShip);

        // add server asteroids
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
                .withLocalPlayerId(this.playerShip.id())
                .withSize(sizeF)
                .build();

        // client only gets a dummy representation of the player to satisfy the builder
        io.github.animaexinani.game.nentities.ClientNetworkEntity dummyPlayer = 
            new io.github.animaexinani.game.nentities.ClientNetworkEntity(this.playerShip.id(), EntityType.PLAYER);

        this.combinedWorld = new CombinedWorld.Builder()
                .withEntity(dummyPlayer)
                .withLocalPlayerId(this.playerShip.id())
                .withSize(sizeF)
                .withVisualFactory(EntityType.BULLET, entity -> new ConvexPolygon(
                        new PointF[]{new PointF(5.0f, 0.0f), new PointF(-2.0f, 2.5f), new PointF(-2.0f, -2.5f)}, Color.WHITE))
                .withVisualFactory(EntityType.ASTEROID, entity -> new ConvexPolygon(
                        Asteroid.getAsteroidLocalPointsForType(EntityType.ASTEROID).toArray(PointF[]::new), new Color(0.6f, 0.6f, 0.6f, 1.0f)))
                .withVisualFactory(EntityType.PLAYER, entity -> new ConvexPolygon(
                        PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), Color.GREEN))
                .withVisualFactory(EntityType.SCOUT_DRONE, entity -> new ConvexPolygon(
                        PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), new Color(1.0f, 0.0f, 0.0f, 1.0f))) // Red
                .withVisualFactory(EntityType.STRIKE_FIGHTER, entity -> new ConvexPolygon(
                        PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), new Color(1.0f, 0.0f, 1.0f, 1.0f))) // Magenta
                .build();

        // actually create the InputSystem object in memory
        var bindings = InputBindings.defaultBindings();
        this.inputListener = new GameInputListener(bindings);
        this.rebindingController = new RebindingController(bindings);

        this.assetManager().registerLoader(new ResourceLoader());

        // tell the engine to send key presses to the inputSystem, not 'this'
        this.eventRegistry().register(KeyboardListener.class, this.inputListener);
        this.eventRegistry().register(KeyboardListener.class, this.rebindingController);

        // spin up authoritative UDP server instance bound to port 9000
        this.gameServer = new GameServer(serverWorld, 9000);
        this.gameServer.start();

        // connect UDP client to the local loopback server
        this.gameClient = new GameClient(this.combinedWorld, this.inputListener, this.playerShip.id());
        this.gameClient.connect("127.0.0.1", 9000);

        // reset the clock right before the constructor finishes!
        this.lastTime = System.nanoTime();
    }
}