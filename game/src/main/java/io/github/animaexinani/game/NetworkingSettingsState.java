package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.ui.UITextField;
import io.github.animaexinani.engine.ui.UITextLabel;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.RebindingController;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Settings menu for networking configuration.
 */
public class NetworkingSettingsState extends BaseMenuState {
    private static final Logger LOGGER = Logger.getLogger(NetworkingSettingsState.class.getName());
    private final UITextField portField;
    private String selectedInterface;
    private final List<UIButton> interfaceButtons = new ArrayList<>();
    private final Map<UIButton, String> buttonToInternalName = new HashMap<>();

    public NetworkingSettingsState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;
        this.selectedInterface = settingsManager.getSettings().getNetworking().getNetworkInterface();

        // Port Label
        Text portLabelText = new Text(fontFace, "Preferred Port:");
        portLabelText.fontSize(24.0f);
        portLabelText.color(Color.WHITE);
        portLabelText.origin(TextOrigin.CENTER);
        UITextLabel portLabel = new UITextLabel(portLabelText);
        portLabel.position(new PointF(centerX, 150));
        this.components.add(portLabel);

        // Port Field
        Text portFieldText = new Text(fontFace, "");
        portFieldText.fontSize(24.0f);
        portFieldText.color(Color.WHITE);
        portFieldText.origin(TextOrigin.CENTER_LEFT);
        this.portField = new UITextField(portFieldText);
        this.portField.position(new PointF(centerX - 100, 180));
        this.portField.size(new SizeF(200, 40));
        this.portField.text(String.valueOf(settingsManager.getSettings().getNetworking().getPreferredPort()));
        this.components.add(this.portField);

        // Interfaces Label
        Text interfaceLabelText = new Text(fontFace, "Network Interface:");
        interfaceLabelText.fontSize(24.0f);
        interfaceLabelText.color(Color.WHITE);
        interfaceLabelText.origin(TextOrigin.CENTER);
        UITextLabel interfaceLabel = new UITextLabel(interfaceLabelText);
        interfaceLabel.position(new PointF(centerX, 280));
        this.components.add(interfaceLabel);

        float interfaceY = 320;
        
        // "All Interfaces" option
        this.addInterfaceButton("all", "All Interfaces", centerX, interfaceY);
        interfaceY += 45;

        // List Physical/Virtual Interfaces
        try {
            var nets = NetworkInterface.getNetworkInterfaces();
            for (var netint : Collections.list(nets)) {
                if (netint.isUp()) {
                    this.addInterfaceButton(netint.getName(), netint.getDisplayName(), centerX, interfaceY);
                    interfaceY += 45;
                    if (interfaceY > 850) break; // Limit list to prevent overflowing screen
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, "Failed to list network interfaces", e);
        }
        
        this.updateInterfaceButtons();

        // Apply Button
        this.components.add(this.createButton("Apply", centerX - 160, 950, () -> {
            try {
                int port = Integer.parseInt(this.portField.text());
                this.settingsManager.getSettings().getNetworking().setPreferredPort(port);
                this.settingsManager.getSettings().getNetworking().setNetworkInterface(this.selectedInterface);
                this.settingsManager.save();
                this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
            } catch (NumberFormatException e) {
                // Ignore invalid port for now
            }
        }));

        // Cancel Button
        this.components.add(this.createButton("Cancel", centerX + 160, 950, () -> {
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    private void addInterfaceButton(String internalName, String displayName, float x, float y) {
        Text text = new Text(this.fontFace, displayName);
        text.fontSize(18.0f);
        text.color(Color.WHITE);
        text.origin(TextOrigin.CENTER);
        
        UIButton button = new UIButton(text, () -> {
            this.selectedInterface = internalName;
            this.updateInterfaceButtons();
        });
        button.position(new PointF(x - 300, y - 15));
        button.size(new SizeF(600, 30));
        
        this.interfaceButtons.add(button);
        this.buttonToInternalName.put(button, internalName);
        this.components.add(button);
    }

    private void updateInterfaceButtons() {
        for (UIButton btn : this.interfaceButtons) {
            String internalName = this.buttonToInternalName.get(btn);
            if (internalName != null && internalName.equals(this.selectedInterface)) {
                btn.backgroundColor(Color.GREEN);
            } else {
                btn.backgroundColor(Color.GRAY);
            }
        }
    }
}
