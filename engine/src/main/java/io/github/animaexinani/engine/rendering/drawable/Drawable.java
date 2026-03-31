package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Drawable {
    @NotNull Vertex @NotNull [] vertices();

    int @NotNull [] indices();

    @Nullable Texture texture();
}
