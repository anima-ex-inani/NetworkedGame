package io.github.animaexinani.engine.audio;

import io.github.animaexinani.engine.assets.Asset;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A stream of audio samples.
 */
public abstract class AudioStream implements Closeable, Asset {
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
     *
     * @return The sample format.
     */
    public abstract SampleFormat sampleFormat();

    /**
     * Retrieves a buffer of audio samples from the stream.
     *
     * @param offset      The offset of the first sample to retrieve.
     * @param sampleCount The number of samples to retrieve.
     * @return A buffer containing the requested samples.
     * @throws IOException if an I/O error occurs while reading the samples.
     * @implSpec The returned buffer should remain valid until either the next call to this method or the stream is closed.
     * <p>
     * Once this method is called, the audio system will no longer use any previously returned buffers, so reusing the
     * same buffer with contents modified to contain the newly requested samples is allowed.
     * </p>
     */
    public abstract @NotNull ByteBuffer getSamples(int offset, int sampleCount) throws IOException;
}
