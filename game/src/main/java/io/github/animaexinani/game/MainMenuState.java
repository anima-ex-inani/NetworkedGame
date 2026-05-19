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
 * The main menu of the game.
 */
public class MainMenuState extends BaseMenuState {

    /**
     * Creates a new MainMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     */
    public MainMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry) {
        super(window, stateManager, fontFace, eventRegistry);

        float centerX = 1920 / 2.0f;
        float startY = 300;
        float spacing = 100;

        this.components.add(this.createButton("Singleplayer", centerX, startY, () -> {
            this.stateManager.transitionTo(new SingleplayerMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry));
        }));
        this.components.add(this.createButton("Multiplayer", centerX, startY + spacing, () -> {
            this.stateManager.transitionTo(new MultiplayerMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry));
        }));
        this.components.add(this.createButton("Settings", centerX, startY + 2 * spacing, () -> {
            this.stateManager.transitionTo(new SettingsState(this.window, this.stateManager, this.fontFace, this.eventRegistry));
        }));
        this.components.add(this.createButton("Quit", centerX, startY + 3 * spacing, () -> {
            System.exit(0);
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
