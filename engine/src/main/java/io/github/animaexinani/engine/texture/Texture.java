package io.github.animaexinani.engine.texture;

import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.Size;
import org.jetbrains.annotations.NotNull;

public interface Texture extends AutoCloseable {
    @NotNull
    Size getSize();

    @NotNull
    default PointF getUvOfPoint(@NotNull Point point) {
        var size = getSize();
        var u = (float)point.x() / (float)size.width();
        var v = (float)point.y() / (float)size.height();
        return new PointF(u, v);
    }
}
