package io.github.animaexinani.game;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.nentities.Asteroid;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.listeners.KeyboardListener;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.game.settings.SettingsManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The game state representing the active gameplay.
 */
public class PlayState implements GameState, KeyboardListener {
    private final CombinedWorld combinedWorld;
    private final PlayerShip playerShip;
    private Text entityCountText;
    private final Window window;
    private final GameStateManager stateManager;
    private final EventRegistry eventRegistry;
    private final FontFace fontFace;
    private final SettingsManager settingsManager;

    /**
     * Creates a new PlayState.
     * @param window the game window
     * @param fontFace the font to use for UI
     * @param stateManager the state manager
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     */
    public PlayState(Window window, FontFace fontFace, GameStateManager stateManager, EventRegistry eventRegistry, SettingsManager settingsManager) {
        this.window = window;
        this.fontFace = fontFace;
        this.stateManager = stateManager;
        this.eventRegistry = eventRegistry;
        this.settingsManager = settingsManager;

        float width = window != null ? window.clientSize().width() : 1920.0f;
        float height = window != null ? window.clientSize().height() : 1080.0f;
        var centerX = width / 2.0f;
        var centerY = height / 2.0f;

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
            float x = rand.nextFloat() * width;
            float y = rand.nextFloat() * height;
            double vx = rand.nextDouble() * 100 - 50;
            double vy = rand.nextDouble() * 100 - 50;
            var asteroid = new Asteroid(EntityType.ASTEROID, x, y, vx, vy);
            initialEntities.add(asteroid);
        }

        var sizeF = new SizeF(width, height);
        var worldBuilder = new CombinedWorld.Builder()
                .withEntities(initialEntities)
                .withLocalPlayerId(this.playerShip.id())
                .withSize(sizeF)
                .withVisualFactory(EntityType.BULLET, entity -> {
                    PointF[] bulletPoints = {
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
                .withVisualFactory(EntityType.PLAYER, entity -> {
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), Color.GREEN);
                });

        this.combinedWorld = worldBuilder.build();

        if (fontFace != null) {
            this.entityCountText = new Text(fontFace, "Entities: 0");
            this.entityCountText.translation(new PointF(10.0f, 10.0f));
            this.entityCountText.origin(TextOrigin.TOP_LEFT);
            this.entityCountText.fontSize(24.0f);
            this.entityCountText.color(Color.WHITE);
        }
    }

    @Override
    public void enter() {
        this.eventRegistry.register(KeyboardListener.class, this);
    }

    @Override
    public void update(Duration dt) {
        this.combinedWorld.preUpdate(dt);
        this.combinedWorld.update(dt);
        this.combinedWorld.postUpdate(dt);
    }

    @Override
    public void render(Renderer renderer) {
        this.combinedWorld.render(renderer);

        if (this.entityCountText != null) {
            int count = this.combinedWorld.entities().size();
            this.entityCountText.text("Entities: " + count);
            renderer.draw(this.entityCountText);
        }
    }

    @Override
    public void handleInput(GameInputListener inputListener, Duration dt) {
        this.combinedWorld.handleInput(inputListener, dt);
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        if (event.action() == KeyEvent.Action.PRESS && event.scancode() == 41) { // 41 is ESCAPE in SDL scancodes
            this.stateManager.transitionTo(new PauseState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this, this.settingsManager));
        }
    }

    @Override
    public void exit() {
        this.eventRegistry.remove(KeyboardListener.class, this);
    }
}
