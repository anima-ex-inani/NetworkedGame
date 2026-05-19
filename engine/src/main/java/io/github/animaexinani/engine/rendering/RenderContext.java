package io.github.animaexinani.engine.rendering;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rectangle.Rect;
import io.github.animaexinani.engine.rectangle.RectF;
import io.github.animaexinani.engine.size.Size;
import io.github.animaexinani.engine.size.SizeF;
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
     * Draws multiple rectangular outlines.
     *
     * @param rects the rectangles to draw
     * @param color the outline color
     */
    void drawRects(@NotNull RectF @NotNull [] rects, @NotNull Color color);

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
     * Fills multiple rectangular areas.
     *
     * @param rects the rectangles to fill
     * @param color the fill color
     */
    void fillRects(@NotNull RectF @NotNull [] rects, @NotNull Color color);

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
     * Draws a part of a texture to a specific area with rotation and flipping.
     *
     * @param texture  the texture to draw
     * @param src      the source rectangle in texture coordinates, or {@code null} for the entire texture
     * @param dst      the destination rectangle in screen coordinates, or {@code null} for the entire render target
     * @param angle    the rotation angle in degrees clockwise
     * @param center   the center of rotation, or {@code null} to rotate around the center of the destination rectangle
     * @param flipMode the flipping state
     */
    void drawTexture(@NotNull Texture texture, @Nullable Rect src, @Nullable RectF dst, double angle, @Nullable PointF center, @NotNull FlipMode flipMode);

    /**
     * Performs a 9-grid (nine-slice) scaled copy of a texture.
     *
     * @param texture      the texture to draw
     * @param src          the source rectangle, or {@code null} for the entire texture
     * @param leftWidth    the width of the left corners in {@code src}
     * @param rightWidth   the width of the right corners in {@code src}
     * @param topHeight    the height of the top corners in {@code src}
     * @param bottomHeight the height of the bottom corners in {@code src}
     * @param scale        the scale for the corners
     * @param dst          the destination rectangle, or {@code null} for the entire target
     */
    void drawTexture9Grid(@NotNull Texture texture, @Nullable Rect src, float leftWidth, float rightWidth, float topHeight, float bottomHeight, float scale, @Nullable RectF dst);

    /**
     * Performs a 9-grid (nine-slice) scaled copy of a texture where the borders and center are tiled.
     *
     * @param texture      the texture to draw
     * @param src          the source rectangle, or {@code null} for the entire texture
     * @param leftWidth    the width of the left corners in {@code src}
     * @param rightWidth   the width of the right corners in {@code src}
     * @param topHeight    the height of the top corners in {@code src}
     * @param bottomHeight the height of the bottom corners in {@code src}
     * @param scale        the scale for the corners
     * @param dst          the destination rectangle, or {@code null} for the entire target
     * @param tileScale    the scale for the tiled borders and center
     */
    void drawTexture9GridTiled(@NotNull Texture texture, @Nullable Rect src, float leftWidth, float rightWidth, float topHeight, float bottomHeight, float scale, @Nullable RectF dst, float tileScale);

    /**
     * Performs an affine transformation by mapping corners of the source to specific points.
     *
     * @param texture the texture to draw
     * @param src     the source rectangle, or {@code null} for the entire texture
     * @param origin  the destination point for the top-left corner of {@code src}
     * @param right   the destination point for the top-right corner of {@code src}
     * @param down    the destination point for the bottom-left corner of {@code src}
     */
    void drawTextureAffine(@NotNull Texture texture, @Nullable Rect src, @NotNull PointF origin, @NotNull PointF right, @NotNull PointF down);

    /**
     * Tiles a portion of a texture across a destination rectangle.
     *
     * @param texture the texture to draw
     * @param src     the source rectangle, or {@code null} for the entire texture
     * @param scale   the scale of the texture
     * @param dst     the destination rectangle, or {@code null} for the entire target
     */
    void drawTextureTiled(@NotNull Texture texture, @Nullable Rect src, float scale, @Nullable RectF dst);

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
     * Gets the current render viewport.
     *
     * @return the viewport rectangle
     */
    @NotNull Rect viewport();

    /**
     * Sets the current draw color.
     *
     * @param color the color to use for drawing primitives
     */
    void drawColor(@NotNull Color color);

    /**
     * Gets the current draw color.
     *
     * @return the current draw color
     */
    @NotNull Color drawColor();

    /**
     * Sets the current blend mode.
     *
     * @param blendMode the blend mode to use for drawing primitives
     */
    void blendMode(@NotNull BlendMode blendMode);

    /**
     * Gets the current blend mode.
     *
     * @return the current blend mode
     */
    @NotNull BlendMode blendMode();

    /**
     * Sets the rendering scale.
     *
     * @param scale the scaling factor
     */
    void scale(@NotNull SizeF scale);

    /**
     * Gets the rendering scale.
     *
     * @return the scaling factor
     */
    @NotNull SizeF scale();
}
