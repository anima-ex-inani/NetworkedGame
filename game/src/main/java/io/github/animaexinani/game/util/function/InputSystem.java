package io.github.animaexinani.game.util.function;

import java.util.HashSet;
import java.util.Set;

import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.listeners.KeyboardListener;

public class InputSystem implements KeyboardListener {
    // memory bank
    private final Set<Integer> activeKeys = new HashSet<>();

    @Override
    public void onKeyEvent(KeyEvent event) {
        // route the logic based on the action enum
        if (event.action() == KeyEvent.Action.PRESS) {
            this.activeKeys.add(event.scancode());
        } 
        else if (event.action() == KeyEvent.Action.RELEASE) {
            this.activeKeys.remove(event.scancode());
        }
        // Notice we silently ignore KeyEvent.Action.REPEAT because holding
        // a key down doesn't change the fact that it is currently in the Set.
    }

    // readable method for the game loop to ask about key states
    public boolean isKeyPressed(int scancode) {
        return this.activeKeys.contains(scancode);
    }
}