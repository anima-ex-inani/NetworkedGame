package io.github.animaexinani.engine.internal.audio;

import io.github.animaexinani.engine.audio.AudioPlayback;
import io.github.animaexinani.engine.audio.AudioSource;
import io.github.animaexinani.engine.audio.AudioSystem;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLInit;

import java.lang.ref.Cleaner;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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

    private static final int MINIMUM_CLEAN_THRESHOLD = 20; 

    private final Set<WeakReference<AudioPlayback>> playbackHandles;
    private final ReferenceQueue<AudioPlayback> playbackHandlesRefQueue;
    private int playbackHandlesCleanThreshold;

    @SuppressWarnings("unchecked")
    private void cleanPlaybackHandleReferences() {
        synchronized (this.playbackHandles) {
            var reference = (WeakReference<AudioPlayback>)this.playbackHandlesRefQueue.poll();
            if (reference == null) {
                return;
            }

            while (reference != null) {
                this.playbackHandles.remove(reference);
                reference = (WeakReference<AudioPlayback>)this.playbackHandlesRefQueue.poll();
            }

            this.playbackHandlesCleanThreshold = StrictMath.max(this.playbackHandles.size(), MINIMUM_CLEAN_THRESHOLD);
        }
    }

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

        var playback = new NativeAudioPlayback(stream);

        synchronized (this.playbackHandles) {
            this.playbackHandles.add(new WeakReference<AudioPlayback>(playback, this.playbackHandlesRefQueue));
            if (this.playbackHandles.size() >= this.playbackHandlesCleanThreshold) {
                this.cleanPlaybackHandleReferences();
            }
        }

        return playback;
    }

    /**
     * Initializes SDL's audio subsystem.
     */
    public NativeAudioSystem() {
        this.nativeState = new NativeState();
        this.cleanable = GlobalCleaner.register(this, this.nativeState);
        this.playbackHandles = Collections.synchronizedSet(new HashSet<>());
        this.playbackHandlesRefQueue = new ReferenceQueue<>();
        this.playbackHandlesCleanThreshold = MINIMUM_CLEAN_THRESHOLD;
    }

    /**
     * Releases the SDL audio subsystem owned by this instance.
     */
    @Override
    public void close() {
        if (this.nativeState.cleaned.getAcquire()) {
            return;
        }

        synchronized (this.playbackHandles) {
            for (var reference : this.playbackHandles) {
                var playback = reference.get();
                if (playback != null) {
                    try {
                        playback.close();
                    } catch (Exception _) {
                        // Intentionally ignored
                    }
                }
                this.playbackHandles.clear();
            }
        }

        this.cleanable.clean();
    }
}
