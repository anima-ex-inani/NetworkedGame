package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.ui.UITextLabel;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.game.settings.SettingsManager;
import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.engine.input.RebindingController;

import java.util.EnumMap;
import java.util.Map;

/**
 * Settings menu for keyboard configuration.
 */
public class KeybindsSettingsState extends BaseMenuState {
    private final Map<GameAction, UIButton> rebindButtons = new EnumMap<>(GameAction.class);
    private GameAction activeRebindAction = null;

    public KeybindsSettingsState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry, SettingsManager settingsManager, RebindingController rebindingController) {
        super(window, stateManager, fontFace, eventRegistry, settingsManager, rebindingController);

        float centerX = 1920 / 2.0f;
        float startY = 200;
        float spacing = 60;

        int index = 0;
        for (GameAction action : GameAction.values()) {
            float y = startY + index * spacing;
            
            Text labelText = new Text(fontFace, action.name() + ":");
            labelText.fontSize(24.0f);
            labelText.color(Color.WHITE);
            labelText.origin(TextOrigin.CENTER_RIGHT);
            UITextLabel label = new UITextLabel(labelText);
            label.position(new PointF(centerX - 20, y));
            this.components.add(label);

            Integer scancode = settingsManager.getSettings().getKeybinds().get(action.name());
            String keyName = scancode != null ? String.valueOf(scancode) : "None";
            
            UIButton btn = this.createRebindButton(action, keyName, centerX + 20, y);
            this.rebindButtons.put(action, btn);
            this.components.add(btn);
            
            index++;
        }

        // Set rebinding callback
        this.rebindingController.setCallback((action, scancode) -> {
            UIButton btn = this.rebindButtons.get(action);
            if (btn != null) {
                btn.text().text(String.valueOf(scancode));
                btn.backgroundColor(Color.GRAY);
            }
            this.activeRebindAction = null;
            // Update settings map
            this.settingsManager.getSettings().getKeybinds().put(action.name(), scancode);
        });

        // Apply Button
        this.components.add(this.createButton("Apply", centerX - 160, 900, () -> {
            this.settingsManager.save();
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));

        // Cancel Button
        this.components.add(this.createButton("Cancel", centerX + 160, 900, () -> {
            // Re-load settings to discard unsaved changes in memory
            this.settingsManager.setSettings(this.settingsManager.load());
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry, this.settingsManager, this.rebindingController));
        }));
    }

    private UIButton createRebindButton(GameAction action, String label, float x, float y) {
        Text text = new Text(this.fontFace, label);
        text.fontSize(24.0f);
        text.color(Color.WHITE);
        text.origin(TextOrigin.CENTER);
        
        UIButton button = new UIButton(text, () -> {
            if (this.activeRebindAction != null) {
                // Reset previous active button
                this.rebindButtons.get(this.activeRebindAction).backgroundColor(Color.GRAY);
            }
            this.activeRebindAction = action;
            this.rebindingController.startRebinding(action);
            this.rebindButtons.get(action).backgroundColor(Color.GREEN);
            this.rebindButtons.get(action).text().text("Press any key...");
        });
        button.position(new PointF(x, y - 20));
        button.size(new SizeF(250, 40));
        return button;
    }

    @Override
    public void exit() {
        super.exit();
        this.rebindingController.setCallback(null);
        this.rebindingController.cancelRebinding();
    }
}
