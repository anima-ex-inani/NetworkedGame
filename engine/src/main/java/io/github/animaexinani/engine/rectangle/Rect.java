package io.github.animaexinani.engine.rectangle;

import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.size.Size;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Rect(int left, int top, int width, int height) {
    public @NotNull Point topLeft() {
        return new Point(this.left, this.top);
    }

    public @NotNull Point topRight() {
        return new Point(this.left + this.width, this.top);
    }

    public @NotNull Point bottomLeft() {
        return new Point(this.left, this.top + this.height);
    }

    public @NotNull Point bottomRight() {
        return new Point(this.left + this.width, this.top + this.height);
    }

    public @NotNull Size size() {
        return new Size(this.width, this.height);
    }

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
