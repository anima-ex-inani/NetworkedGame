package io.github.animaexinani.engine.rendering.drawable;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

public class ConvexPolygon implements Drawable, Transformable {
    private @NotNull PointF translation = PointF.ZERO;
    private @NotNull SizeF scale = SizeF.ONE;
    private float rotation = 0.0F;
    private @NotNull PointF origin = PointF.ZERO;
    private @NotNull Color tint;

    private final PointF[] localCoords;
    private Vertex[] vertexCache = null;
    private final int[] indices;

    public ConvexPolygon(PointF[] localCoords, @NotNull Color tint) {
        this.localCoords = localCoords;
        this.tint = tint;

        // triangle fan algo: anchor to 0, connect adjacent points
        int numTriangles = localCoords.length - 2;
        this.indices = new int[numTriangles * 3];
        for (int i = 0; i < numTriangles; i++) {
            this.indices[i * 3]     = 0;
            this.indices[i * 3 + 1] = i + 1;
            this.indices[i * 3 + 2] = i + 2;
        }
    }

    @Override
    public @NotNull Vertex @NotNull [] vertices() {
        // 4. Null-check to see if we need to regenerate the vertices
        if (this.vertexCache == null) {
            // Instantiate the array since it was nullified
            this.vertexCache = new Vertex[this.localCoords.length];
            var currentTransform = this.transform();

            for (int i = 0; i < this.localCoords.length; i++) {
                PointF transformedPos = currentTransform.transform(this.localCoords[i]);
                
                // texture UV is Point.ZERO since this is a solid color polygon
                this.vertexCache[i] = new Vertex(transformedPos, new Point(0, 0), this.tint);
            }
        }

        return this.vertexCache;
    }

    @Override
    public int @NotNull [] indices() {
        return this.indices;
    }

    @Override
    public Texture texture() {
        return null;
    }

    // transformable implementation with dirty flags
    @Override public @NotNull PointF translation() { return this.translation; }
    @Override public void translation(@NotNull PointF translation) {
        Objects.requireNonNull(translation);
        if (!this.translation.equals(translation)) {
            this.translation = translation;
            this.vertexCache = null;
        }
    }

    @Override public float rotation() { return this.rotation; }
    @Override public void rotation(float rotation) {
        if (this.rotation != rotation) {
            this.rotation = rotation;
            this.vertexCache = null;
        }
    }

    @Override public @NotNull PointF pivot() { return this.origin; }
    @Override public void pivot(@NotNull PointF pivot) {
        Objects.requireNonNull(pivot);
        if (!this.origin.equals(pivot)) {
            this.origin = pivot;
            this.vertexCache = null;
        }
    }

    @Override public @NotNull SizeF scale() { return this.scale; }
    @Override public void scale(@NotNull SizeF scale) {
        Objects.requireNonNull(scale);
        if (!this.scale.equals(scale)) {
            this.scale = scale;
            this.vertexCache = null;
        }
    }
}