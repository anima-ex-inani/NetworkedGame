package io.github.animaexinani.engine.font;

import io.github.animaexinani.engine.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a font that can be used for rendering text.
 */
public interface Font {
    FontWeight weight();

    FontStyle style();

    float size();

    int lineHeight();

    int ascender();

    /**
     * @return The texture containing the glyphs for this font.
     */
    @NotNull Texture texture();

    /**
     * Retrieves the glyph metrics for a given code point.
     * @param codePoint The Unicode code point of the character.
     * @return The glyph metrics, or null if the glyph is not loaded/available.
     */
    @Nullable Glyph glyph(int codePoint);

    /**
     * Retrieves the glyph metrics for a given character.
     * @param character The character.
     * @return The glyph metrics, or null if the glyph is not loaded/available.
     * @throws UnsupportedOperationException if the character is a surrogate.
     */
    @Nullable
    default Glyph glyph(char character) {
        if (Character.isSurrogate(character)) {
            throw new UnsupportedOperationException("Surrogate pairs must be queried using the surrogate pair overload");
        }
        return this.glyph((int) character);
    }

    /**
     * Retrieves the glyph metrics for a given surrogate pair.
     * @param high The high surrogate.
     * @param low The low surrogate.
     * @return The glyph metrics, or null if the glyph is not loaded/available.
     */
    @Nullable
    default Glyph glyph(char high, char low) {
        return this.glyph(Character.toCodePoint(high, low));
    }
}
