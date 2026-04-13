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

    private static final ApplicationOptions OPTIONS = new ApplicationOptions("Networked Game", "0.1.0-alpha.1", "io.github.animaexinani.networkedgame");

    @Override
    protected boolean iterate() {
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);

        if (this.inputSystem.isKeyPressed(InputMap.KEY_W)) {
            this.playerShip.applyThrust();
        }
        if (this.inputSystem.isKeyPressed(InputMap.KEY_A)) {
            this.playerShip.turnLeft();
        }
        if (this.inputSystem.isKeyPressed(InputMap.KEY_D)) {
            this.playerShip.turnRight();
        }

        // physics and math
        this.playerShip.update();

        // pass the ship directly to the renderer
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
    }
}