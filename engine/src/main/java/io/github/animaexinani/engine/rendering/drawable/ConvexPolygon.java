package io.github.animaexinani.engine.rendering.drawable;

import org.dyn4j.geometry.Vector2;
import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

public class ConvexPolygon implements Drawable, Transformable {
    private final Geometry geometry;
    
    // backing fields for the Transformable interface
    private PointF translation = new PointF(0, 0);
    private PointF pivot = new PointF(0, 0);
    private SizeF scale = new SizeF(1, 1);
    private float rotation = 0f;

    public ConvexPolygon(Vector2[] localCoords, Color color) {
        int numVertices = localCoords.length;
        Vertex[] vertices = new Vertex[numVertices];

        for (int i = 0; i < numVertices; i++) {
            float px = (float) localCoords[i].x;
            float py = (float) localCoords[i].y;
            vertices[i] = new Vertex(new PointF(px, py), new Point(0, 0), color);
        }

        int numTriangles = numVertices - 2;
        int[] indices = new int[numTriangles * 3];

        for (int i = 0; i < numTriangles; i++) {
            indices[i * 3]     = 0;         
            indices[i * 3 + 1] = i + 1;     
            indices[i * 3 + 2] = i + 2;     
        }

        this.geometry = new Geometry(vertices, indices, null);
    }

    // transformable implementation
    @Override public @NotNull PointF translation() { return this.translation; }
    @Override public void translation(@NotNull PointF translation) { this.translation = translation; }

    @Override public float rotation() { return this.rotation; }
    @Override public void rotation(float rotation) { this.rotation = rotation; }

    @Override public @NotNull PointF pivot() { return this.pivot; }
    @Override public void pivot(@NotNull PointF pivot) { this.pivot = pivot; }

    @Override public @NotNull SizeF scale() { return this.scale; }
    @Override public void scale(@NotNull SizeF scale) { this.scale = scale; }

    // drawable Implementation
    @Override public Vertex[] vertices() { return this.geometry.vertices(); }
    @Override public int[] indices() { return this.geometry.indices(); }
    @Override public Texture texture() { return this.geometry.texture(); }
}