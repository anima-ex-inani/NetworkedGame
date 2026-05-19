package io.github.animaexinani.engine.ui;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.font.Text;
import io.github.animaexinani.engine.font.TextOrigin;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.point.PointF;

/**
 * A UI component that can be clicked to trigger an action.
 */
public class UIButton extends UIComponent {
    private final Text label;
    private final Runnable onClick;
    private Color backgroundColor = Color.GRAY;
    private Color hoverColor = Color.LIGHT_GRAY;
    private boolean isHovered = false;

    /**
     * Creates a new UIButton.
     * @param label the text to display on the button
     * @param onClick the action to perform when clicked
     */
    public UIButton(Text label, Runnable onClick) {
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    public void render(Renderer renderer) {
        if (!this.visible()) return;

        Color currentBackground = this.isHovered ? this.hoverColor : this.backgroundColor;
        
        // Simple rectangular background using ConvexPolygon
        PointF[] points = {
            new PointF(this.position().x(), this.position().y()),
            new PointF(this.position().x() + this.size().width(), this.position().y()),
            new PointF(this.position().x() + this.size().width(), this.position().y() + this.size().height()),
            new PointF(this.position().x(), this.position().y() + this.size().height())
        };
        ConvexPolygon background = new ConvexPolygon(points, currentBackground);
        renderer.draw(background);

        // Center the text
        this.label.origin(TextOrigin.CENTER);
        this.label.translation(new PointF(
            this.position().x() + this.size().width() / 2.0f,
            this.position().y() + this.size().height() / 2.0f
        ));
        renderer.draw(this.label);
    }

    /**
     * Handles a mouse down event.
     * @param x the x-coordinate of the click
     * @param y the y-coordinate of the click
     */
    public void handleMouseDown(float x, float y) {
        if (this.visible() && this.contains(x, y)) {
            this.onClick.run();
        }
    }

    /**
     * Handles a mouse move event.
     * @param x the x-coordinate of the mouse
     * @param y the y-coordinate of the mouse
     */
    public void handleMouseMove(float x, float y) {
        this.isHovered = this.visible() && this.contains(x, y);
    }

    public Color backgroundColor() { return this.backgroundColor; }
    public void backgroundColor(Color color) { this.backgroundColor = color; }

    public Color hoverColor() { return this.hoverColor; }
    public void hoverColor(Color color) { this.hoverColor = color; }
}
