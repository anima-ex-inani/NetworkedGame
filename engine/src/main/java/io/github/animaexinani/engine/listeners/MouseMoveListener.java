package io.github.animaexinani.engine.listeners;

import java.util.EventListener;

/**
 * Functional interface for listening to mouse motion events.
 */
@FunctionalInterface
public interface MouseMoveListener extends EventListener {
    /**
     * Called when the mouse is moved.
     *
     * @param x the new x-coordinate of the mouse
     * @param y the new y-coordinate of the mouse
     */
    void onMouseMove(float x, float y);
}
