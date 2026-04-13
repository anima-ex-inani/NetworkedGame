package io.github.animaexinani.engine.rendering.transformable;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.transform.Transform;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for objects that can be transformed.
 * <p>
 * This interface provides methods for translation, rotation, and scaling.
 * </p>
 */
public interface Transformable {
    /**
     * Gets the translation of this object.
     *
     * @return The translation point.
     */
    @NotNull PointF translation();

    /**
     * Sets the translation of this object.
     *
     * @param translation The new translation point.
     */
    void translation(@NotNull PointF translation);

    /**
     * Gets the rotation of this object.
     *
     * @return The rotation in radians.
     */
    float rotation();

    /**
     * Sets the rotation of this object.
     *
     * @param rotation The new rotation in radians.
     */
    void rotation(float rotation);

    /**
     * Gets the pivot point of this object.
     *
     * @return The pivot point.
     */
    @NotNull PointF pivot();

    /**
     * Sets the pivot point of this object.
     *
     * @param pivot The new pivot point.
     */
    void pivot(@NotNull PointF pivot);

    /**
     * Gets the scale of this object.
     *
     * @return The scale size.
     */
    @NotNull SizeF scale();

    /**
     * Sets the scale of this object.
     *
     * @param scale The new scale size.
     */
    void scale(@NotNull SizeF scale);

    /**
     * Resets the transformations of this object to their default values.
     */
    default void reset() {
        this.translation(PointF.ZERO);
        this.rotation(0);
        this.scale(SizeF.ONE);
        this.pivot(PointF.ZERO);
    }

    /**
     * Gets the combined transformation of this object.
     *
     * @return The combined transformation.
     */
    default @NotNull Transform transform() {
        SizeF scale = this.scale();
        PointF translation = this.translation();
        float rotation = this.rotation();
        PointF pivot = this.pivot();

        return Transform.scale(scale.width(), scale.height(), pivot)
            .then(Transform.rotation(rotation, pivot))
            .then(Transform.translation(translation.x(), translation.y()));
    }
}
