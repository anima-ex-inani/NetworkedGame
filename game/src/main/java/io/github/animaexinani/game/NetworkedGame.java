package io.github.animaexinani.game;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.input.InputBindings;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;
import io.github.animaexinani.game.assets.ResourceLoader;
import io.github.animaexinani.game.entities.Asteroid;
import io.github.animaexinani.game.entities.Entity;
import io.github.animaexinani.game.entities.GameWorld;
import io.github.animaexinani.game.entities.Ship;

public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;
    
    // master world manager
    private final GameWorld gameWorld;
    // keep a direct reference to the player's ship specifically so we can route keyboard inputs to it
    private final Ship playerShip;

    // instantiate Input System
    private final GameInputListener inputListener;
    private final RebindingController rebindingController;

    private static final ApplicationOptions OPTIONS = new ApplicationOptions("Networked Game", "0.1.0-alpha.5", "io.github.animaexinani.networkedgame");

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
        if (frameTime > 0.25f) frameTime = 0.25f; 

        // pour that time into our bucket
        this.accumulator += frameTime;

        var clientSize = this.mainWindow.clientSize();
        float currentWidth = clientSize.width();
        float currentHeight = clientSize.height();

        while (this.accumulator >= TIME_STEP) {
            float dt = (float) TIME_STEP; // We pass the exact fixed step to the physics

            if (this.inputListener.isHeld(GameAction.MOVE_UP)) {
                this.playerShip.applyThrust(dt);
            }
            if (this.inputListener.isHeld(GameAction.MOVE_LEFT)) {
                this.playerShip.turnLeft(dt);
            }
            if (this.inputListener.isHeld(GameAction.MOVE_RIGHT)) {
                this.playerShip.turnRight(dt);
            }
            if (this.inputListener.isHeld(GameAction.ATTACK)) {
                this.playerShip.fire(this.gameWorld);
            }

            // STEP THE ENTIRE GAME WORLD
            // this single line calculates physics, handles wrapping, and updates visuals for everything.
            this.gameWorld.update(dt, currentWidth, currentHeight);
            
            // remove one tick's worth of time from the bucket
            this.accumulator -= TIME_STEP;
        }

        // the Render Loop
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);
        
        for (Entity entity : this.gameWorld.getEntities()) {
            entity.updateVisuals();
            renderer.draw(entity.getPolygon());
        }

        renderer.present();
        return true;
    }

    @Override
    public void close() {
        try {
            this.mainWindow.close();
        }
        catch (Exception e) {
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

        // initialize the GameWorld
        this.gameWorld = new GameWorld();

        // create the Ship and register it with the World
        this.playerShip = new Ship(centerX, centerY);
        this.gameWorld.addEntity(this.playerShip);
        
        // add test asteroid
        Asteroid testAsteroid = new Asteroid(200.0f, 200.0f, 60.0, 30.0);
        this.gameWorld.addEntity(testAsteroid);

        Random rand = new Random();

        // Add four more random asteroids
        for (int i = 0; i < 4; i++) {
            float x = rand.nextFloat() * 960f;      // random X between 0 and screen width
            float y = rand.nextFloat() * 720f;      // random Y between 0 and screen height
            double vx = rand.nextDouble() * 100 - 50;  // random velocity X between -50 and 50
            double vy = rand.nextDouble() * 100 - 50;  // random velocity Y between -50 and 50
            Asteroid asteroid = new Asteroid(x, y, vx, vy);
            this.gameWorld.addEntity(asteroid);
        }

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