package io.github.animaexinani.engine.ui;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.point.PointF;

/**
 * A UI component that allows the user to input text.
 */
public class UITextField extends UIComponent {
    private final Text textDisplay;
    private String text = "";
    private Color backgroundColor = Color.BLACK;

    /**
     * Creates a new UITextField.
     * @param textDisplay the text object used to render the content
     */
    public UITextField(Text textDisplay) {
        this.textDisplay = textDisplay;
    }

    @Override
    public void render(Renderer renderer) {
        if (!this.visible()) return;

        // Draw background
        PointF[] points = {
            new PointF(this.position().x(), this.position().y()),
            new PointF(this.position().x() + this.size().width(), this.position().y()),
            new PointF(this.position().x() + this.size().width(), this.position().y() + this.size().height()),
            new PointF(this.position().x(), this.position().y() + this.size().height())
        };
        
        ConvexPolygon background = new ConvexPolygon(points, this.backgroundColor);
        renderer.draw(background);

        this.textDisplay.text(this.text + (this.focused() ? "|" : ""));
        this.textDisplay.origin(TextOrigin.CENTER_LEFT);
        this.textDisplay.translation(new PointF(this.position().x() + 5, this.position().y() + this.size().height() / 2.0f));
        renderer.draw(this.textDisplay);
    }

    /**
     * Handles text input events.
     * @param input the input text
     */
    public void handleTextInput(String input) {
        if (!this.focused()) return;
        this.text += input;
    }

    /**
     * Handles key events for text input (control keys).
     * @param event the key event to handle
     */
    public void handleKeyEvent(KeyEvent event) {
        if (!this.focused() || event.action() == KeyEvent.Action.RELEASE) return;

        // Scancode 42 is Backspace in SDL
        int scancode = event.scancode();
        if (scancode == 42) {
            if (!this.text.isEmpty()) {
                this.text = this.text.substring(0, this.text.length() - 1);
            }
        }
    }


    public String text() { return this.text; }
    public void text(String text) { this.text = text; }
}
