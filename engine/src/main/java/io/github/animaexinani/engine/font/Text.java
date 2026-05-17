package io.github.animaexinani.engine.font;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Text implements Drawable, Transformable {
    private @NotNull FontFace font;
    private @NotNull FontWeight fontWeight;
    private @NotNull FontStyle fontStyle;
    private float fontSize;
    private @NotNull Color color;
    private @NotNull String text;
    private @NotNull TextOrigin origin = TextOrigin.BASELINE_LEFT;

    private @NotNull PointF translation = PointF.ZERO;
    private @NotNull SizeF scale = SizeF.ONE;
    private float rotation = 0.0f;
    private @NotNull PointF pivot = PointF.ZERO;

    private boolean geometryDirty = true;
    private Font cachedFont;
    private Vertex[] vertexCache = new Vertex[0];

    public Text(@NotNull FontFace fontFace, @NotNull String text) {
        this.font = Objects.requireNonNull(fontFace);
        this.text = Objects.requireNonNull(text);
        this.fontWeight = FontWeight.NORMAL;
        this.fontStyle = FontStyle.NORMAL;
        this.fontSize = 16.0f;
        this.color = Color.WHITE;
    }

    @NotNull
    public FontFace font() {
        return this.font;
    }

    public void font(@NotNull FontFace font) {
        this.font = Objects.requireNonNull(font);
        this.cachedFont = null;
        this.geometryDirty = true;
    }

    @NotNull
    public FontWeight fontWeight() {
        return this.fontWeight;
    }

    public void fontWeight(@NotNull FontWeight fontWeight) {
        this.fontWeight = Objects.requireNonNull(fontWeight);
        this.cachedFont = null;
        this.geometryDirty = true;
    }

    @NotNull
    public FontStyle fontStyle() {
        return this.fontStyle;
    }

    public void fontStyle(@NotNull FontStyle fontStyle) {
        this.fontStyle = Objects.requireNonNull(fontStyle);
        this.cachedFont = null;
        this.geometryDirty = true;
    }

    public float fontSize() {
        return this.fontSize;
    }

    public void fontSize(float fontSize) {
        if (!Float.isFinite(fontSize)) {
            throw new IllegalArgumentException("fontSize must be a finite number");
        }
        if (fontSize < 0) {
            throw new IllegalArgumentException("Font size must be non-negative");
        }
        this.fontSize = fontSize;
        this.cachedFont = null;
        this.geometryDirty = true;
    }

    @NotNull
    public Color color() {
        return this.color;
    }

    public void color(@NotNull Color color) {
        this.color = Objects.requireNonNull(color);
        this.geometryDirty = true;
    }

    @NotNull
    public String text() {
        return this.text;
    }

    public void text(@NotNull String text) {
        this.text = Objects.requireNonNull(text);
        this.geometryDirty = true;
    }

    @NotNull
    public TextOrigin origin() {
        return this.origin;
    }

    public void origin(@NotNull TextOrigin origin) {
        this.origin = Objects.requireNonNull(origin);
        this.geometryDirty = true;
    }

    @Override
    public @NotNull PointF translation() {
        return this.translation;
    }

    @Override
    public void translation(@NotNull PointF translation) {
        this.translation = Objects.requireNonNull(translation);
        this.geometryDirty = true;
    }

    @Override
    public float rotation() {
        return this.rotation;
    }

    @Override
    public void rotation(float rotation) {
        this.rotation = rotation;
        this.geometryDirty = true;
    }

    @Override
    public @NotNull PointF pivot() {
        return this.pivot;
    }

    @Override
    public void pivot(@NotNull PointF pivot) {
        this.pivot = Objects.requireNonNull(pivot);
        this.geometryDirty = true;
    }

    @Override
    public @NotNull SizeF scale() {
        return this.scale;
    }

    @Override
    public void scale(@NotNull SizeF scale) {
        this.scale = Objects.requireNonNull(scale);
        this.geometryDirty = true;
    }

    @Override
    public int indexCount() {
        return this.text.length() * 6;
    }

    @Override
    public int indexAt(int index) {
        if (index < 0 || index >= this.indexCount()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds");
        }
        int quad = index / 6;
        int rem = index % 6;
        int offset = quad * 4;
        return switch (rem) {
            case 0 -> offset;
            case 1 -> offset + 1;
            case 2 -> offset + 2;
            case 3 -> offset + 1;
            case 4 -> offset + 2;
            case 5 -> offset + 3;
            default -> throw new IllegalStateException();
        };
    }

    @Override
    public int vertexCount() {
        this.ensureGeometryUpdated();
        return this.vertexCache.length;
    }

    @Override
    public @NotNull Vertex vertexAt(int index) {
        this.ensureGeometryUpdated();
        return this.vertexCache[index];
    }

    @Override
    public @Nullable Texture texture() {
        this.ensureGeometryUpdated();
        if (this.cachedFont != null) {
            return this.cachedFont.texture();
        }
        return null;
    }

    private void ensureGeometryUpdated() {
        if (!this.geometryDirty) {
            return;
        }

        if (this.cachedFont == null) {
            this.cachedFont = this.font.fontAt(this.fontSize, this.fontWeight, this.fontStyle);
        }

        var transform = this.transform();
        int charCount = this.text.length();
        if (this.vertexCache.length != charCount * 4) {
            this.vertexCache = new Vertex[charCount * 4];
        }

        float totalWidth = 0.0f;
        for (int i = 0; i < charCount; i++) {
            Glyph glyph = this.cachedFont.glyph(this.text.codePointAt(i));
            if (glyph != null) {
                totalWidth += glyph.advance();
            }
        }

        float offsetX = switch (this.origin) {
            case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT, BASELINE_LEFT -> 0.0f;
            case TOP_CENTER, CENTER, BOTTOM_CENTER, BASELINE_CENTER -> -totalWidth / 2.0f;
            case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT, BASELINE_RIGHT -> -totalWidth;
        };

        float ascender = this.cachedFont.ascender();
        float lineHeight = this.cachedFont.lineHeight();
        float descender = lineHeight - ascender; // Approximation

        float offsetY = switch (this.origin) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> ascender;
            case CENTER_LEFT, CENTER, CENTER_RIGHT -> ascender - (lineHeight / 2.0f);
            case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> -descender;
            case BASELINE_LEFT, BASELINE_CENTER, BASELINE_RIGHT -> 0.0f;
        };

        float currentX = offsetX;
        float currentY = offsetY;

        for (int i = 0; i < charCount; i++) {
            int codePoint = this.text.codePointAt(i);
            Glyph glyph = this.cachedFont.glyph(codePoint);

            if (glyph == null) {
                // If glyph is not loaded, just render nothing and don't advance,
                // or we could advance by a default space. We'll skip for now.
                for (int j = 0; j < 4; j++) {
                    this.vertexCache[i * 4 + j] = new Vertex(PointF.ZERO, new Point(0, 0), this.color);
                }
                continue;
            }

            float x0 = currentX + glyph.xOffset();
            float y0 = currentY + glyph.yOffset();
            float x1 = x0 + glyph.width();
            float y1 = y0 + glyph.height();

            PointF tl = transform.transform(new PointF(x0, y0));
            PointF tr = transform.transform(new PointF(x1, y0));
            PointF bl = transform.transform(new PointF(x0, y1));
            PointF br = transform.transform(new PointF(x1, y1));

            Point uvTL = glyph.textureRect().topLeft();
            Point uvTR = glyph.textureRect().topRight();
            Point uvBL = glyph.textureRect().bottomLeft();
            Point uvBR = glyph.textureRect().bottomRight();

            this.vertexCache[i * 4 + 0] = new Vertex(tl, uvTL, this.color);
            this.vertexCache[i * 4 + 1] = new Vertex(tr, uvTR, this.color);
            this.vertexCache[i * 4 + 2] = new Vertex(bl, uvBL, this.color);
            this.vertexCache[i * 4 + 3] = new Vertex(br, uvBR, this.color);

            currentX += glyph.advance();
        }

        this.geometryDirty = false;
    }
}
