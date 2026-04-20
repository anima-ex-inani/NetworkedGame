package io.github.animaexinani.game;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;
import io.github.animaexinani.game.assets.ResourceLoader;
import io.github.animaexinani.game.classes.Ship;
import io.github.animaexinani.game.util.function.InputMap;
import io.github.animaexinani.game.util.function.InputSystem;

public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;
    private final Ship playerShip;

    // instantiate Input System
    private final InputSystem inputSystem;

    private static final ApplicationOptions OPTIONS = new ApplicationOptions("Networked Game", "0.1.0-alpha.3", "io.github.animaexinani.networkedgame");

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

            if (this.inputSystem.isKeyPressed(InputMap.KEY_W)) {
                this.playerShip.applyThrust(dt);
            }
            if (this.inputSystem.isKeyPressed(InputMap.KEY_A)) {
                this.playerShip.turnLeft(dt);
            }
            if (this.inputSystem.isKeyPressed(InputMap.KEY_D)) {
                this.playerShip.turnRight(dt);
            }
            // TODO: Wire KEY_SPACE once projectiles are implemented
            // if (this.inputSystem.isKeyPressed(InputMap.KEY_SPACE)) {
            //     this.playerShip.firePrimary(dt);
            // }

            // physics and math happen strictly at 20 TPS
            this.playerShip.update(dt, currentWidth, currentHeight);

            // remove one tick's worth of time from the bucket
            this.accumulator -= TIME_STEP;
        }

        // 4. the Render Loop
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);

        renderer.draw(this.playerShip);

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

        var windowOptions = new WindowOptions("Networked Game", 960, 720);
        windowOptions.setResizable(true);

        var windowFactory = super.windowFactory();
        this.mainWindow = windowFactory.createWindow(windowOptions);

        var clientSize = this.mainWindow.clientSize();
        var centerX = clientSize.width() / 2.0f;
        var centerY = clientSize.height() / 2.0f;

        this.playerShip = new Ship(centerX, centerY);

        // actually create the InputSystem object in memory
        this.inputSystem = new InputSystem();

        this.assetManager().registerLoader(new ResourceLoader());
        
        // tell the engine to send key presses to the inputSystem, not 'this'
        this.eventRegistry().register(KeyboardListener.class, this.inputSystem);
        // reset the clock right before the constructor finishes!
        this.lastTime = System.nanoTime();
    }
}