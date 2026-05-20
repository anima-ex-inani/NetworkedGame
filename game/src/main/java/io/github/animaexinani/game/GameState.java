package io.github.animaexinani.game;

import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.rendering.Renderer;
import java.time.Duration;

/**
 * Represents a distinct state or screen in the application.
 */
public interface GameState {
    /**
     * Called when the state is entered.
     */
    void enter();

    /**
     * Called on each frame to update the state's logic.
     * @param dt the time elapsed since the last frame
     */
    void update(Duration dt);

    /**
     * Called on each frame to render the state.
     * @param renderer the renderer to use
     */
    void render(Renderer renderer);

    /**
     * Called to handle input for this state.
     * @param inputListener the input listener to query
     * @param dt the time elapsed since the last frame
     */
    void handleInput(GameInputListener inputListener, Duration dt);

    /**
     * Called when the state is exited.
     */
    void exit();
}
