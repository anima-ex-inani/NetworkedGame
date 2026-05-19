package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

import java.time.Duration;

/**
 * A non-pausing pause menu that appears over the active gameplay.
 */
public class PauseState extends BaseMenuState {
    private final PlayState playState;
    private final Text pauseTitle;

    /**
     * Creates a new PauseState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param playState the active play state to continue updating/rendering
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     */
    public PauseState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, PlayState playState, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);
        this.playState = playState;

        this.pauseTitle = new Text(fontFace, "PAUSED");
        this.pauseTitle.fontSize(64.0f);
        this.pauseTitle.color(Color.WHITE);
        this.pauseTitle.origin(TextOrigin.CENTER);
        this.pauseTitle.translation(new PointF(1920 / 2.0f, 200));

        float centerX = 1920 / 2.0f;
        float startY = 400;
        float spacing = 100;

        this.components.add(this.createButton("Resume", centerX, startY, () -> {
            this.stateManager.popState();
        }));

        this.components.add(this.createButton("Quit to Menu", centerX, startY + spacing, () -> {
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    @Override
    public void update(Duration dt) {
        // Continue updating the play state (non-pausing)
        this.playState.update(dt);
        super.update(dt);
    }

    @Override
    public void render(Renderer renderer) {
        // Render play state as background
        this.playState.render(renderer);
        
        // Render pause menu on top
        renderer.draw(this.pauseTitle);
        super.render(renderer);
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        super.onKeyEvent(event);
        // Pressing ESC again resumes the game
        if (event.action() == KeyEvent.Action.PRESS && event.scancode() == 41) { // 41 is ESCAPE
            this.stateManager.popState();
        }
    }
}
