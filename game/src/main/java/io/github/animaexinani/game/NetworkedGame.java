package io.github.animaexinani.game;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.input.InputBindings;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;
import io.github.animaexinani.game.assets.ResourceLoader;
import io.github.animaexinani.game.nentities.Asteroid;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.playfield.CombinedWorld;

import org.dyn4j.geometry.Vector2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;

    // master world manager
    private final CombinedWorld combinedWorld;
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

    // // 20 Ticks Per Second = 0.05 seconds per tick
    // private static final double TIME_STEP = 1.0 / 20.0;
    // 60 for now for smoother gameplay
    private static final double TIME_STEP = 1.0 / 60.0;

    @Override
    protected boolean iterate() {
        long currentTime = System.nanoTime();
        // divide by 1 billion to convert nanoseconds to seconds
        float frameTime = (currentTime - this.lastTime) / 1_000_000_000.0f;
        this.lastTime = currentTime;

        // prevent the "Spiral of Death" if the window is dragged or minimized
        if (frameTime > 0.25f)
            frameTime = 0.25f;

        // pour that time into our bucket
        this.accumulator += frameTime;

        while (this.accumulator >= TIME_STEP) {
            Duration dt = Duration.ofNanos((long) (1_000_000_000L * TIME_STEP));

            this.combinedWorld.preUpdate(dt);
            this.combinedWorld.handleInput(this.inputListener, dt);
            this.combinedWorld.update(dt);
            this.combinedWorld.postUpdate(dt);

            // remove one tick's worth of time from the bucket
            this.accumulator -= TIME_STEP;
        }

        // the Render Loop
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);

        this.combinedWorld.render(renderer);

        renderer.present();
        return true;
    }

    @Override
    public void close() {
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
        var centerX = clientSize.width() / 2.0f;
        var centerY = clientSize.height() / 2.0f;

        // initialize the player ship
        this.playerShip = new PlayerShip();
        this.playerShip.physicsBody().translate(centerX, centerY);

        List<Entity> initialEntities = new ArrayList<>();
        initialEntities.add(this.playerShip);

        // add test asteroid
        var testAsteroid = new Asteroid(EntityType.ASTEROID, 200.0f, 200.0f, 60.0, 30.0);
        initialEntities.add(testAsteroid);

        Random rand = new Random();

        // Add four more random asteroids
        for (int i = 0; i < 4; i++) {
            float x = rand.nextFloat() * clientSize.width();
            float y = rand.nextFloat() * clientSize.height();
            double vx = rand.nextDouble() * 100 - 50;
            double vy = rand.nextDouble() * 100 - 50;
            var asteroid = new Asteroid(EntityType.ASTEROID, x, y, vx, vy);
            initialEntities.add(asteroid);
        }

        var sizeF = new SizeF(clientSize.width(), clientSize.height());
        var worldBuilder = new CombinedWorld.Builder()
                .withEntities(initialEntities)
                .withLocalPlayerId(this.playerShip.id())
                .withSize(sizeF)
                .withVisualFactory(EntityType.BULLET, entity -> {
                    var bulletPoints = new PointF[] {
                            new PointF(5.0f, 0.0f),
                            new PointF(-2.0f, 2.5f),
                            new PointF(-2.0f, -2.5f)
                    };
                    return new ConvexPolygon(bulletPoints, Color.WHITE);
                })
                .withVisualFactory(EntityType.ASTEROID, entity -> {
                    var points = Asteroid.getAsteroidLocalPointsForType(EntityType.ASTEROID).toArray(PointF[]::new);
                    return new ConvexPolygon(points, new Color(0.6f, 0.6f, 0.6f, 1.0f));
                })
                .withVisualFactory(EntityType.PLAYER,
                        entity -> new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new),
                                new Color(0.0f, 1.0f, 0.0f, 1.0f)));

        this.combinedWorld = worldBuilder.build();

        // actually create the InputSystem object in memory
        var bindings = InputBindings.defaultBindings();
        this.inputListener = new GameInputListener(bindings);
        this.rebindingController = new RebindingController(bindings);

        this.assetManager().registerLoader(new ResourceLoader());

        // tell the engine to send key presses to the inputSystem, not 'this'
        this.eventRegistry().register(KeyboardListener.class, this.inputListener);
        this.eventRegistry().register(KeyboardListener.class, this.rebindingController);

        // reset the clock right before the constructor finishes!
        this.lastTime = System.nanoTime();
    }
}
