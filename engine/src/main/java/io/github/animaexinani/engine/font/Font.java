package io.github.animaexinani.engine.font;

/**
 * Represents a font that can be used for rendering text.
 */
public interface Font {
    FontWeight weight();

    FontStyle style();

    float size();

    int lineHeight();

    int ascender();
}
