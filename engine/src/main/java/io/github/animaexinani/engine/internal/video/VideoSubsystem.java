package io.github.animaexinani.engine.internal.video;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLVideo;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import io.github.animaexinani.engine.windowing.Window;
import io.github.animaexinani.engine.windowing.WindowCreationFailedException;
import io.github.animaexinani.engine.windowing.WindowFactory;
import io.github.animaexinani.engine.windowing.WindowOptions;

public final class VideoSubsystem implements AutoCloseable, WindowFactory {
    private static final class NativeState implements Runnable {
        private final AtomicBoolean cleaned;
        
        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_VIDEO);
        }

        public NativeState() {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_VIDEO)
            );
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;

    @Override
    public @NotNull Window createWindow(@NotNull WindowOptions options) {
        Objects.requireNonNull(options);

        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to create a window after the video subsystem has been closed");
        }

        try {
            var windowHandle = SdlOperationFailedException.throwOnFailure(
                SDLVideo.SDL_CreateWindow(
                    options.getTitle(),
                    options.getClientWidth(),
                    options.getClientHeight(),
                    options.getWindowFlags()
                )
            );

            return new NativeWindow(windowHandle);
        }
        catch (SdlOperationFailedException e) {
            throw new WindowCreationFailedException("Failed to create window", e);
        }
    }

    public VideoSubsystem() {
        this.nativeState = new NativeState();
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }

    @Override
    public void close() {
        this.cleanable.clean();
    }
}
