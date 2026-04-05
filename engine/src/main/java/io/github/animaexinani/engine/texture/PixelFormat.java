package io.github.animaexinani.engine.texture;

/**
 * The format of a pixel in a texture.
 */
public enum PixelFormat {
    /**
     * RGBA pixel format using 8-bit integers for each channel.
     */
    RGBA_8888(4),

    /**
     * ARGB pixel format using 8-bit integers for each channel.
     */
    ARGB_8888(4),

    /**
     * BGRA pixel format using 8-bit integers for each channel.
     */
    BGRA_8888(4),

    /**
     * ABGR pixel format using 8-bit integers for each channel.
     */
    ABGR_8888(4),

    /**
     * RGBA pixel format using 16-bit floating point numbers for each channel.
     */
    RGBA_F16(8),

    /**
     * ARGB pixel format using 16-bit floating point numbers for each channel.
     */
    ARGB_F16(8),

    /**
     * BGRA pixel format using 16-bit floating point numbers for each channel.
     */
    BGRA_F16(8),

    /**
     * ABGR pixel format using 16-bit floating point numbers for each channel.
     */
    ABGR_F16(8),

    /**
     * RGBA pixel format using 32-bit floating point numbers for each channel.
     */
    RGBA_F32(16),

    /**
     * ARGB pixel format using 32-bit floating point numbers for each channel.
     */
    ARGB_F32(16),

    /**
     * BGRA pixel format using 32-bit floating point numbers for each channel.
     */
    BGRA_F32(16),

    /**
     * ABGR pixel format using 32-bit floating point numbers for each channel.
     */
    ABGR_F32(16);

    private final int bytesPerPixel;

    public int bytesPerPixel() {
        return this.bytesPerPixel;
    }

    public int calculatePitch(int width) {
        return StrictMath.unsignedMultiplyExact(width, this.bytesPerPixel);
    }

    PixelFormat(int bytesPerPixel) {
        this.bytesPerPixel = bytesPerPixel;
    }
}
