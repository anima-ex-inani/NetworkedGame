package io.github.animaexinani.engine.windowing;

import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.size.Size;

public interface Window extends AutoCloseable {
    /**
     * Gets the renderer associated with the window.
     * @return A renderer that can be used to draw content to the window.
     * 
     * @implSpec The renderer returned by this method should be owned by the window.
     */
    Renderer getRenderer();

    Size clientSize();

    /**
     * Starts text input for this window.
     */
    void startTextInput();

    /**
     * Stops text input for this window.
     */
    void stopTextInput();
}
