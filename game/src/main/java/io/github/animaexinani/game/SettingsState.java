package io.github.animaexinani.game;

import io.github.animaexinani.engine.font.FontFace;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.ui.UIButton;
import io.github.animaexinani.engine.EventRegistry;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.windowing.Window;

/**
 * The settings menu of the game.
 */
public class SettingsState extends BaseMenuState {

    /**
     * Creates a new SettingsState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     */
    public SettingsState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry) {
        super(window, stateManager, fontFace, eventRegistry);

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
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry));
        }));
    }
}
