package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;

import java.time.Duration;

/**
 * A simple splash screen that transitions to the main menu after a delay.
 */
public class SplashState implements GameState {
    private final GameStateManager stateManager;
    private final FontFace fontFace;
    private final EventRegistry eventRegistry;
    private final Text loadingText;
    private Duration timer = Duration.ZERO;
    private static final Duration SPLASH_DURATION = Duration.ofSeconds(2);

    /**
     * Creates a new SplashState.
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     */
    public SplashState(GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry) {
        this.stateManager = stateManager;
        this.fontFace = fontFace;
        this.eventRegistry = eventRegistry;
        this.loadingText = new Text(fontFace, "Loading...");
        this.loadingText.fontSize(48.0f);
        this.loadingText.color(Color.WHITE);
        this.loadingText.origin(TextOrigin.CENTER);
        this.loadingText.translation(new PointF(1920 / 2.0f, 1080 / 2.0f));
    }

    @Override
    public void enter() {}

    @Override
    public void update(Duration dt) {
        this.timer = this.timer.plus(dt);
        if (this.timer.compareTo(SPLASH_DURATION) >= 0) {
            this.stateManager.transitionTo(new MainMenuState(this.stateManager, this.fontFace, this.eventRegistry));
        }
    }

    @Override
    public void render(Renderer renderer) {
        renderer.draw(this.loadingText);
    }

    @Override
    public void handleInput(io.github.animaexinani.engine.input.GameInputListener inputListener, Duration dt) {}

    @Override
    public void exit() {}
}
