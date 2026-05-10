package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes a renderable source of indexed vertex data.
 */
public interface Drawable {
    /**
     * Returns the number of indices available for rendering.
     *
     * @return the total number of indices
     */
    int indexCount();

    /**
     * Returns the index value at the specified position.
     *
     * @param index the position of the index to retrieve
     * @return the index value at the requested position
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    int indexAt(int index);

    /**
     * Returns the number of vertices available for rendering.
     *
     * @return the total number of vertices
     */
    int vertexCount();

    /**
     * Returns the vertex at the specified position.
     *
     * @param index the position of the vertex to retrieve
     * @return the vertex at the requested position
     * @throws IndexOutOfBoundsException if {@code index} is out of range
     */
    @NotNull Vertex vertexAt(int index);

    /**
     * Returns the texture associated with this drawable, if any.
     *
     * @return the texture, or {@code null} if this drawable is untextured
     */
    @Nullable Texture texture();
}
