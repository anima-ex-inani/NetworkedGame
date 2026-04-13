package io.github.animaexinani.engine.rendering.transformable;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.transform.Transform;
import org.jetbrains.annotations.NotNull;

public interface Transformable {
    @NotNull PointF translation();

    void translation(@NotNull PointF translation);

    float rotation();

    void rotation(float rotation);

    PointF pivot();

    void pivot(@NotNull PointF pivot);

    @NotNull SizeF scale();

    void scale(@NotNull SizeF scale);

    default void reset() {
        this.translation(PointF.ZERO);
        this.rotation(0);
        this.scale(SizeF.ONE);
        this.pivot(PointF.ZERO);
    }

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
