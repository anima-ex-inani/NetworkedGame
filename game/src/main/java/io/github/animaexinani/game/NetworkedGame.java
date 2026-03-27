package io.github.animaexinani.game;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;

    @Override
    protected boolean iterate() {
        var renderer = this.mainWindow.getRenderer();

        renderer.clear(Color.BLACK);

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
        var appOptions = new ApplicationOptions("Networked Game", "0.1.0-alpha.1", "io.github.animaexinani.networkedgame");
        super(appOptions);

        var windowOptions = new WindowOptions("Networked Game", 960, 720);
        windowOptions.setResizable(true);

        var windowFactory = super.windowFactory();
        this.mainWindow = windowFactory.createWindow(windowOptions);
    }
}
