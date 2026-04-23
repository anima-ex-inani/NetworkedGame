package io.github.animaexinani.engine.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public final class InputBindings {
    private final Map<Integer, GameAction> bindings = new HashMap<>();

    public void bind(int scancode, @NotNull GameAction action) {
        this.bindings.entrySet().removeIf(entry -> entry.getValue() == action);
        this.bindings.put(scancode, action);
    }

    public void unbind(int scancode) {
        this.bindings.remove(scancode);
    }

    public Optional<GameAction> resolve(int scancode) {
        return Optional.ofNullable(this.bindings.get(scancode));
    }

    public Optional<Integer> getScancodeFor(@NotNull GameAction action) {
        return this.bindings.entrySet().stream()
                .filter(e -> e.getValue() == action)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Map<Integer, GameAction> getAll() {
        return Collections.unmodifiableMap(this.bindings);
    }

    public static InputBindings defaultBindings() {
        var b = new InputBindings();
        b.bind(26, GameAction.MOVE_UP);     // W
        b.bind(22, GameAction.MOVE_DOWN);   // S
        b.bind(4,  GameAction.MOVE_LEFT);   // A
        b.bind(7,  GameAction.MOVE_RIGHT);  // D
        b.bind(9,  GameAction.ATTACK);      // F
        return b;
    }
}