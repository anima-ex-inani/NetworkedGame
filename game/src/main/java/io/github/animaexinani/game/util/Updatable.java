package io.github.animaexinani.game.util;

import java.time.Duration;

/**
 * Represents an object whose state can be updated after a certain amount of time has passed.
 */
public interface Updatable {
    /**
     * A function that is called to update the state of the object.
     * @param delta The time that has passed since the last update
     */
    default void update(Duration delta) {
    }

    /**
     * A function that is called to update the state of the object after the {@link #update}
     * function has been called.
     * @param delta The time that has passed since the last update. This is the same value as passed
     *              to {@link #update}
     */
    default void postUpdate(Duration delta) {
    }
}
