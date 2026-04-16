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
    private final @NotNull AudioStream stream;

    /**
     * Gets the sample format of the underlying audio stream.
     * @return The sample format.
     */
    protected SampleFormat streamSampleFormat() {
        return this.stream.sampleFormat();
    }

    protected @NotNull ByteBuffer fetchSamples(long sampleCount) throws IOException {
        long streamSampleCount = this.stream.sampleCount();
        if (this.sampleOffset + sampleCount > streamSampleCount && this.shouldLoop) {
            long firstPartCount = streamSampleCount - this.sampleOffset;
            long secondPartCount = sampleCount - firstPartCount;

            ByteBuffer firstPartBuffer = this.stream.getSamples(this.sampleOffset, firstPartCount);
            ByteBuffer secondPartBuffer = this.stream.getSamples(0, secondPartCount);

            ByteBuffer totalBuffer = ByteBuffer.allocate(firstPartBuffer.remaining() + secondPartBuffer.remaining());
            totalBuffer.put(firstPartBuffer);
            totalBuffer.put(secondPartBuffer);
            totalBuffer.flip();

            this.sampleOffset = secondPartCount;
            return totalBuffer;
        }

        ByteBuffer sampleBuffer = this.stream.getSamples(this.sampleOffset, sampleCount);
        this.sampleOffset += sampleCount;
        return sampleBuffer;
    }

    private long sampleOffset = 0;

    protected long sampleOffset() {
        return this.sampleOffset;
    }

    protected void sampleOffset(long sampleOffset) {
        this.sampleOffset = sampleOffset;
    }

    private boolean shouldLoop = false;

    /**
     * Whether the playback should loop once the end of the audio stream is reached.
     * @return <code>true</code> if the playback should loop, <code>false</code> otherwise.
     */
    public boolean shouldLoop() {
        return this.shouldLoop;
    }

    /**
     * Sets whether the playback should loop once the end of the audio stream is reached.
     * @param value <code>true</code> if the playback should loop, <code>false</code> otherwise.
     */
    public void shouldLoop(boolean value) {
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
    protected AudioPlayback(@NotNull AudioStream stream) {
        Objects.requireNonNull(stream);

        this.stream = stream;
    }
}
