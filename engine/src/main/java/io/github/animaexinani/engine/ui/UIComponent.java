package io.github.animaexinani.engine.ui;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.rendering.Renderer;

/**
 * Base class for all UI components.
 * Handles position, size, and basic visibility.
 */
public abstract class UIComponent {
    private PointF position = new PointF(0, 0);
    private SizeF size = new SizeF(0, 0);
    private boolean visible = true;
    private boolean focused = false;

    /**
     * Renders the component.
     * @param renderer the renderer to use
     */
    public abstract void render(Renderer renderer);

    /**
     * Checks if a point is within the bounds of this component.
     * @param x the x-coordinate to check
     * @param y the y-coordinate to check
     * @return true if the point is within bounds, false otherwise
     */
    public boolean contains(float x, float y) {
        return x >= this.position.x() && x <= this.position.x() + this.size.width() &&
               y >= this.position.y() && y <= this.position.y() + this.size.height();
    }

    public PointF position() { return this.position; }
    public void position(PointF position) { this.position = position; }

    public SizeF size() { return this.size; }
    public void size(SizeF size) { this.size = size; }

    public boolean visible() { return this.visible; }
    public void visible(boolean visible) { this.visible = visible; }

    public boolean focused() { return this.focused; }
    public void focused(boolean focused) { this.focused = focused; }
}
