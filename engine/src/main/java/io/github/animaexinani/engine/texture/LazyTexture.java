package io.github.animaexinani.engine.texture;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.Renderer;
import org.jetbrains.annotations.NotNull;
import java.nio.ByteBuffer;
import java.util.Objects;

import io.github.animaexinani.engine.size.Size;

public abstract class LazyTexture implements Texture {
    private Texture nativeTexture;
    private @NotNull Color colorModifier = Color.WHITE;

    protected abstract ByteBuffer pixelBuffer();

    protected abstract Size textureSize();

    protected abstract PixelFormat pixelFormat();

    public @NotNull Texture getOrCreateNativeTexture(@NotNull Renderer renderer) {
        if (this.nativeTexture == null) {
            this.nativeTexture = renderer.createTexture(this.textureSize(), this.pixelFormat(), this.pixelBuffer());
            this.nativeTexture.setColorModifier(this.colorModifier);
        }
        return this.nativeTexture;
    }

    @Override
    public @NotNull Size getSize() {
        return this.textureSize();
    }

    @Override
    public void setColorModifier(@NotNull Color color) {
        this.colorModifier = Objects.requireNonNull(color);
        if (this.nativeTexture != null) {
            this.nativeTexture.setColorModifier(color);
        }
    }

    @Override
    public @NotNull Color getColorModifier() {
        return this.colorModifier;
    }

    @Override
    public void close() {
        if (this.nativeTexture != null) {
            try {
                this.nativeTexture.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
