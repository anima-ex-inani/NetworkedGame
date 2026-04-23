package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Geometry implements Drawable {
    private @NotNull Vertex @NotNull [] vertices;
    private int @NotNull [] indices;
    private @Nullable Texture texture;

    public Geometry(@NotNull Vertex @NotNull [] vertices, int @NotNull [] indices, @Nullable Texture texture) {
        Objects.requireNonNull(vertices);
        for (Vertex vertex : vertices) {
            Objects.requireNonNull(vertex);
        }
        Objects.requireNonNull(indices);

        validateIndices(vertices.length, indices);
        this.vertices = vertices.clone();
        this.indices = indices.clone();
        this.texture = texture;
    }

    // --- Drawable ---

    @Override
    public int vertexCount() {
        return this.vertices.length;
    }

    @Override
    public @NotNull Vertex vertexAt(int index) {
        if (index < 0 || index >= this.vertices.length) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + index + " out of bounds for length " + this.vertices.length);
        }
        return this.vertices[index];
    }

    @Override
    public int indexCount() {
        return this.indices.length;
    }

    @Override
    public int indexAt(int index) {
        if (index < 0 || index >= this.indices.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for length " + this.indices.length);
        }
        return this.indices[index];
    }

    @Override
    public @Nullable Texture texture() {
        return this.texture;
    }

    // --- Setters ---

    public void setVertices(@NotNull Vertex @NotNull [] vertices) {
        Objects.requireNonNull(vertices);
        for (Vertex vertex : vertices) {
            Objects.requireNonNull(vertex);
        }
        validateIndices(vertices.length, this.indices);
        this.vertices = vertices.clone();
    }

    public void setIndices(int @NotNull [] indices) {
        Objects.requireNonNull(indices);
        validateIndices(this.vertices.length, indices);
        this.indices = indices.clone();
    }

    public void setTexture(@Nullable Texture texture) {
        this.texture = texture;
    }

    private static void validateIndices(int vertexCount, int[] indices) {
        for (int index : indices) {
            if (index < 0 || index >= vertexCount) {
                throw new IllegalArgumentException(
                        "Index " + index + " out of bounds for vertex count " + vertexCount);
            }
        }
    }
}