package io.github.animaexinani.engine.font;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Text implements Drawable, Transformable {
    private @NotNull FontFace font;
    private @NotNull FontWeight fontWeight;
    private @NotNull FontStyle fontStyle;
    private float fontSize;
    private @NotNull Color color;
    private @NotNull String text;

    @NotNull
    FontFace font() {
        return this.font;
    }

    void font(@NotNull FontFace font) {
        this.font = Objects.requireNonNull(font);
    }

    @NotNull
    FontWeight fontWeight() {
        return this.fontWeight;
    }

    void fontWeight(@NotNull FontWeight fontWeight) {
        this.fontWeight = Objects.requireNonNull(fontWeight);
    }

    @NotNull
    FontStyle fontStyle() {
        return this.fontStyle;
    }

    void fontStyle(@NotNull FontStyle fontStyle) {
        this.fontStyle = Objects.requireNonNull(fontStyle);
    }

    float fontSize() {
        return this.fontSize;
    }

    void fontSize(float fontSize) {
        if (!Float.isFinite(fontSize)) {
            throw new IllegalArgumentException("fontSize must be a finite number");
        }

        this.fontSize = fontSize;
    }

    @NotNull
    Color color() {
        return this.color;
    }

    void color(@NotNull Color color) {
        this.color = Objects.requireNonNull(color);
    }

    @NotNull
    String text() {
        return this.text;
    }

    void text(@NotNull String text) {
        this.text = Objects.requireNonNull(text);
    }

    @Override
    public @NotNull PointF translation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'translation'");
    }

    @Override
    public void translation(@NotNull PointF translation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'translation'");
    }

    @Override
    public float rotation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotation'");
    }

    @Override
    public void rotation(float rotation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotation'");
    }

    @Override
    public @NotNull PointF pivot() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pivot'");
    }

    @Override
    public void pivot(@NotNull PointF pivot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pivot'");
    }

    @Override
    public @NotNull SizeF scale() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scale'");
    }

    @Override
    public void scale(@NotNull SizeF scale) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scale'");
    }

    @Override
    public int indexCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'indexCount'");
    }

    @Override
    public int indexAt(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'indexAt'");
    }

    @Override
    public int vertexCount() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'vertexCount'");
    }

    @Override
    public @NotNull Vertex vertexAt(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'vertexAt'");
    }

    @Override
    public @Nullable Texture texture() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'texture'");
    }
}
