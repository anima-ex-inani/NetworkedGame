package io.github.animaexinani.engine.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;

public final class InputBindings {

    // scancode → GameAction
    private final Map<Integer, GameAction> bindings = new HashMap<>();


    // Binds a scancode to a GameAction,
    public void bind(int scancode, @NotNull GameAction action) {
        bindings.put(scancode, action);
    }

    // unbind scancode
    public void unbind(int scancode) {
        bindings.remove(scancode);
    }


    // returns the GameAction bound to the given scancode, or empty if none.

    public Optional<GameAction> resolve(int scancode) {
        return Optional.ofNullable(bindings.get(scancode));
    }

    // reverse-lookup: returns the scancode currently bound to the given action,
    public Optional<Integer> getScancodeFor(@NotNull GameAction action) {
        return bindings.entrySet().stream()
                .filter(e -> e.getValue() == action)
                .map(Map.Entry::getKey)
                .findFirst();
    }


    // Returns an unmodifiable view of all current bindings (scancode → action).
    public Map<Integer, GameAction> getAll() {
        return Collections.unmodifiableMap(bindings);
    }


    // default keybindings
    public static InputBindings defaultBindings() {
        var b = new InputBindings();
        b.bind(26,  GameAction.MOVE_UP);       // SDL_SCANCODE_W
        b.bind(22,  GameAction.MOVE_DOWN);     // SDL_SCANCODE_S
        b.bind(4,   GameAction.MOVE_LEFT);     // SDL_SCANCODE_A
        b.bind(7,   GameAction.MOVE_RIGHT);    // SDL_SCANCODE_D
        b.bind(9,   GameAction.ATTACK);        // SDL_SCANCODE_F
        return b;
    }
}