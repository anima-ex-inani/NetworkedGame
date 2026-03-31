package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Geometry implements Drawable {
    private @NotNull Vertex @NotNull [] vertices;

    @Override
    public Vertex @NotNull [] vertices() {
        return vertices;
    }

    public void vertices(@NotNull Vertex @NotNull [] vertices) {
        Objects.requireNonNull(vertices);
        for (Vertex vertex : vertices) {
            Objects.requireNonNull(vertex);
        }
        this.vertices = vertices;
    }

    private int @NotNull [] indices;

    public int @NotNull [] indices() {
        return indices;
    }

    public void indices(int @NotNull [] indices) {
        Objects.requireNonNull(indices);
        this.indices = indices;
    }

    private @Nullable Texture texture;

    public @Nullable Texture texture() {
        return texture;
    }

    public void texture(@Nullable Texture texture) {
        this.texture = texture;
    }

    public Geometry(@NotNull Vertex @NotNull [] vertices, int @NotNull [] indices, @Nullable Texture texture) {
        Objects.requireNonNull(vertices);
        for (Vertex vertex : vertices) {
            Objects.requireNonNull(vertex);
        }
        Objects.requireNonNull(indices);

        this.vertices = vertices;
        this.indices = indices;
        this.texture = texture;
    }
}
