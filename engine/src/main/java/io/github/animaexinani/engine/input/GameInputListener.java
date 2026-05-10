package io.github.animaexinani.engine.input;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.listeners.KeyboardListener;

public final class GameInputListener implements KeyboardListener {

    @NotNull
    private final InputBindings bindings;

    private final Set<GameAction> heldActions = EnumSet.noneOf(GameAction.class);

    private final Map<Integer, GameAction> pressedScancodes = new HashMap<>();

    public GameInputListener(@NotNull InputBindings bindings) {
        this.bindings = bindings;
    }

    @Override
    public void onKeyEvent(@NotNull KeyEvent event) {
        switch (event.action()) {
            case PRESS -> this.bindings.resolve(event.scancode()).ifPresent(action -> {
                this.pressedScancodes.put(event.scancode(), action);
                this.heldActions.add(action);
            });
            case RELEASE -> {
                var action = this.pressedScancodes.remove(event.scancode());
                // Only remove the action from heldActions when no other scancode is still holding it down.
                if (action != null && !this.pressedScancodes.containsValue(action)) {
                    this.heldActions.remove(action);
                }
            }
            case REPEAT -> {}
        }
    }

    public boolean isHeld(@NotNull GameAction action) {
        return this.heldActions.contains(action);
    }

    public Set<GameAction> getHeldActions() {
        return Collections.unmodifiableSet(this.heldActions);
    }

    public void releaseAll() {
        this.pressedScancodes.clear();
        this.heldActions.clear();
    }

    public @NotNull InputBindings getBindings() {
        return this.bindings;
    }
}