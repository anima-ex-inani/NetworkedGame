package io.github.animaexinani.engine.transform;

import io.github.animaexinani.engine.point.PointF;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A record representing a 2D affine transformation matrix.
 * <p>
 * The matrix is stored in row-major order:
 * </p>
 * <pre>
 * | m00 m01 m02 |
 * | m10 m11 m12 |
 * |  0   0   1  |
 * </pre>
 *
 * @param m00 The value at row 0, column 0 (horizontal scale/rotation).
 * @param m01 The value at row 0, column 1 (horizontal shear/rotation).
 * @param m02 The value at row 0, column 2 (horizontal translation).
 * @param m10 The value at row 1, column 0 (vertical shear/rotation).
 * @param m11 The value at row 1, column 1 (vertical scale/rotation).
 * @param m12 The value at row 1, column 2 (vertical translation).
 */
public record Transform(
    float m00, float m01, float m02,
    float m10, float m11, float m12
) {
    /**
     * The identity transformation.
     */
    public static final @NotNull Transform IDENTITY = new Transform(
        1.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f
    );

    /**
     * Creates a new transform that translates the object by the given amount.
     * @param x The amount to translate the object in the x-axis.
     * @param y The amount to translate the object in the y-axis.
     * @return The new transform.
     */
    @Contract("_, _ -> new")
    public static @NotNull Transform translation(float x, float y) {
        return new Transform(
            1, 0, x,
            0, 1, y
        );
    }

    /**
     * Creates a new transform that rotates the object by the given angle.
     * @param angle The angle to rotate the object by, in radians.
     * @return The new transform.
     */
    @Contract("_ -> new")
    public static @NotNull Transform rotation(float angle) {
        float cos = (float) StrictMath.cos(angle);
        float sin = (float) StrictMath.sin(angle);
        return new Transform(
            cos, -sin, 0,
            sin, cos, 0
        );
    }

    /**
     * Creates a new transform that rotates the object around the given pivot point by the given angle.
     * @param angle The angle to rotate the object by, in radians.
     * @param pivot The pivot point around which to rotate the object.
     * @return The new transform.
     *
     * @apiNote
     * This is equivalent to the following code:
     *
     * <pre>{@code
     * Transform.translation(-pivot.x(), -pivot.y())
     *     .then(Transform.rotation(angle))
     *     .then(Transform.translation(pivot.x(), pivot.y()));
     * }</pre>
     */
    public static @NotNull Transform rotation(float angle, @NotNull PointF pivot) {
        Objects.requireNonNull(pivot);

        float cos = (float) StrictMath.cos(angle);
        float sin = (float) StrictMath.sin(angle);

        float px = pivot.x();
        float py = pivot.y();

        float omc = 1 - cos;

        return new Transform(
            cos, -sin, px * omc + py * sin,
            sin, cos, -px * sin + py * omc
        );
    }

    /**
     * Creates a new transform that scales the object by the given factors.
     * @param x The factor to scale the object in the x-axis.
     * @param y The factor to scale the object in the y-axis.
     * @return The new transform.
     */
    @Contract("_, _ -> new")
    public static @NotNull Transform scale(float x, float y) {
        return new Transform(
            x, 0, 0,
            0, y, 0
        );
    }

    /**
     * Creates a new transform that scales the object by the given factors around a pivot point.
     *
     * @param x     The factor to scale the object in the x-axis.
     * @param y     The factor to scale the object in the y-axis.
     * @param pivot The pivot point around which to scale the object.
     * @return The new transform.
     *
     * @apiNote
     * This is equivalent to the following code:
     *
     * <pre>{@code
     * Transform.translation(-pivot.x(), -pivot.y())
     *     .then(Transform.scale(x, y))
     *     .then(Transform.translation(pivot.x(), pivot.y()));
     * }</pre>
     */
    @Contract("_, _, _ -> new")
    public static @NotNull Transform scale(float x, float y, @NotNull PointF pivot) {
        Objects.requireNonNull(pivot);

        float px = pivot.x();
        float py = pivot.y();

        return new Transform(
            x, 0, px * (1 - x),
            0, y, py * (1 - y)
        );
    }

    /**
     * Creates a new transform that represents the result of applying this transform followed by the other transform.
     * @param other The transform to apply after this transform.
     * @return The resulting transform.
     */
    @Contract("_ -> new")
    public @NotNull Transform then(@NotNull Transform other) {
        Objects.requireNonNull(other);
        return new Transform(
            other.m00 * this.m00 + other.m01 * this.m10,
            other.m00 * this.m01 + other.m01 * this.m11,
            other.m00 * this.m02 + other.m01 * this.m12 + other.m02,
            other.m10 * this.m00 + other.m11 * this.m10,
            other.m10 * this.m01 + other.m11 * this.m11,
            other.m10 * this.m02 + other.m11 * this.m12 + other.m12
        );
    }

    /**
     * Transforms the given point using this transformation matrix.
     *
     * @param point The point to transform.
     * @return The transformed point.
     * @throws NullPointerException if {@code point} is null.
     */
    @Contract("_ -> new")
    public @NotNull PointF transform(@NotNull PointF point) {
        Objects.requireNonNull(point);

        return new PointF(
            this.m00 * point.x() + this.m01 * point.y() + this.m02,
            this.m10 * point.x() + this.m11 * point.y() + this.m12
        );
    }
}
