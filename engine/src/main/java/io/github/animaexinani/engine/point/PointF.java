package io.github.animaexinani.engine.point;

public record PointF(float x, float y) {
    public static final PointF ZERO = new PointF(0.0f, 0.0f);
}
