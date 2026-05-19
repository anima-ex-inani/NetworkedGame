package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UITextField;
import io.github.animaexinani.engine.ui.UITextLabel;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

/**
 * The multiplayer menu of the game.
 */
public class MultiplayerMenuState extends BaseMenuState {

    /**
     * Creates a new MultiplayerMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     */
    public MultiplayerMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;
        
        Text labelText = new Text(fontFace, "Player Name:");
        labelText.fontSize(24.0f);
        labelText.color(Color.WHITE);
        labelText.origin(TextOrigin.CENTER);
        UITextLabel nameLabel = new UITextLabel(labelText);
        nameLabel.position(new PointF(centerX, 200));
        this.components.add(nameLabel);

        Text fieldText = new Text(fontFace, "");
        fieldText.fontSize(24.0f);
        fieldText.color(Color.WHITE);
        fieldText.origin(TextOrigin.CENTER);
        UITextField nameField = new UITextField(fieldText);
        nameField.position(new PointF(centerX - 150, 230));
        nameField.size(new SizeF(300, 40));
        nameField.text(this.settingsManager.getSettings().getPlayerName());
        this.components.add(nameField);

        float startY = 400;
        float spacing = 100;

        this.components.add(this.createButton("Create Game", centerX, startY, () -> {
            // Save player name before starting
            this.settingsManager.getSettings().setPlayerName(nameField.text());
            this.settingsManager.save();
            this.stateManager.transitionTo(new PlayState(this.window, this.fontFace, this.stateManager, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
        this.components.add(this.createButton("Join Game", centerX, startY + spacing, () -> {
            // Save player name before joining
            this.settingsManager.getSettings().setPlayerName(nameField.text());
            this.settingsManager.save();
            this.stateManager.transitionTo(new JoinGameState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
        this.components.add(this.createButton("Back", centerX, startY + 2 * spacing, () -> {
            // Save player name when going back
            this.settingsManager.getSettings().setPlayerName(nameField.text());
            this.settingsManager.save();
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }
}
