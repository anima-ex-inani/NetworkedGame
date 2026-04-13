package io.github.animaexinani.engine.size;

public record SizeF(float width, float height) {
    public static final SizeF ONE = new SizeF(1.0f, 1.0f);

    public SizeF {
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non-negative");
        }
    }
}
