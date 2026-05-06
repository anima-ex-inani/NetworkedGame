package io.github.animaexinani.engine.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InputBindingsTest {

    private InputBindings bindings;

    @BeforeEach
    void setUp() {
        this.bindings = new InputBindings();
    }

    // --- bind / resolve ---

    @Test
    void bind_resolvesCorrectAction() {
        this.bindings.bind(10, GameAction.MOVE_UP);
        Optional<GameAction> result = this.bindings.resolve(10);
        assertTrue(result.isPresent());
        assertEquals(GameAction.MOVE_UP, result.get());
    }

    @Test
    void resolve_returnsEmptyForUnboundScancode() {
        Optional<GameAction> result = this.bindings.resolve(999);
        assertTrue(result.isEmpty());
    }

    @Test
    void bind_replacesOldScancodeForSameAction() {
        this.bindings.bind(10, GameAction.MOVE_UP);
        this.bindings.bind(20, GameAction.MOVE_UP);

        // old scancode should be gone
        assertTrue(this.bindings.resolve(10).isEmpty());
        // new scancode should have the action
        assertEquals(GameAction.MOVE_UP, this.bindings.resolve(20).get());
    }

    @Test
    void bind_allowsDifferentActionsOnDifferentScancodes() {
        this.bindings.bind(1, GameAction.MOVE_LEFT);
        this.bindings.bind(2, GameAction.MOVE_RIGHT);

        assertEquals(GameAction.MOVE_LEFT, this.bindings.resolve(1).get());
        assertEquals(GameAction.MOVE_RIGHT, this.bindings.resolve(2).get());
    }

    @Test
    void bind_overwritesActionForSameScancode() {
        this.bindings.bind(5, GameAction.MOVE_UP);
        this.bindings.bind(5, GameAction.ATTACK);
        assertEquals(GameAction.ATTACK, this.bindings.resolve(5).get());
    }

    // --- unbind ---

    @Test
    void unbind_removesBinding() {
        this.bindings.bind(10, GameAction.MOVE_DOWN);
        this.bindings.unbind(10);
        assertTrue(this.bindings.resolve(10).isEmpty());
    }

    @Test
    void unbind_onUnboundScancode_doesNotThrow() {
        assertDoesNotThrow(() -> this.bindings.unbind(999));
    }

    // --- getScancodeFor ---

    @Test
    void getScancodeFor_returnsScancodeForBoundAction() {
        this.bindings.bind(42, GameAction.ATTACK);
        Optional<Integer> scancode = this.bindings.getScancodeFor(GameAction.ATTACK);
        assertTrue(scancode.isPresent());
        assertEquals(42, scancode.get());
    }

    @Test
    void getScancodeFor_returnsEmptyForUnboundAction() {
        Optional<Integer> scancode = this.bindings.getScancodeFor(GameAction.MOVE_LEFT);
        assertTrue(scancode.isEmpty());
    }

    @Test
    void getScancodeFor_reflectsRebind() {
        this.bindings.bind(10, GameAction.MOVE_UP);
        this.bindings.bind(20, GameAction.MOVE_UP);
        assertEquals(20, this.bindings.getScancodeFor(GameAction.MOVE_UP).get());
    }

    // --- getAll ---

    @Test
    void getAll_returnsAllBindings() {
        this.bindings.bind(1, GameAction.MOVE_UP);
        this.bindings.bind(2, GameAction.MOVE_DOWN);
        Map<Integer, GameAction> all = this.bindings.getAll();
        assertEquals(2, all.size());
        assertEquals(GameAction.MOVE_UP, all.get(1));
        assertEquals(GameAction.MOVE_DOWN, all.get(2));
    }

    @Test
    void getAll_returnsUnmodifiableMap() {
        this.bindings.bind(1, GameAction.MOVE_UP);
        Map<Integer, GameAction> all = this.bindings.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.put(2, GameAction.MOVE_DOWN));
    }

    @Test
    void getAll_returnsEmptyMapWhenNoBindings() {
        assertTrue(this.bindings.getAll().isEmpty());
    }

    // --- defaultBindings ---

    @Test
    void defaultBindings_hasExpectedScancodes() {
        InputBindings defaults = InputBindings.defaultBindings();
        assertEquals(GameAction.MOVE_UP, defaults.resolve(26).get());
        assertEquals(GameAction.MOVE_DOWN, defaults.resolve(22).get());
        assertEquals(GameAction.MOVE_LEFT, defaults.resolve(4).get());
        assertEquals(GameAction.MOVE_RIGHT, defaults.resolve(7).get());
        assertEquals(GameAction.ATTACK, defaults.resolve(9).get());
    }

    @Test
    void defaultBindings_containsExactlyFiveBindings() {
        InputBindings defaults = InputBindings.defaultBindings();
        assertEquals(5, defaults.getAll().size());
    }

    // --- edge cases ---

    @Test
    void bind_negativeScancodeIsAccepted() {
        this.bindings.bind(-1, GameAction.MOVE_UP);
        assertEquals(GameAction.MOVE_UP, this.bindings.resolve(-1).get());
    }

    @Test
    void bind_scancode_zeroIsAccepted() {
        this.bindings.bind(0, GameAction.MOVE_UP);
        assertEquals(GameAction.MOVE_UP, this.bindings.resolve(0).get());
    }
}