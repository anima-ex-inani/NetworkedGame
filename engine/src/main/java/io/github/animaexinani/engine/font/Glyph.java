package io.github.animaexinani.engine.font;

import io.github.animaexinani.engine.rectangle.Rect;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single glyph's rendering metrics within a font.
 */
public interface Glyph {
    /**
     * @return The texture coordinates of this glyph in the font's atlas.
     */
    @NotNull
    Rect textureRect();

    /**
     * @return The width of the glyph.
     */
    float width();

    /**
     * @return The height of the glyph.
     */
    float height();

    /**
     * @return The X offset to apply when rendering this glyph relative to the
     *         cursor.
     */
    float xOffset();

    /**
     * @return The Y offset to apply when rendering this glyph relative to the
     *         cursor.
     */
    float yOffset();

    /**
     * @return The amount to advance the cursor horizontally after rendering this
     *         glyph.
     */
    float advance();
}
