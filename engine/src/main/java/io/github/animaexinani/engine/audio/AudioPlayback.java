package io.github.animaexinani.engine.audio;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

/**
 * A playback of an audio stream.
 *
 * @implSpec
 * Closing the playback should unbind the audio stream from the audio system.
 */
public abstract class AudioPlayback implements AutoCloseable {
    private final @NotNull AudioSource stream;

    /**
     * Gets the sample format of the underlying audio stream.
     * @return The sample format.
     */
    protected SampleFormat streamSampleFormat() {
        return this.stream.sampleFormat();
    }

    protected int streamChannelCount() {
        return this.stream.channelCount();
    }

    protected long streamSampleCount() {
        return this.stream.sampleCount();
    }

    private static final @NotNull ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

    protected synchronized @NotNull ByteBuffer fetchSamples(long sampleCount) throws IOException {
        if (sampleCount <= 0) {
            return EMPTY_BUFFER;
        }

        long streamSampleCount = this.stream.sampleCount();
        if (streamSampleCount <= 0) {
            return EMPTY_BUFFER;
        }

        if (this.shouldLoop) {
            this.sampleOffset %= streamSampleCount;
            if (this.sampleOffset < 0) {
                this.sampleOffset += streamSampleCount;
            }
        }

        if (this.shouldLoop && (this.sampleOffset + sampleCount > streamSampleCount)) {
            long firstPartCount = streamSampleCount - this.sampleOffset;
            long secondPartCount = sampleCount - firstPartCount;

            var bytesPerFrame = this.streamSampleFormat().bytes() * this.streamChannelCount();
            ByteBuffer totalBuffer = ByteBuffer.allocate((int) StrictMath.multiplyExact(sampleCount, bytesPerFrame));

            if (firstPartCount > 0) {
                ByteBuffer firstPartBuffer = this.stream.getSamples(this.sampleOffset, firstPartCount);
                totalBuffer.put(firstPartBuffer);
            }

            long remainingToLoad = secondPartCount;
            while (remainingToLoad > 0) {
                long loadNow = Math.min(remainingToLoad, streamSampleCount);
                ByteBuffer partBuffer = this.stream.getSamples(0, loadNow);
                totalBuffer.put(partBuffer);
                remainingToLoad -= loadNow;
            }

            totalBuffer.flip();

            this.sampleOffset = secondPartCount % streamSampleCount;
            return totalBuffer;
        }

        // If not looping, we should still ensure we don't go out of bounds
        if (!this.shouldLoop && this.sampleOffset >= streamSampleCount) {
            return EMPTY_BUFFER;
        }

        long actualLoad = sampleCount;
        if (!this.shouldLoop && (this.sampleOffset + sampleCount > streamSampleCount)) {
            actualLoad = streamSampleCount - this.sampleOffset;
        }

        ByteBuffer sampleBuffer = this.stream.getSamples(this.sampleOffset, actualLoad);
        this.sampleOffset += actualLoad;
        return sampleBuffer;
    }

    private long sampleOffset = 0;

    protected synchronized long sampleOffset() {
        return this.sampleOffset;
    }

    protected synchronized void sampleOffset(long sampleOffset) {
        this.sampleOffset = sampleOffset;
    }

    private boolean shouldLoop = false;

    /**
     * Whether the playback should loop once the end of the audio stream is reached.
     * @return <code>true</code> if the playback should loop, <code>false</code> otherwise.
     */
    public synchronized boolean shouldLoop() {
        return this.shouldLoop;
    }

    /**
     * Sets whether the playback should loop once the end of the audio stream is reached.
     * @param value <code>true</code> if the playback should loop, <code>false</code> otherwise.
     */
    public synchronized void shouldLoop(boolean value) {
        this.shouldLoop = value;
    }

    /**
     * Whether the playback is currently playing.
     * @return <code>true</code> if the playback is playing, <code>false</code> otherwise.
     */
    public abstract boolean isPlaying();

    /**
     * Plays the audio stream.
     */
    public abstract void play();

    /**
     * Pauses the audio stream.
     */
    public abstract void pause();

    /**
     * Moves the playback position to the given sample offset.
     * @param sampleOffset The sample offset to move to.
     * @return <code>true</code> if the playback position was successfully moved, <code>false</code> otherwise.
     */
    public abstract boolean seek(long sampleOffset);

    /**
     * Moves the playback position to the given duration.
     * @param position The duration to move to.
     * @return <code>true</code> if the playback position was successfully moved, <code>false</code> otherwise.
     */
    public abstract boolean seek(Duration position);

    /**
     * Creates a new playback of the given audio stream.
     * @param stream The audio stream to play.
     *
     * @apiNote
     * This does not take ownership of the audio stream. The caller remains responsible for closing it when no longer
     * needed.
     */
    protected AudioPlayback(@NotNull AudioSource stream) {
        Objects.requireNonNull(stream);

        this.stream = stream;
    }
}
