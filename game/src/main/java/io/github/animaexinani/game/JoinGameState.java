package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.ui.UITextField;
import io.github.animaexinani.engine.ui.UITextLabel;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

/**
 * Screen to input IP and Port to join a game.
 */
public class JoinGameState extends BaseMenuState {

    /**
     * Creates a new JoinGameState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     * @param settingsManager the settings manager
     * @param rebindingController the rebinding controller
     */
    public JoinGameState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;

        // IP Address
        Text ipLabelText = new Text(fontFace, "IP Address:");
        ipLabelText.fontSize(24.0f);
        ipLabelText.color(Color.WHITE);
        ipLabelText.origin(TextOrigin.CENTER);
        UITextLabel ipLabel = new UITextLabel(ipLabelText);
        ipLabel.position(new PointF(centerX, 200));
        this.components.add(ipLabel);

        Text ipFieldText = new Text(fontFace, "");
        ipFieldText.fontSize(24.0f);
        ipFieldText.color(Color.WHITE);
        ipFieldText.origin(TextOrigin.CENTER);
        UITextField ipField = new UITextField(ipFieldText);
        ipField.position(new PointF(centerX - 150, 230));
        ipField.size(new SizeF(300, 40));
        this.components.add(ipField);

        // Port
        Text portLabelText = new Text(fontFace, "Port:");
        portLabelText.fontSize(24.0f);
        portLabelText.color(Color.WHITE);
        portLabelText.origin(TextOrigin.CENTER);
        UITextLabel portLabel = new UITextLabel(portLabelText);
        portLabel.position(new PointF(centerX, 300));
        this.components.add(portLabel);

        Text portFieldText = new Text(fontFace, "");
        portFieldText.fontSize(24.0f);
        portFieldText.color(Color.WHITE);
        portFieldText.origin(TextOrigin.CENTER);
        UITextField portField = new UITextField(portFieldText);
        portField.position(new PointF(centerX - 150, 330));
        portField.size(new SizeF(300, 40));
        portField.text(String.valueOf(this.settingsManager.getSettings().getNetworking().getPreferredPort()));
        this.components.add(portField);

        this.components.add(this.createButton("Connect", centerX, 500, () -> {
            this.stateManager.transitionTo(new ConnectingState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));

        this.components.add(this.createButton("Back", centerX, 600, () -> {
            this.stateManager.transitionTo(new MultiplayerMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }
}
