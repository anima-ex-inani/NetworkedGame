package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.PixelFormat;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class ImageProcessor {
    public static @NotNull Image processData(@NotNull InputStream data) throws IOException {
        Objects.requireNonNull(data);

        BufferedImage image;
        image = ImageIO.read(data);

        var minX = image.getMinX();
        var minY = image.getMinY();
        var width = image.getWidth();
        var height = image.getHeight();
        var scanSize = StrictMath.multiplyExact(width, Integer.BYTES);

        var pixelData = image.getRGB(minX, minY, width, height, null, 0, scanSize);
        var pixelBuffer = ByteBuffer.allocateDirect(StrictMath.multiplyExact(scanSize, height));
        pixelBuffer.asIntBuffer().put(pixelData);

        var textureSize = new Size(width - minX, height - minY);

        var pixelFormat = PixelFormat.ARGB_8888;

        return new Image(pixelBuffer.asReadOnlyBuffer(), textureSize, pixelFormat);
    }

    private ImageProcessor() {}
}
