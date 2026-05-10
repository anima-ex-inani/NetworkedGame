package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.audio.AudioSource;
import io.github.animaexinani.engine.audio.SampleFormat;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public final class MemoryAudioSource extends AudioSource {
    private final @NotNull ByteBuffer data;
    private final @NotNull SampleFormat sampleFormat;
    private final long sampleCount;
    private final int channelCount;
    private final int sampleRate;
    private boolean closed = false;

    public MemoryAudioSource(
        @NotNull ByteBuffer data,
        @NotNull SampleFormat sampleFormat,
        long sampleCount,
        int channelCount,
        int sampleRate
    ) {
        this.data = Objects.requireNonNull(data);
        this.sampleFormat = Objects.requireNonNull(sampleFormat);
        this.sampleCount = sampleCount;
        this.channelCount = channelCount;
        this.sampleRate = sampleRate;
    }

    @Override
    protected boolean closed() {
        return this.closed;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
    }

    @Override
    public @NotNull SampleFormat sampleFormat() {
        return this.sampleFormat;
    }

    @Override
    public long sampleCount() {
        return this.sampleCount;
    }

    @Override
    public int channelCount() {
        return this.channelCount;
    }

    @Override
    public int sampleRate() {
        return this.sampleRate;
    }

    private static final @NotNull ByteBuffer EMPTY_BUFFER = ByteBuffer.allocateDirect(0).asReadOnlyBuffer();

    @Override
    public @NotNull ByteBuffer getSamples(long offset, long sampleCount) throws IOException {
        if (this.closed) {
            throw new IOException("Audio source is closed");
        }

        if (offset < 0 || sampleCount <= 0) {
            return EMPTY_BUFFER;
        }

        var bytesPerFrame = (long) this.sampleFormat.bytes() * this.channelCount;
        var startByte = offset * bytesPerFrame;
        var endByte = (offset + sampleCount) * bytesPerFrame;

        if (startByte >= this.data.capacity()) {
            return EMPTY_BUFFER;
        }

        if (endByte > this.data.capacity()) {
            endByte = this.data.capacity();
        }

        var slice = this.data.slice((int) startByte, (int) (endByte - startByte));
        return slice.asReadOnlyBuffer();
    }
}
