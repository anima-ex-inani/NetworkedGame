package io.github.animaexinani.engine.rendering;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rectangle.Rect;
import io.github.animaexinani.engine.rectangle.RectF;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides operations and state for rendering during a draw pass.
 */
public interface RenderContext {
    /**
     * Gets the size of the framebuffer in pixels.
     *
     * @return the framebuffer size
     */
    @NotNull Size framebufferSize();

    /**
     * Renders indexed geometry.
     *
     * @param vertices the vertices to render
     * @param indices  the indices to render
     * @param texture  the texture to use, or {@code null} for untextured geometry
     */
    void renderGeometry(@NotNull Vertex @NotNull [] vertices, int @NotNull [] indices, @Nullable Texture texture);

    /**
     * Draws a single point.
     *
     * @param p     the point to draw
     * @param color the color of the point
     */
    void drawPoint(@NotNull PointF p, @NotNull Color color);

    /**
     * Draws multiple points.
     *
     * @param points the points to draw
     * @param color  the color of the points
     */
    void drawPoints(@NotNull PointF @NotNull [] points, @NotNull Color color);

    /**
     * Draws a line between two points.
     *
     * @param p1    the starting point
     * @param p2    the ending point
     * @param color the color of the line
     */
    void drawLine(@NotNull PointF p1, @NotNull PointF p2, @NotNull Color color);

    /**
     * Draws a series of connected lines.
     *
     * @param points the points defining the lines
     * @param color  the color of the lines
     */
    void drawLines(@NotNull PointF @NotNull [] points, @NotNull Color color);

    /**
     * Draws a rectangular outline.
     *
     * @param x      the x-coordinate of the top-left corner
     * @param y      the y-coordinate of the top-left corner
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param color  the outline color
     */
    void drawRect(float x, float y, float width, float height, @NotNull Color color);

    /**
     * Fills a rectangular area with a color.
     *
     * @param x      the x-coordinate of the top-left corner
     * @param y      the y-coordinate of the top-left corner
     * @param width  the width of the rectangle
     * @param height the height of the rectangle
     * @param color  the fill color
     */
    void fillRect(float x, float y, float width, float height, @NotNull Color color);

    /**
     * Draws a texture at the specified position.
     *
     * @param texture the texture to draw
     * @param x       the x-coordinate of the top-left corner
     * @param y       the y-coordinate of the top-left corner
     */
    void drawTexture(@NotNull Texture texture, float x, float y);

    /**
     * Draws a texture scaled to the specified size.
     *
     * @param texture the texture to draw
     * @param x       the x-coordinate of the top-left corner
     * @param y       the y-coordinate of the top-left corner
     * @param width   the width to draw
     * @param height  the height to draw
     */
    void drawTexture(@NotNull Texture texture, float x, float y, float width, float height);

    /**
     * Draws a part of a texture to a specific area.
     *
     * @param texture the texture to draw
     * @param src     the source rectangle in texture coordinates, or {@code null} for the entire texture
     * @param dst     the destination rectangle in screen coordinates, or {@code null} for the entire render target
     */
    void drawTexture(@NotNull Texture texture, @Nullable Rect src, @Nullable RectF dst);

    /**
     * Sets the clip rectangle for rendering.
     *
     * @param rect the clip rectangle, or {@code null} to disable clipping
     */
    void clipRect(@Nullable Rect rect);

    /**
     * Gets the current clip rectangle.
     *
     * @return the clip rectangle, or {@code null} if clipping is disabled
     */
    @Nullable Rect clipRect();

    /**
     * Sets the render viewport.
     *
     * @param rect the viewport rectangle, or {@code null} for the entire render target
     */
    void viewport(@Nullable Rect rect);

    /**
     * Gets the current render viewport.
     *
     * @return the viewport rectangle
     */
    @NotNull Rect viewport();
}
