package io.github.animaexinani.game;

import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.rendering.Renderer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages the current game state and transitions between states.
 */
public class GameStateManager {
    private final Deque<GameState> stateStack = new ArrayDeque<>();

    /**
     * Transitions to a new state, clearing the current state stack.
     * @param newState the state to transition to
     */
    public void transitionTo(GameState newState) {
        while (!this.stateStack.isEmpty()) {
            this.stateStack.pop().exit();
        }
        this.stateStack.push(newState);
        if (newState != null) {
            newState.enter();
        }
    }

    /**
     * Pushes a new state onto the stack without exiting the current one.
     * @param newState the state to push
     */
    public void pushState(GameState newState) {
        this.stateStack.push(newState);
        if (newState != null) {
            newState.enter();
        }
    }

    /**
     * Pops the current state off the stack, resuming the previous one.
     */
    public void popState() {
        if (!this.stateStack.isEmpty()) {
            this.stateStack.pop().exit();
        }
    }

    /**
     * Updates the current state.
     * @param dt the time elapsed since the last frame
     */
    public void update(Duration dt) {
        GameState current = this.currentState();
        if (current != null) {
            current.update(dt);
        }
    }

    /**
     * Renders the current state.
     * @param renderer the renderer to use
     */
    public void render(Renderer renderer) {
        GameState current = this.currentState();
        if (current != null) {
            current.render(renderer);
        }
    }

    /**
     * Handles input for the current state.
     * @param inputListener the input listener to query
     * @param dt the time elapsed since the last frame
     */
    public void handleInput(GameInputListener inputListener, Duration dt) {
        GameState current = this.currentState();
        if (current != null) {
            current.handleInput(inputListener, dt);
        }
    }

    public GameState currentState() {
        return this.stateStack.peek();
    }
}
