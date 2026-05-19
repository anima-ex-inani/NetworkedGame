package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rectangle.Rect;
import io.github.animaexinani.engine.rendering.RenderContext;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A class representing a drawable sprite that can be transformed.
 * <p>
 * A sprite consists of a texture and a region of that texture to display.
 * </p>
 */
public class Sprite implements Drawable, Transformable {
    private @NotNull PointF translation;

    private @NotNull SizeF scale;

    private float rotation;

    private @NotNull PointF origin;

    private @NotNull Color tint;

    private @NotNull Texture texture;

    private @NotNull Rect textureRect;

    /**
     * Creates a new sprite with the given texture and texture region.
     *
     * @param texture     The texture to use for the sprite.
     * @param textureRect The region of the texture to display.
     * @throws NullPointerException if {@code texture} or {@code textureRect} is
     *                              null.
     */
    public Sprite(@NotNull Texture texture, @NotNull Rect textureRect) {
        Objects.requireNonNull(texture);
        Objects.requireNonNull(textureRect);

        this.translation = PointF.ZERO;
        this.scale = SizeF.ONE;
        this.rotation = 0.0F;
        this.origin = new PointF(textureRect.width() / 2.0F, textureRect.height() / 2.0F);
        this.tint = Color.WHITE;
        this.texture = texture;
        this.textureRect = textureRect;
    }

    // --- Drawable ---

    @Override
    public void draw(@NotNull RenderContext context) {
        var oldColor = this.texture.getColorModifier();
        this.texture.setColorModifier(this.tint);
        try {
            var transform = this.transform();
            var originPos = transform.transform(PointF.ZERO);
            var rightPos = transform.transform(new PointF(this.textureRect.width(), 0.0f));
            var downPos = transform.transform(new PointF(0.0f, this.textureRect.height()));

            context.drawTextureAffine(this.texture, this.textureRect, originPos, rightPos, downPos);
        } finally {
            this.texture.setColorModifier(oldColor);
        }
    }

    /**
     * Gets the texture used by this sprite.
     *
     * @return The texture.
     */
    public @NotNull Texture texture() {
        return this.texture;
    }

    /**
     * Sets the texture used by this sprite.
     *
     * @param texture The new texture.
     * @throws NullPointerException if {@code texture} is null.
     */
    public void texture(@NotNull Texture texture) {
        Objects.requireNonNull(texture);

        this.texture = texture;
    }

    /**
     * Gets the texture region displayed by this sprite.
     *
     * @return The texture region.
     */
    public Rect textureRect() {
        return this.textureRect;
    }

    /**
     * Sets the texture region displayed by this sprite.
     *
     * @param textureRect The new texture region.
     * @throws NullPointerException if {@code textureRect} is null.
     */
    public void textureRect(@NotNull Rect textureRect) {
        Objects.requireNonNull(textureRect);

        if (!this.textureRect.equals(textureRect)) {
            var px = this.origin.x() / this.textureRect.width();
            var py = this.origin.y() / this.textureRect.height();

            this.textureRect = textureRect;
            this.origin = new PointF(px * this.textureRect.width(), py * this.textureRect.height());
        }
    }

    @Override
    public @NotNull PointF translation() {
        return this.translation;
    }

    @Override
    public void translation(@NotNull PointF translation) {
        Objects.requireNonNull(translation);

        if (!this.translation.equals(translation)) {
            this.translation = translation;
        }
    }

    @Override
    public float rotation() {
        return this.rotation;
    }

    @Override
    public void rotation(float rotation) {
        if (this.rotation != rotation) {
            this.rotation = rotation;
        }
    }

    @Override
    public @NotNull PointF pivot() {
        return this.origin;
    }

    @Override
    public void pivot(@NotNull PointF pivot) {
        Objects.requireNonNull(pivot);

        if (!this.origin.equals(pivot)) {
            this.origin = pivot;
        }
    }

    @Override
    public @NotNull SizeF scale() {
        return this.scale;
    }

    @Override
    public void scale(@NotNull SizeF scale) {
        Objects.requireNonNull(scale);

        if (!this.scale.equals(scale)) {
            this.scale = scale;
        }
    }

    @Override
    public void reset() {
        Transformable.super.reset();
        this.origin = new PointF(this.textureRect.width() / 2.0F, this.textureRect.height() / 2.0F);
    }

    /**
     * Gets the tint color of this sprite.
     *
     * @return The tint color.
     */
    public Color tint() {
        return this.tint;
    }

    /**
     * Sets the tint color of this sprite.
     *
     * @param tint The new tint color.
     * @throws NullPointerException if {@code tint} is null.
     */
    public void tint(@NotNull Color tint) {
        Objects.requireNonNull(tint);

        if (!this.tint.equals(tint)) {
            this.tint = tint;
        }
    }
}
