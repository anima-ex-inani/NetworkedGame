package io.github.animaexinani.engine.rendering;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import org.jetbrains.annotations.NotNull;

public interface Renderer extends AutoCloseable {
    void clear(Color color);

    void draw(@NotNull Drawable drawable);

    void present();
}
