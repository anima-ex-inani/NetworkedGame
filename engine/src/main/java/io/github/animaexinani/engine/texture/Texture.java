package io.github.animaexinani.engine.texture;

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
}
