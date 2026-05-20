package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;
import io.github.animaexinani.engine.ui.UITextLabel;

/**
 * A screen to display an error message.
 */
public class ErrorMenuState extends BaseMenuState {

    /**
     * Creates a new ErrorMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     * @param errorMessage the error message to display
     */
    public ErrorMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController, String errorMessage) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;

        Text titleText = new Text(fontFace, "An Error Occurred");
        titleText.fontSize(48.0f);
        titleText.color(new Color(1.0f, 0.0f, 0.0f, 1.0f));
        titleText.origin(TextOrigin.CENTER);
        UITextLabel titleLabel = new UITextLabel(titleText);
        titleLabel.position(new PointF(centerX, 200));
        this.components.add(titleLabel);

        Text msgText = new Text(fontFace, errorMessage);
        msgText.fontSize(32.0f);
        msgText.color(Color.WHITE);
        msgText.origin(TextOrigin.CENTER);
        UITextLabel msgLabel = new UITextLabel(msgText);
        msgLabel.position(new PointF(centerX, 400));
        this.components.add(msgLabel);

        this.components.add(this.createButton("Back to Main Menu", centerX, 600, () -> {
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }
}
