package io.github.animaexinani.engine.rectangle;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A record representing a rectangle with floating-point coordinates and dimensions.
 *
 * @param left   The x-coordinate of the left edge of the rectangle.
 * @param top    The y-coordinate of the top edge of the rectangle.
 * @param width  The width of the rectangle. Must be non-negative.
 * @param height The height of the rectangle. Must be non-negative.
 */
public record RectF(float left, float top, float width, float height) {
    /**
     * Gets the top-left corner of the rectangle.
     *
     * @return A {@link PointF} representing the top-left corner.
     */
    @Contract("-> new")
    public @NotNull PointF topLeft() {
        return new PointF(this.left, this.top);
    }

    /**
     * Gets the top-right corner of the rectangle.
     *
     * @return A {@link PointF} representing the top-right corner.
     */
    @Contract("-> new")
    public @NotNull PointF topRight() {
        return new PointF(this.left + this.width, this.top);
    }

    /**
     * Gets the bottom-left corner of the rectangle.
     *
     * @return A {@link PointF} representing the bottom-left corner.
     */
    @Contract("-> new")
    public @NotNull PointF bottomLeft() {
        return new PointF(this.left, this.top + this.height);
    }

    /**
     * Gets the bottom-right corner of the rectangle.
     *
     * @return A {@link PointF} representing the bottom-right corner.
     */
    @Contract("-> new")
    public @NotNull PointF bottomRight() {
        return new PointF(this.left + this.width, this.top + this.height);
    }

    /**
     * Gets the size of the rectangle.
     *
     * @return A {@link SizeF} representing the width and height of the rectangle.
     */
    @Contract("-> new")
    public @NotNull SizeF size() {
        return new SizeF(this.width, this.height);
    }

    /**
     * Creates a new rectangle from a top-left point and a size.
     *
     * @param topLeft The top-left corner of the rectangle.
     * @param size    The size of the rectangle.
     * @return A new {@link RectF} instance.
     * @throws NullPointerException if {@code topLeft} or {@code size} is null.
     */
    @Contract("_, _ -> new")
    public static @NotNull RectF of(@NotNull PointF topLeft, @NotNull SizeF size) {
        Objects.requireNonNull(topLeft);
        Objects.requireNonNull(size);

        return new RectF(topLeft.x(), topLeft.y(), size.width(), size.height());
    }

    public RectF {
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non-negative");
        }
    }
}
