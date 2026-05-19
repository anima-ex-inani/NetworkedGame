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
import java.util.List;
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

        // List Interfaces
        List<String> interfaces = new ArrayList<>();
        interfaces.add("all");
        try {
            var nets = NetworkInterface.getNetworkInterfaces();
            for (var netint : Collections.list(nets)) {
                if (!netint.isLoopback() || netint.getName().equals("lo")) {
                    interfaces.add(netint.getName());
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, "Failed to list network interfaces", e);
        }

        float interfaceY = 320;
        for (String iface : interfaces) {
            UIButton btn = this.createInterfaceButton(iface, centerX, interfaceY);
            this.interfaceButtons.add(btn);
            this.components.add(btn);
            interfaceY += 45;
            if (interfaceY > 800) break; // Limit list
        }
        this.updateInterfaceButtons();

        // Apply Button
        this.components.add(this.createButton("Apply", centerX - 160, 900, () -> {
            try {
                int port = Integer.parseInt(this.portField.text());
                this.settingsManager.getSettings().getNetworking().setPreferredPort(port);
                this.settingsManager.getSettings().getNetworking().setNetworkInterface(this.selectedInterface);
                this.settingsManager.save();
                this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
            } catch (NumberFormatException e) {
                // Should show error in UI really
            }
        }));

        // Cancel Button
        this.components.add(this.createButton("Cancel", centerX + 160, 900, () -> {
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    private UIButton createInterfaceButton(String name, float x, float y) {
        Text text = new Text(this.fontFace, name);
        text.fontSize(20.0f);
        text.color(Color.WHITE);
        text.origin(TextOrigin.CENTER);
        
        UIButton button = new UIButton(text, () -> {
            this.selectedInterface = name;
            this.updateInterfaceButtons();
        });
        button.position(new PointF(x - 100, y - 15));
        button.size(new SizeF(200, 30));
        return button;
    }

    private void updateInterfaceButtons() {
        for (UIButton btn : this.interfaceButtons) {
            if (btn.text().text().equals(this.selectedInterface)) {
                btn.backgroundColor(Color.GREEN);
            } else {
                btn.backgroundColor(Color.GRAY);
            }
        }
    }
}
