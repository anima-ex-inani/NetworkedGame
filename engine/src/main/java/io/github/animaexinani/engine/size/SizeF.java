package io.github.animaexinani.engine.size;

public record SizeF(float width, float height) {
    public SizeF {
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative");
        }
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non-negative");
        }
    }
}
