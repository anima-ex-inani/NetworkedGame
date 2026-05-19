package io.github.animaexinani.engine.ui;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.font.Text;
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
        this.textDisplay.translation(new PointF(this.position().x() + 5, this.position().y() + this.size().height() / 2.0f));
        renderer.draw(this.textDisplay);
    }

    /**
     * Handles key events for text input.
     * @param event the key event to handle
     */
    public void handleKeyEvent(KeyEvent event) {
        if (!this.focused() || event.action() == KeyEvent.Action.RELEASE) return;

        // Very basic mapping for demo purposes. 
        // In a real engine, we'd use SDL_EVENT_TEXT_INPUT.
        // Scancodes for A-Z are roughly 4-29 in SDL2/3.
        int scancode = event.scancode();
        
        if (scancode >= 4 && scancode <= 29) { // A-Z
            char c = (char) ('a' + (scancode - 4));
            // Shift check would be needed here for uppercase
            this.text += c;
        } else if (scancode >= 30 && scancode <= 38) { // 1-9
            this.text += (char) ('1' + (scancode - 30));
        } else if (scancode == 39) { // 0
            this.text += '0';
        } else if (scancode == 42) { // Backspace
            if (!this.text.isEmpty()) {
                this.text = this.text.substring(0, this.text.length() - 1);
            }
        } else if (scancode == 44) { // Space
            this.text += " ";
        } else if (scancode == 55) { // Period
            this.text += ".";
        }
    }

    public String text() { return this.text; }
    public void text(String text) { this.text = text; }
}
