package io.github.animaexinani.engine.internal.video;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLRender;
import org.lwjgl.sdl.SDL_Texture;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class NativeTexture implements Texture {
    private static final class NativeState implements Runnable {
        @NotNull
        private final SDL_Texture texture;

        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLRender.SDL_DestroyTexture(this.texture);
        }

        public NativeState(@NotNull SDL_Texture texture) {
            this.cleaned = new AtomicBoolean(false);
            this.texture = texture;
        }
    }

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;
    private @NotNull Color colorModifier = Color.WHITE;

    @Override
    public void close() {
        this.cleanable.clean();
    }

    public SDL_Texture getBackingTexture() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to get backing texture of a closed texture");
        }

        return this.nativeState.texture;
    }

    @Override
    @NotNull
    public Size getSize() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to get size of a closed texture");
        }

        return new Size(this.nativeState.texture.w(), this.nativeState.texture.h());
    }

    @Override
    public void setColorModifier(@NotNull Color color) {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to set color modifier of a closed texture");
        }

        Objects.requireNonNull(color);

        try {
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_SetTextureColorModFloat(this.nativeState.texture, color.red(), color.green(), color.blue())
            );
            SdlOperationFailedException.throwOnFailure(
                    SDLRender.SDL_SetTextureAlphaModFloat(this.nativeState.texture, color.alpha())
            );
            this.colorModifier = color;
        } catch (SdlOperationFailedException e) {
            // We could log this or throw a runtime exception, but usually these should succeed if handle is valid
            throw new RuntimeException("Failed to set texture color/alpha modulation", e);
        }
    }

    @Override
    public @NotNull Color getColorModifier() {
        return this.colorModifier;
    }

    public NativeTexture(@NotNull SDL_Texture texture) {
        this.nativeState = new NativeState(texture);
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }
}
