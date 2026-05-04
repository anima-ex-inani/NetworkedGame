package io.github.animaexinani.engine.rendering;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.PixelFormat;
import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public interface Renderer extends AutoCloseable {
    /**
     * Creates a new texture from the given pixel buffer.
     * @param textureSize The size of the texture to create
     * @param pixelFormat The format of the pixel data in the buffer
     * @param pixelBuffer The pixel buffer containing the texture data
     * @return A new texture
     */
    @Contract("_, _, _ -> new")
    Texture createTexture(@NotNull Size textureSize, PixelFormat pixelFormat, @NotNull ByteBuffer pixelBuffer);

    void clear(Color color);

    void draw(@NotNull Drawable drawable);

    void present();
}
