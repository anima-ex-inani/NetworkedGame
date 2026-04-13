package io.github.animaexinani.engine.rectangle;

import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.size.Size;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A record representing a rectangle with integer coordinates and dimensions.
 *
 * @param left   The x-coordinate of the left edge of the rectangle.
 * @param top    The y-coordinate of the top edge of the rectangle.
 * @param width  The width of the rectangle. Must be non-negative.
 * @param height The height of the rectangle. Must be non-negative.
 */
public record Rect(int left, int top, int width, int height) {
    /**
     * Gets the top-left corner of the rectangle.
     *
     * @return A {@link Point} representing the top-left corner.
     */
    @Contract("-> new")
    public @NotNull Point topLeft() {
        return new Point(this.left, this.top);
    }

    /**
     * Gets the top-right corner of the rectangle.
     *
     * @return A {@link Point} representing the top-right corner.
     */
    @Contract("-> new")
    public @NotNull Point topRight() {
        return new Point(this.left + this.width, this.top);
    }

    /**
     * Gets the bottom-left corner of the rectangle.
     *
     * @return A {@link Point} representing the bottom-left corner.
     */
    @Contract("-> new")
    public @NotNull Point bottomLeft() {
        return new Point(this.left, this.top + this.height);
    }

    /**
     * Gets the bottom-right corner of the rectangle.
     *
     * @return A {@link Point} representing the bottom-right corner.
     */
    @Contract("-> new")
    public @NotNull Point bottomRight() {
        return new Point(this.left + this.width, this.top + this.height);
    }

    /**
     * Gets the size of the rectangle.
     *
     * @return A {@link Size} representing the width and height of the rectangle.
     */
    @Contract("-> new")
    public @NotNull Size size() {
        return new Size(this.width, this.height);
    }

    /**
     * Creates a new rectangle from a top-left point and a size.
     *
     * @param topLeft The top-left corner of the rectangle.
     * @param size    The size of the rectangle.
     * @return A new {@link Rect} instance.
     * @throws NullPointerException if {@code topLeft} or {@code size} is null.
     */
    @Contract("_, _ -> new")
    public static @NotNull Rect of(@NotNull Point topLeft, @NotNull Size size) {
        Objects.requireNonNull(topLeft);
        Objects.requireNonNull(size);

        return new Rect(topLeft.x(), topLeft.y(), size.width(), size.height());
    }

    public Rect {
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non-negative");
        }
    }
}
