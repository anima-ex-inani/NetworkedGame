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
    private static final int PAGE_SIZE = 8;
    private static final int MAX_DISPLAY_LENGTH = 25;

    private final UITextField portField;
    private String selectedInterface;
    private int currentPage = 0;

    private record InterfaceInfo(String internalName, String displayName) {}
    private final List<InterfaceInfo> allInterfaces = new ArrayList<>();
    private final List<UIButton> interfaceButtons = new ArrayList<>();
    private final Map<UIButton, String> buttonToInternalName = new HashMap<>();
    
    private UIButton prevPageBtn;
    private UIButton nextPageBtn;
    private UITextLabel pageLabel;

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

        // Discover Interfaces
        this.allInterfaces.add(new InterfaceInfo("all", "All Interfaces"));
        try {
            var nets = NetworkInterface.getNetworkInterfaces();
            for (var netint : Collections.list(nets)) {
                if (netint.isUp()) {
                    String displayName = netint.getDisplayName();
                    if (displayName.length() > MAX_DISPLAY_LENGTH) {
                        displayName = displayName.substring(0, MAX_DISPLAY_LENGTH) + "...";
                    }
                    this.allInterfaces.add(new InterfaceInfo(netint.getName(), displayName));
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, "Failed to list network interfaces", e);
        }

        // Find page of selected interface
        for (int i = 0; i < this.allInterfaces.size(); i++) {
            if (this.allInterfaces.get(i).internalName().equals(this.selectedInterface)) {
                this.currentPage = i / PAGE_SIZE;
                break;
            }
        }

        // Page Label
        Text pageText = new Text(fontFace, "");
        pageText.fontSize(18.0f);
        pageText.color(Color.WHITE);
        pageText.origin(TextOrigin.CENTER);
        this.pageLabel = new UITextLabel(pageText);
        this.pageLabel.position(new PointF(centerX, 850));
        this.components.add(this.pageLabel);

        // Navigation Buttons
        this.prevPageBtn = this.createNavigationButton("< Previous", centerX - 200, 850, () -> {
            if (this.currentPage > 0) {
                this.currentPage--;
                this.rebuildInterfaceButtons();
            }
        });
        this.nextPageBtn = this.createNavigationButton("Next >", centerX + 200, 850, () -> {
            if ((this.currentPage + 1) * PAGE_SIZE < this.allInterfaces.size()) {
                this.currentPage++;
                this.rebuildInterfaceButtons();
            }
        });
        this.components.add(this.prevPageBtn);
        this.components.add(this.nextPageBtn);

        this.rebuildInterfaceButtons();

        // Apply Button
        this.components.add(this.createButton("Apply", centerX - 160, 950, () -> {
            try {
                int port = Integer.parseInt(this.portField.text());
                if (port >= 0 && port <= 65535) {
                    this.settingsManager.getSettings().getNetworking().setPreferredPort(port);
                    this.settingsManager.getSettings().getNetworking().setNetworkInterface(this.selectedInterface);
                    this.settingsManager.save();
                    this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
                } else {
                    this.portField.backgroundColor(new Color(0.5f, 0.0f, 0.0f, 1.0f)); // Dark Red
                }
            } catch (NumberFormatException e) {
                this.portField.backgroundColor(new Color(0.5f, 0.0f, 0.0f, 1.0f)); // Dark Red
            }
        }));

        // Cancel Button
        this.components.add(this.createButton("Cancel", centerX + 160, 950, () -> {
            this.settingsManager.setSettings(this.settingsManager.load());
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    private UIButton createNavigationButton(String label, float x, float y, Runnable onClick) {
        Text text = new Text(this.fontFace, label);
        text.fontSize(18.0f);
        text.color(Color.WHITE);
        text.origin(TextOrigin.CENTER);
        UIButton btn = new UIButton(text, onClick);
        btn.position(new PointF(x - 80, y - 15));
        btn.size(new SizeF(160, 30));
        return btn;
    }

    private void rebuildInterfaceButtons() {
        // Remove old buttons
        for (UIButton btn : this.interfaceButtons) {
            this.components.remove(btn);
        }
        this.interfaceButtons.clear();
        this.buttonToInternalName.clear();

        float startY = 320;
        float spacing = 45;
        
        int start = this.currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, this.allInterfaces.size());

        for (int i = start; i < end; i++) {
            InterfaceInfo info = this.allInterfaces.get(i);
            this.addInterfaceButton(info.internalName(), info.displayName(), 1920 / 2.0f, startY + (i - start) * spacing);
        }

        this.updateInterfaceButtons();
        
        // Update navigation visibility and label
        int totalPages = (this.allInterfaces.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        this.pageLabel.text().text("Page " + (this.currentPage + 1) + " / " + totalPages);
        this.prevPageBtn.visible(this.currentPage > 0);
        this.nextPageBtn.visible((this.currentPage + 1) * PAGE_SIZE < this.allInterfaces.size());
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
