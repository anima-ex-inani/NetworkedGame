package io.github.animaexinani.engine.windowing;

import io.github.animaexinani.engine.rendering.Renderer;

public interface Window extends AutoCloseable {
    /**
     * Gets the renderer associated with the window.
     * @return A renderer that can be used to draw content to the window.
     * 
     * @implSpec The renderer returned by this method should be owned by the window.
     */
    Renderer getRenderer();
}
