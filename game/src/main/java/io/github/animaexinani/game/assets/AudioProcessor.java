package io.github.animaexinani.game.assets;

import io.github.animaexinani.engine.audio.SampleFormat;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public final class AudioProcessor {
    public static @NotNull MemoryAudioSource processData(@NotNull InputStream data) throws IOException {
        Objects.requireNonNull(data);

        try (var audioStream = AudioSystem.getAudioInputStream(data)) {
            var baseFormat = audioStream.getFormat();
            
            // We'll target S16LE for compatibility if it's not already something we easily support
            var targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false // Little endian
            );

            try (var decodedStream = AudioSystem.getAudioInputStream(targetFormat, audioStream)) {
                var format = decodedStream.getFormat();
                var sampleFormat = mapFormat(format);
                
                var bytes = decodedStream.readAllBytes();
                var buffer = ByteBuffer.allocateDirect(bytes.length);
                buffer.order(ByteOrder.nativeOrder());
                buffer.put(bytes);
                buffer.flip();

                var frameSize = format.getFrameSize();
                if (frameSize <= 0) {
                    throw new IOException("Invalid frame size: " + frameSize);
                }
                var sampleCount = bytes.length / (long) frameSize;

                return new MemoryAudioSource(
                    buffer,
                    sampleFormat,
                    sampleCount,
                    format.getChannels(),
                    (int) format.getSampleRate()
                );
            }
        } catch (UnsupportedAudioFileException e) {
            throw new IOException("Unsupported audio format", e);
        }
    }

    private static SampleFormat mapFormat(AudioFormat format) {
        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
            if (format.getSampleSizeInBits() == 16) {
                return format.isBigEndian() ? SampleFormat.S16BE : SampleFormat.S16LE;
            } else if (format.getSampleSizeInBits() == 8) {
                return SampleFormat.S8;
            } else if (format.getSampleSizeInBits() == 32) {
                return format.isBigEndian() ? SampleFormat.S32BE : SampleFormat.S32LE;
            }
        } else if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
            if (format.getSampleSizeInBits() == 8) {
                return SampleFormat.U8;
            }
        } else if (format.getEncoding() == AudioFormat.Encoding.PCM_FLOAT) {
             if (format.getSampleSizeInBits() == 32) {
                return format.isBigEndian() ? SampleFormat.F32BE : SampleFormat.F32LE;
            }
        }
        throw new IllegalArgumentException("Unsupported audio encoding: " + format);
    }

    private AudioProcessor() {}
}
