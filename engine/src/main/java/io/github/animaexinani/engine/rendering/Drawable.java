package io.github.animaexinani.engine.rendering;

import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Drawable {
    @NotNull Vertex @NotNull [] getVertices();

    int @NotNull [] getIndices();

    @Nullable Texture getTexture();
}
