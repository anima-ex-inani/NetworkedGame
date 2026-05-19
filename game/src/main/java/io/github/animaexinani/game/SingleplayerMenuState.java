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
 * The singleplayer menu of the game.
 */
public class SingleplayerMenuState extends BaseMenuState {

    /**
     * Creates a new SingleplayerMenuState.
     * @param window the game window
     * @param stateManager the state manager
     * @param fontFace the font to use
     * @param eventRegistry the event registry
     */
    public SingleplayerMenuState(Window window, GameStateManager stateManager, FontFace fontFace, EventRegistry eventRegistry) {
        super(window, stateManager, fontFace, eventRegistry);

        float centerX = 1920 / 2.0f;
        float startY = 300;
        float spacing = 100;

        this.components.add(this.createButton("New Game", centerX, startY, () -> {
            this.stateManager.transitionTo(new PlayState(this.window, this.fontFace, this.stateManager, this.eventRegistry));
        }));
        this.components.add(this.createButton("High Scores", centerX, startY + spacing, () -> {
            // High scores logic
        }));
        this.components.add(this.createButton("Back", centerX, startY + 2 * spacing, () -> {
            this.stateManager.transitionTo(new MainMenuState(this.window, this.stateManager, this.fontFace, this.eventRegistry));
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
