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
     * 
     * @implSpec Due to `context` being shared across all drawables, there is no
     *           guarantee as to the initial state of the render context when
     *           this function is called. You must initialize the context to
     *           your expected state before you use it.
     */
    void draw(@NotNull RenderContext context);
}
