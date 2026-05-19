package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;

/**
 * The settings menu of the game.
 */
public class SettingsState extends BaseMenuState {

    /**
     * Creates a new SettingsState.
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     */
    public SettingsState(GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry) {
        super(stateManager, fontFace, eventRegistry);

        float centerX = 1920 / 2.0f;
        float startY = 300;
        float spacing = 100;

        this.components.add(this.createButton("Keybinds", centerX, startY, () -> {
            // Keybinds settings
        }));
        this.components.add(this.createButton("Networking", centerX, startY + spacing, () -> {
            // Networking settings
        }));
        this.components.add(this.createButton("Back", centerX, startY + 2 * spacing, () -> {
            this.stateManager.transitionTo(new MainMenuState(this.stateManager, this.fontFace, this.eventRegistry));
        }));
    }

    private UIButton createButton(String label, float x, float y, Runnable onClick) {
        Text text = new Text(this.fontFace, label);
        text.fontSize(32.0f);
        text.color(Color.WHITE);
        text.origin(TextOrigin.CENTER);
        
        UIButton button = new UIButton(text, onClick);
        button.position(new PointF(x - 150, y - 25));
        button.size(new SizeF(300, 50));
        return button;
    }
}
