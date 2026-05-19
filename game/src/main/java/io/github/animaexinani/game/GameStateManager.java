package io.github.animaexinani.game;

import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.rendering.Renderer;
import java.time.Duration;
import java.util.Objects;

/**
 * Manages the current game state and transitions between states.
 */
public class GameStateManager {
    private GameState currentState;

    /**
     * Transitions to a new state.
     * @param newState the state to transition to
     */
    public void transitionTo(GameState newState) {
        if (this.currentState != null) {
            this.currentState.exit();
        }
        this.currentState = newState;
        if (this.currentState != null) {
            this.currentState.enter();
        }
    }

    /**
     * Updates the current state.
     * @param dt the time elapsed since the last frame
     */
    public void update(Duration dt) {
        if (this.currentState != null) {
            this.currentState.update(dt);
        }
    }

    /**
     * Renders the current state.
     * @param renderer the renderer to use
     */
    public void render(Renderer renderer) {
        if (this.currentState != null) {
            this.currentState.render(renderer);
        }
    }

    /**
     * Handles input for the current state.
     * @param inputListener the input listener to query
     * @param dt the time elapsed since the last frame
     */
    public void handleInput(GameInputListener inputListener, Duration dt) {
        if (this.currentState != null) {
            this.currentState.handleInput(inputListener, dt);
        }
    }

    public GameState currentState() {
        return this.currentState;
    }
}
