package io.github.animaexinani.engine.internal.audio;

import io.github.animaexinani.engine.audio.AudioPlayback;
import io.github.animaexinani.engine.audio.AudioSource;
import io.github.animaexinani.engine.audio.AudioSystem;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLInit;

import java.lang.ref.Cleaner;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Native SDL-backed audio subsystem implementation.
 *
 * <p>This class owns initialization and shutdown of SDL's audio subsystem and
 * creates per-stream playback instances.</p>
 */
public final class NativeAudioSystem implements AudioSystem {
    private static final class NativeState implements Runnable {
        private final AtomicBoolean cleaned;

        @Override
        public void run() {
            this.cleaned.setRelease(true);
            SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_AUDIO);
        }

        public NativeState() {
            SdlOperationFailedException.throwOnFailure(
                SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_AUDIO)
            );
            this.cleaned = new AtomicBoolean(false);
        }
    }

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;

    /**
     * Creates a playback handle for the provided audio source.
     *
     * @param stream The source to bind.
     * @return A native playback wrapper for the source.
     */
    @Override
    public @NotNull AudioPlayback bindAudio(@NotNull AudioSource stream) {
        Objects.requireNonNull(stream);
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to bind audio after the audio subsystem has been closed");
        }

        return new NativeAudioPlayback(stream);
    }

    /**
     * Initializes SDL's audio subsystem.
     */
    public NativeAudioSystem() {
        this.nativeState = new NativeState();
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
    }

    /**
     * Releases the SDL audio subsystem owned by this instance.
     */
    @Override
    public void close() {
        this.cleanable.clean();
    }
}
