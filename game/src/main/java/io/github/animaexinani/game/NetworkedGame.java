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

import java.util.logging.Level;
import java.util.logging.Logger;

public final class NetworkedGame extends Application {
    private static final Logger LOGGER = Logger.getLogger(NetworkedGame.class.getName());
    private final Window mainWindow;

    private final Geometry colorTriangle;

    @Override
    protected boolean iterate() {
        var renderer = this.mainWindow.getRenderer();

        renderer.clear(Color.BLACK);
        renderer.draw(this.colorTriangle);

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

        var clientSize = this.mainWindow.clientSize();

        var width = clientSize.width();
        var height = clientSize.height();
        var centerX = width / 2.0f;
        var centerY = height / 2.0f;

        var vertices = new Vertex[] {
            new Vertex(new PointF(centerX, centerY - 200.0f), new Point(0, 0), new Color(1.0f, 0.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(centerX - 173.2f, centerY + 100.0f), new Point(0, 0), new Color(0.0f, 1.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(centerX + 173.2f, centerY + 100.0f), new Point(0, 0), new Color(0.0f, 0.0f, 1.0f, 1.0f))
        };

        var indices = new int[] { 0, 1, 2 };

        this.colorTriangle = new Geometry(vertices, indices, null);
    }
}
