package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.rendering.RenderContext;
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
    public void draw(@NotNull RenderContext context) {
        context.renderGeometry(this.vertices, this.indices, this.texture);
    }

    // --- Getters ---

    public int vertexCount() {
        return this.vertices.length;
    }

    public @NotNull Vertex vertexAt(int index) {
        if (index < 0 || index >= this.vertices.length) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + index + " out of bounds for length " + this.vertices.length);
        }
        return this.vertices[index];
    }

    public int indexCount() {
        return this.indices.length;
    }

    public int indexAt(int index) {
        if (index < 0 || index >= this.indices.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for length " + this.indices.length);
        }
        return this.indices[index];
    }

    public @Nullable Texture texture() {
        return this.texture;
    }

    // --- Setters ---

    // Replaces the entire vertex array.
    public void vertices(@NotNull Vertex @NotNull [] vertices) {
        Objects.requireNonNull(vertices);
        for (Vertex vertex : vertices) {
            Objects.requireNonNull(vertex);
        }
        validateIndices(vertices.length, this.indices);
        this.vertices = vertices.clone();
    }

    // Replaces the vertex at the specified position.
    public void vertex(int index, @NotNull Vertex vertex) {
        if (index < 0 || index >= this.vertices.length) {
            throw new IndexOutOfBoundsException(
                    "Vertex index " + index + " out of bounds for length " + this.vertices.length);
        }
        Objects.requireNonNull(vertex);
        this.vertices[index] = vertex;
    }

    // Replaces the entire index array.
    public void indices(int @NotNull [] indices) {
        Objects.requireNonNull(indices);
        validateIndices(this.vertices.length, indices);
        this.indices = indices.clone();
    }


    // Replaces the index value at the specified position.
    public void index(int index, int value) {
        if (index < 0 || index >= this.indices.length) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for length " + this.indices.length);
        }
        if (value < 0 || value >= this.vertices.length) {
            throw new IllegalArgumentException(
                    "Index " + value + " out of bounds for vertex count " + this.vertices.length);
        }
        this.indices[index] = value;
    }

    public void texture(@Nullable Texture texture) {
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