package io.github.animaexinani.engine.vertex;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;

public record Vertex(PointF position, Point uv, Color color) {
}
