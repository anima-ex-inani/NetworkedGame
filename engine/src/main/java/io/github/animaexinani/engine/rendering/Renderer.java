package io.github.animaexinani.engine.rendering;

import io.github.animaexinani.engine.color.Color;

public interface Renderer extends AutoCloseable {
    void clear(Color color);

    void present();
}
