package io.github.animaexinani.engine.size;

public record Size(int width, int height) {
    public Size {
        if (width < 0) {
            throw new IllegalArgumentException("Width must be non-negative");
        }

        if (height < 0) {
            throw new IllegalArgumentException("Height must be non-negative");
        }
    }
}
