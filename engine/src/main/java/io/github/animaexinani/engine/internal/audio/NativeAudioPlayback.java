package io.github.animaexinani.engine.internal.audio;

import io.github.animaexinani.engine.audio.AudioPlayback;
import io.github.animaexinani.engine.audio.AudioSource;
import io.github.animaexinani.engine.audio.SampleFormat;
import io.github.animaexinani.engine.internal.GlobalCleaner;
import io.github.animaexinani.engine.internal.SdlOperationFailedException;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDL_AudioSpec;
import org.lwjgl.sdl.SDL_AudioStreamCallback;
import org.lwjgl.sdl.SDLAudio;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SDL audio-stream backed implementation of {@link AudioPlayback}.
 *
 * <p>This class owns a single SDL audio stream and feeds it from the bound
 * {@link AudioSource} through a native callback.</p>
 */
public final class NativeAudioPlayback extends AudioPlayback {
    private static final class NativeState implements Runnable {
        private final long streamHandle;
        private final SDL_AudioStreamCallback feedCallback;
        private final WeakReference<NativeAudioPlayback> playbackRef;
        private final AtomicBoolean cleaned;

        private synchronized void feed(int additionalAmount) {
            if (additionalAmount <= 0) {
                return;
            }

            NativeAudioPlayback playback = this.playbackRef.get();
            if (playback == null || this.cleaned.getAcquire()) {
                return;
            }

            long requestedSamples = Math.ceilDiv(additionalAmount, playback.bytesPerFrame);
            try {
                var samples = playback.fetchSamples(requestedSamples);
                if (!samples.hasRemaining()) {
                    playback.pause();
                    return;
                }

                SdlOperationFailedException.throwOnFailure(
                    SDLAudio.SDL_PutAudioStreamData(this.streamHandle, samples)
                );
            } catch (IOException | RuntimeException _) {
                playback.pause();
            }
        }

        @Override
        public synchronized void run() {
            if (this.cleaned.getAndSet(true)) {
                return;
            }

            SDLAudio.SDL_DestroyAudioStream(this.streamHandle);
            this.feedCallback.close();
        }

        public NativeState(NativeAudioPlayback playback, @NotNull SDL_AudioSpec streamSpec) {
            this.playbackRef = new WeakReference<>(playback);
            this.cleaned = new AtomicBoolean(false);
            this.feedCallback = SDL_AudioStreamCallback.create((userdata, stream, additionalAmount, totalAmount) -> this.feed(additionalAmount));
            try {
                this.streamHandle = SdlOperationFailedException.throwOnFailure(
                    SDLAudio.SDL_OpenAudioDeviceStream(
                        SDLAudio.SDL_AUDIO_DEVICE_DEFAULT_PLAYBACK,
                        streamSpec,
                        this.feedCallback,
                        0L
                    )
                );
            }
            catch (SdlOperationFailedException e) {
                this.feedCallback.close();
                throw e;
            }
        }
    }

    private final NativeState nativeState;
    private final Cleaner.Cleanable cleanable;
    private final AtomicBoolean isPlaying;
    private final int sampleRate;
    private final int bytesPerFrame;

    /**
     * Checks whether this playback is currently resumed.
     *
     * @return {@code true} when playback is active, {@code false} otherwise.
     */
    @Override
    public boolean isPlaying() {
        return !this.nativeState.cleaned.getAcquire() && this.isPlaying.getAcquire();
    }

    /**
     * Resumes this playback's underlying SDL audio stream.
     */
    @Override
    public synchronized void play() {
        this.throwIfClosed();
        SdlOperationFailedException.throwOnFailure(
            SDLAudio.SDL_ResumeAudioStreamDevice(this.nativeState.streamHandle)
        );
        this.isPlaying.setRelease(true);
    }

    /**
     * Pauses this playback's underlying SDL audio stream.
     */
    @Override
    public synchronized void pause() {
        if (this.nativeState.cleaned.getAcquire()) {
            return;
        }

        SdlOperationFailedException.throwOnFailure(
            SDLAudio.SDL_PauseAudioStreamDevice(this.nativeState.streamHandle)
        );
        this.isPlaying.setRelease(false);
    }

    /**
     * Seeks to an absolute sample offset in the source stream.
     *
     * @param sampleOffset The target sample offset.
     * @return {@code true} if the offset is valid and the seek was applied.
     */
    @Override
    public synchronized boolean seek(long sampleOffset) {
        this.throwIfClosed();
        if (sampleOffset < 0 || sampleOffset > this.streamSampleCount()) {
            return false;
        }

        this.sampleOffset(sampleOffset);
        SdlOperationFailedException.throwOnFailure(
            SDLAudio.SDL_FlushAudioStream(this.nativeState.streamHandle)
        );
        return true;
    }

    /**
     * Seeks to the sample position represented by the given duration.
     *
     * @param position Target position relative to start of stream.
     * @return {@code true} if the seek target is valid and was applied.
     */
    @Override
    public synchronized boolean seek(@NotNull Duration position) {
        Objects.requireNonNull(position);
        long targetSamples = Math.multiplyExact(position.toNanos(), this.sampleRate) / 1_000_000_000L;
        return this.seek(targetSamples);
    }

    /**
     * Creates playback for a source stream using SDL audio stream callbacks.
     *
     * @param stream The source stream to play.
     */
    public NativeAudioPlayback(@NotNull AudioSource stream) {
        super(stream);
        this.sampleRate = stream.sampleRate();
        this.bytesPerFrame = Math.multiplyExact(this.streamSampleFormat().bytes(), this.streamChannelCount());

        try (var streamSpec = SDL_AudioSpec.calloc()) {
            streamSpec.set(
                mapSampleFormat(this.streamSampleFormat()),
                this.streamChannelCount(),
                this.sampleRate
            );
            this.nativeState = new NativeState(this, streamSpec);
        }

        this.cleanable = GlobalCleaner.register(this, this.nativeState);
        this.isPlaying = new AtomicBoolean(false);
    }

    /**
     * Stops playback and releases native stream resources.
     */
    @Override
    public void close() {
        if (this.nativeState.cleaned.getAcquire()) {
            return;
        }

        this.pause();
        this.cleanable.clean();
    }

    private void throwIfClosed() {
        if (this.nativeState.cleaned.getAcquire()) {
            throw new IllegalStateException("Attempted to control a closed playback");
        }
    }

    private static int mapSampleFormat(@NotNull SampleFormat sampleFormat) {
        return switch (sampleFormat) {
            case U8 -> SDLAudio.SDL_AUDIO_U8;
            case S8 -> SDLAudio.SDL_AUDIO_S8;
            case S16LE -> SDLAudio.SDL_AUDIO_S16LE;
            case S16BE -> SDLAudio.SDL_AUDIO_S16BE;
            case S32LE -> SDLAudio.SDL_AUDIO_S32LE;
            case S32BE -> SDLAudio.SDL_AUDIO_S32BE;
            case F32LE -> SDLAudio.SDL_AUDIO_F32LE;
            case F32BE -> SDLAudio.SDL_AUDIO_F32BE;
        };
    }
}
