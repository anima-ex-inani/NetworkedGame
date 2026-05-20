package io.github.animaexinani.engine.ui;

import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.rendering.Renderer;

/**
 * A UI component that displays static text.
 */
public class UITextLabel extends UIComponent {
    private final Text text;

    /**
     * Creates a new UITextLabel.
     * @param text the text to display
     */
    public UITextLabel(Text text) {
        this.text = text;
    }

    @Override
    public void render(Renderer renderer) {
        if (!this.visible()) return;
        this.text.translation(this.position());
        renderer.draw(this.text);
    }

    public Text text() { return this.text; }
}
