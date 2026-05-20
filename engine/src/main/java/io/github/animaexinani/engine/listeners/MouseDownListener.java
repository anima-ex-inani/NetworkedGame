package io.github.animaexinani.engine.listeners;

import java.util.EventListener;

/**
 * Functional interface for listening to mouse button down events.
 */
@FunctionalInterface
public interface MouseDownListener extends EventListener {
    /**
     * Called when a mouse button is pressed.
     *
     * @param button the button that was pressed
     * @param x the x-coordinate of the mouse
     * @param y the y-coordinate of the mouse
     */
    void onMouseDown(int button, float x, float y);
}
