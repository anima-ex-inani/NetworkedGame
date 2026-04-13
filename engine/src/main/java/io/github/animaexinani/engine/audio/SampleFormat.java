package io.github.animaexinani.engine.audio;

/**
 * The format of a sample in an audio stream.
 */
public enum SampleFormat {
    /**
     * 8-bit unsigned integer.
     */
    U8,

    /**
     * 8-bit signed integer.
     */
    S8,

    /**
     * 16-bit signed integer in little-endian order.
     */
    S16LE,

    /**
     * 16-bit signed integer in big-endian order.
     */
    S16BE,

    /**
     * 32-bit signed integer in little-endian order.
     */
    S32LE,

    /**
     * 32-bit signed integer in big-endian order.
     */
    S32BE,

    /**
     * 32-bit floating point number in little-endian order.
     */
    F32LE,

    /**
     * 32-bit floating point number in big-endian order.
     */
    F32BE,
}
