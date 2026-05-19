package io.github.animaexinani.engine.listeners;

import java.util.EventListener;

/**
 * Functional interface for listening to mouse button up events.
 */
@FunctionalInterface
public interface MouseUpListener extends EventListener {
    /**
     * Called when a mouse button is released.
     *
     * @param button the button that was released
     * @param x the x-coordinate of the mouse
     * @param y the y-coordinate of the mouse
     */
    void onMouseUp(int button, float x, float y);
}
