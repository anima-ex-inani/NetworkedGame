package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.rendering.RenderContext;
import org.jetbrains.annotations.NotNull;

/**
 * Describes an object that can be drawn to a rendering context.
 */
public interface Drawable {
    /**
     * Draws the object to the specified rendering context.
     *
     * @param context the context to draw to
     */
    void draw(@NotNull RenderContext context);
}
