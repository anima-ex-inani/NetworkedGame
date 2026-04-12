package io.github.animaexinani.game;

import io.github.animaexinani.engine.Application;
import io.github.animaexinani.engine.ApplicationOptions;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.vertex.Vertex;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowOptions;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.game.assets.ResourceLoader;
import io.github.animaexinani.game.classes.Ship;
import io.github.animaexinani.game.util.function.InputMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;

public final class NetworkedGame extends Application implements KeyboardListener {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;
    // replace the raw Geometry with Ship blueprint
    private final Ship playerShip;

    private static final ApplicationOptions OPTIONS = new ApplicationOptions("Networked Game", "0.1.0-alpha.1", "io.github.animaexinani.networkedgame");
    // memory bank for held keys
    public final Set<Integer> activeKeys = new HashSet<>();

    // listener methods
    @Override
    public void onKeyDown(int scancode) {
        this.activeKeys.add(scancode); // key is pressed, add to list
    } 

    @Override
    public void onKeyUp(int scancode) {
        this.activeKeys.remove(scancode); // key is release, remove it
    }

    @Override
    protected boolean iterate() {
        var renderer = this.mainWindow.getRenderer();
        renderer.clear(Color.BLACK);

        System.out.println("Currently holding keys: " + this.activeKeys);

        if (this.activeKeys.contains(InputMap.KEY_W)) {
            System.out.println("Applying forward thrust!");
            this.playerShip.applyThrust();
        }
        if (this.activeKeys.contains(InputMap.KEY_A)) {
            System.out.println("Rotating ship counter-clockwise!");
            this.playerShip.turnLeft();
        }
        if (this.activeKeys.contains(InputMap.KEY_D)) {
            System.out.println("Rotating ship clockwise!");
            this.playerShip.turnRight();
        }
        if (this.activeKeys.contains(InputMap.KEY_SPACE)) {
            System.out.println("Pew pew! Firing lasers!");
        }

        // physics and math
        this.playerShip.update();

        // draw to screen
        this.playerShip.draw(renderer);

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

        var width = clientSize.width();
        var height = clientSize.height();
        var centerX = width / 2.0f;
        var centerY = height / 2.0f;

        // we still create the initial geometry to give it colors, 
        // but the exact coordinates here do not matter much anymore 
        // because Ship.java will immediately overwrite them with its local shape
        var vertices = new Vertex[] {
            new Vertex(new PointF(0, 0), new Point(0, 0), new Color(1.0f, 0.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(0, 0), new Point(0, 0), new Color(0.0f, 1.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(0, 0), new Point(0, 0), new Color(0.0f, 0.0f, 1.0f, 1.0f))
        };
        var indices = new int[] { 0, 1, 2 };
        Geometry colorTriangle = new Geometry(vertices, indices, null);

        this.playerShip = new Ship(centerX, centerY, colorTriangle);

        this.assetManager().registerLoader(new ResourceLoader());
        // tell the engine we want to listen to the keyboard
        this.eventRegistry().register(KeyboardListener.class, this);
    }
    
}
