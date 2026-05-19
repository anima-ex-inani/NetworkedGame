package io.github.animaexinani.engine.texture;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.Size;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a texture that has been loaded into GPU memory.
 */
public interface Texture extends AutoCloseable {
    /**
     * @return The size of the texture.
     */
    @NotNull
    Size getSize();

    /**
     * Gets the UV coordinates of the top-left corner of a pixel in the texture.
     * 
     * @param point The pixel to get the UV coordinates of.
     * @return The UV coordinates of the pixel.
     */
    @NotNull
    default PointF getUvOfPoint(@NotNull Point point) {
        var size = this.getSize();
        var u = (float) point.x() / (float) size.width();
        var v = (float) point.y() / (float) size.height();
        return new PointF(u, v);
    }

    /**
     * Sets the color modifier for this texture.
     * <p>
     * Note: This property is ignored when rendering the texture via {@code renderGeometry}.
     * </p>
     *
     * @param color The color modifier to set.
     */
    void setColorModifier(@NotNull Color color);

    /**
     * Gets the color modifier for this texture.
     *
     * @return The current color modifier.
     */
    @NotNull
    Color getColorModifier();
}
