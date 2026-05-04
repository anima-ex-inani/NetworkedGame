package io.github.animaexinani.engine.audio;

import io.github.animaexinani.engine.assets.Asset;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A source of audio samples.
 */
public abstract class AudioSource implements Closeable, Asset {
    /**
     * Checks if the stream is closed.
     * @return <code>true</code> if the stream is closed, <code>false</code> otherwise.
     */
    protected abstract boolean closed();

    @Override
    public boolean isValid() {
        return !this.closed();
    }

    /**
     * The sample format of the audio stream.
     * @return The sample format.
     */
    public abstract SampleFormat sampleFormat();

    /**
     * The number of samples in the stream.
     * @return The number of samples.
     */
    public abstract long sampleCount();

    /**
     * The number of channels in the stream.
     * @return The number of channels.
     */
    public abstract int channelCount();

    /**
     * The sample rate of the stream.
     * @return The sample rate.
     */
    public abstract int sampleRate();

    /**
     * Retrieves a buffer of audio samples from the stream.
     *
     * @param offset      The offset of the first sample to retrieve.
     * @param sampleCount The number of samples to retrieve.
     * @return A buffer containing the requested samples.
     * @throws IOException if an I/O error occurs while reading the samples.
     *
     * @implSpec The returned buffer should remain valid until either the next call to this method or the stream is closed.
     * <p>
     * This should return interleaved audio data. If the source contains planar audio data, it should be converted
     * before this function returns said data.
     * </p>
     * <p>
     * Once this method is called, the audio system will no longer use any previously returned buffers, so reusing the
     * same buffer with contents modified to contain the newly requested samples is allowed.
     * </p>
     * <p>
     * If the combination of {@code offset} and {@code sampleCount} would go beyond the end of the stream, the returned
     * buffer should end at the end of the stream. No padding should be added to the buffer to fill the gap.
     * </p>
     */
    public abstract @NotNull ByteBuffer getSamples(long offset, long sampleCount) throws IOException;
}
