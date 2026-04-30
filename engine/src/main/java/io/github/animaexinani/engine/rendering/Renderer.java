package io.github.animaexinani.engine.rendering;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.PixelFormat;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.transform.Transform;

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

    /**
     * Draws a drawable object and applies an affine transformation matrix.
     * @param drawable  The visual geometry to draw
     * @param transform The transformation matrix (translation, rotation, scale)
     */
    void draw(@NotNull Drawable drawable, @NotNull Transform transform);
    void setTransform(@NotNull Transform transform);

    void present();
}
