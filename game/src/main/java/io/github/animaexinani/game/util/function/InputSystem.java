package io.github.animaexinani.game.util.function;
import java.util.HashSet;
import java.util.Set;

import io.github.animaexinani.engine.listeners.KeyboardListener;


public class InputSystem implements KeyboardListener {
    // memory bank moves
    private final Set<Integer> activeKeys = new HashSet<>();

    @Override
    public void onKeyDown(int scancode) {
        this.activeKeys.add(scancode);
    }

    @Override
    public void onKeyUp(int scancode) {
        this.activeKeys.remove(scancode);
    }

    // readable method for the game loop to ask about key states
    public boolean isKeyPressed(int scancode) {
        return this.activeKeys.contains(scancode);
    }
}