package io.github.animaexinani.engine.input;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.listeners.KeyboardListener;

public final class GameInputListener implements KeyboardListener {

    @NotNull
    private final InputBindings bindings;

    private final Set<GameAction> heldActions = EnumSet.noneOf(GameAction.class);

    public GameInputListener(@NotNull InputBindings bindings) {
        this.bindings = bindings;
    }

    @Override
    public void onKeyEvent(@NotNull KeyEvent event) {
        bindings.resolve(event.scancode()).ifPresent(action -> {
            switch (event.action()) {
                case PRESS   -> heldActions.add(action);
                case RELEASE -> heldActions.remove(action);
                case REPEAT  -> {}
            }
        });
    }

    public boolean isHeld(@NotNull GameAction action) {
        return heldActions.contains(action);
    }

    public Set<GameAction> getHeldActions() {
        return Collections.unmodifiableSet(heldActions);
    }

    public void releaseAll() {
        heldActions.clear();
    }

    public @NotNull InputBindings getBindings() {
        return bindings;
    }
}