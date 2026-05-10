package io.github.animaexinani.engine.audio;

/**
 * The format of a sample in an audio stream.
 */
public enum SampleFormat {
    /**
     * 8-bit unsigned integer.
     */
    U8(1),

    /**
     * 8-bit signed integer.
     */
    S8(1),

    /**
     * 16-bit signed integer in little-endian order.
     */
    S16LE(2),

    /**
     * 16-bit signed integer in big-endian order.
     */
    S16BE(2),

    /**
     * 32-bit signed integer in little-endian order.
     */
    S32LE(4),

    /**
     * 32-bit signed integer in big-endian order.
     */
    S32BE(4),

    /**
     * 32-bit floating point number in little-endian order.
     */
    F32LE(4),

    /**
     * 32-bit floating point number in big-endian order.
     */
    F32BE(4);

    private final int bytes;

    public int bytes() {
        return this.bytes;
    }

    SampleFormat(int bytes) {
        this.bytes = bytes;
    }
}
