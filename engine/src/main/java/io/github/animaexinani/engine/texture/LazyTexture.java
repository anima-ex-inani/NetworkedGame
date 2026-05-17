package io.github.animaexinani.engine.texture;

import io.github.animaexinani.engine.rendering.Renderer;
import org.jetbrains.annotations.NotNull;
import java.nio.ByteBuffer;
import io.github.animaexinani.engine.size.Size;

public abstract class LazyTexture implements Texture {
    private Texture nativeTexture;

    protected abstract ByteBuffer pixelBuffer();

    protected abstract Size textureSize();

    protected abstract PixelFormat pixelFormat();

    public @NotNull Texture getOrCreateNativeTexture(@NotNull Renderer renderer) {
        if (this.nativeTexture == null) {
            this.nativeTexture = renderer.createTexture(this.textureSize(), this.pixelFormat(), this.pixelBuffer());
        }
        return this.nativeTexture;
    }

    @Override
    public @NotNull Size getSize() {
        return this.textureSize();
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
