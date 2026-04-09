package io.github.animaexinani.engine.internal.video;

import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLRender;
import org.lwjgl.sdl.SDL_Texture;

import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

public class NativeTexture implements Texture {
    private static final class NativeState implements Runnable {
        @NotNull
        private final SDL_Texture texture;

        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLRender.SDL_DestroyTexture(texture);
            this.texture.close();
        }

        public NativeState(@NotNull SDL_Texture texture) {
            this.cleaned = new AtomicBoolean(false);
            this.texture = texture;
        }
    }

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;

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

    public NativeTexture(@NotNull SDL_Texture texture) {
        this.nativeState = new NativeState(texture);
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }
}
