package io.github.animaexinani.engine.internal.font;

import io.github.animaexinani.engine.font.Font;
import io.github.animaexinani.engine.font.FontStyle;
import io.github.animaexinani.engine.font.FontWeight;
import io.github.animaexinani.engine.font.Glyph;
import io.github.animaexinani.engine.rectangle.Rect;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.LazyTexture;
import io.github.animaexinani.engine.texture.PixelFormat;
import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class FreeTypeFont implements Font {
    private final FontWeight weight;
    private final FontStyle style;
    private final float size;
    private final int lineHeight;
    private final int ascender;

    private final LazyTexture texture;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();

    public FreeTypeFont(long faceHandle, float pixelSize, FontWeight weight, FontStyle style) {
        this.weight = weight;
        this.style = style;
        this.size = pixelSize;

        FT_Face face = FT_Face.create(faceHandle);
        FreeType.FT_Set_Pixel_Sizes(face, 0, (int) pixelSize);

        this.lineHeight = (int) (face.size().metrics().height() >> 6);
        this.ascender = (int) (face.size().metrics().ascender() >> 6);

        // Pre-render common ASCII glyphs into an atlas
        int atlasWidth = 512;
        int atlasHeight = 512;
        ByteBuffer atlasBuffer = ByteBuffer.allocateDirect(atlasWidth * atlasHeight * 4)
                .order(ByteOrder.nativeOrder());
        IntBuffer atlasIntBuffer = atlasBuffer.asIntBuffer();

        int currentX = 0;
        int currentY = 0;
        int maxRowHeight = 0;

        // Render basic ASCII + extended
        for (int i = 32; i < 256; i++) {
            if (FreeType.FT_Load_Char(face, i, FreeType.FT_LOAD_RENDER) != 0) {
                continue;
            }

            FT_GlyphSlot slot = face.glyph();
            FT_Bitmap bitmap = slot.bitmap();

            int bw = bitmap.width();
            int bh = bitmap.rows();

            if (currentX + bw > atlasWidth) {
                currentX = 0;
                currentY += maxRowHeight;
                maxRowHeight = 0;
            }

            if (currentY + bh > atlasHeight) {
                break; // Out of atlas space, ideally we should resize, but 512x512 is enough for
                       // ASCII.
            }

            // Copy bitmap to atlas (grayscale to alpha, color to white)
            ByteBuffer bitmapBuffer = bitmap.buffer(bw * bh);
            for (int y = 0; y < bh; y++) {
                for (int x = 0; x < bw; x++) {
                    byte val = bitmapBuffer.get(y * bitmap.pitch() + x);
                    int atlasIdx = (currentY + y) * atlasWidth + (currentX + x);
                    // RGBA8888 expects an integer where R is the highest byte and A is the lowest
                    // byte.
                    int color = (255 << 24) | (255 << 16) | (255 << 8) | (val & 0xFF);
                    atlasIntBuffer.put(atlasIdx, color);
                }
            }

            Rect texRect = new Rect(currentX, currentY, bw, bh);
            float advance = slot.advance().x() >> 6;
            float xOffset = slot.bitmap_left();
            float yOffset = -slot.bitmap_top(); // Relative to baseline

            this.glyphs.put(i, new GlyphImpl(texRect, bw, bh, xOffset, yOffset, advance));

            currentX += bw + 1; // 1 pixel padding
            if (bh > maxRowHeight) {
                maxRowHeight = bh;
            }
        }

        atlasBuffer.rewind();

        this.texture = new LazyTexture() {
            @Override
            protected ByteBuffer pixelBuffer() {
                return atlasBuffer;
            }

            @Override
            protected Size textureSize() {
                return new Size(atlasWidth, atlasHeight);
            }

            @Override
            protected PixelFormat pixelFormat() {
                return PixelFormat.RGBA_8888;
            }
        };
    }

    @Override
    public FontWeight weight() {
        return this.weight;
    }

    @Override
    public FontStyle style() {
        return this.style;
    }

    @Override
    public float size() {
        return this.size;
    }

    @Override
    public int lineHeight() {
        return this.lineHeight;
    }

    @Override
    public int ascender() {
        return this.ascender;
    }

    @Override
    public @NotNull Texture texture() {
        return this.texture;
    }

    @Override
    public @Nullable Glyph glyph(int codePoint) {
        return this.glyphs.get(codePoint);
    }

    private record GlyphImpl(Rect textureRect, float width, float height, float xOffset, float yOffset, float advance)
            implements Glyph {
    }
}
