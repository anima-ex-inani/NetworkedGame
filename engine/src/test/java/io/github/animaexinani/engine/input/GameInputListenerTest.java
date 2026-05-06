package io.github.animaexinani.engine.input;

import io.github.animaexinani.engine.events.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameInputListenerTest {

    private InputBindings bindings;
    private GameInputListener listener;

    // Scancode for MOVE_UP in default bindings
    private static final int SC_MOVE_UP = 26;
    private static final int SC_MOVE_DOWN = 22;
    private static final int SC_MOVE_LEFT = 4;
    private static final int SC_ATTACK = 9;
    private static final int SC_UNBOUND = 999;

    @BeforeEach
    void setUp() {
        this.bindings = InputBindings.defaultBindings();
        this.listener = new GameInputListener(this.bindings);
    }

    private KeyEvent press(int scancode) {
        return new KeyEvent(scancode, KeyEvent.Action.PRESS);
    }

    private KeyEvent release(int scancode) {
        return new KeyEvent(scancode, KeyEvent.Action.RELEASE);
    }

    private KeyEvent repeat(int scancode) {
        return new KeyEvent(scancode, KeyEvent.Action.REPEAT);
    }

    // --- initial state ---

    @Test
    void initiallyNoActionsAreHeld() {
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
        assertTrue(this.listener.getHeldActions().isEmpty());
    }

    // --- PRESS events ---

    @Test
    void press_boundScancode_marksActionAsHeld() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        assertTrue(this.listener.isHeld(GameAction.MOVE_UP));
    }

    @Test
    void press_unboundScancode_doesNotAddAction() {
        this.listener.onKeyEvent(press(SC_UNBOUND));
        assertTrue(this.listener.getHeldActions().isEmpty());
    }

    @Test
    void press_multipleActions_allMarkedAsHeld() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.onKeyEvent(press(SC_MOVE_DOWN));
        this.listener.onKeyEvent(press(SC_ATTACK));
        assertTrue(this.listener.isHeld(GameAction.MOVE_UP));
        assertTrue(this.listener.isHeld(GameAction.MOVE_DOWN));
        assertTrue(this.listener.isHeld(GameAction.ATTACK));
    }

    // --- RELEASE events ---

    @Test
    void release_afterPress_removesHeldAction() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.onKeyEvent(release(SC_MOVE_UP));
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
    }

    @Test
    void release_withoutPriorPress_doesNotThrow() {
        assertDoesNotThrow(() -> this.listener.onKeyEvent(release(SC_MOVE_UP)));
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
    }

    @Test
    void release_unboundScancode_doesNotThrow() {
        assertDoesNotThrow(() -> this.listener.onKeyEvent(release(SC_UNBOUND)));
    }

    // --- REPEAT events ---

    @Test
    void repeat_doesNotChangeHeldState() {
        this.listener.onKeyEvent(repeat(SC_MOVE_UP));
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
    }

    @Test
    void repeat_doesNotAddAlreadyHeldAction_again() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.onKeyEvent(repeat(SC_MOVE_UP));
        // should still be held exactly once (set semantics)
        assertTrue(this.listener.isHeld(GameAction.MOVE_UP));
        assertEquals(1, this.listener.getHeldActions().size());
    }

    // --- two scancodes bound to same action ---

    @Test
    void twoScancodes_sameAction_actionRemainsHeldUntilBothReleased() {
        // bind a second scancode to MOVE_UP
        this.bindings.bind(100, GameAction.MOVE_UP);

        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.onKeyEvent(press(100));

        // release the first scancode - action should still be held
        this.listener.onKeyEvent(release(SC_MOVE_UP));
        assertTrue(this.listener.isHeld(GameAction.MOVE_UP));

        // release the second scancode - action should now be released
        this.listener.onKeyEvent(release(100));
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
    }

    // --- getHeldActions ---

    @Test
    void getHeldActions_returnsUnmodifiableSet() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        Set<GameAction> held = this.listener.getHeldActions();
        assertThrows(UnsupportedOperationException.class, () -> held.add(GameAction.ATTACK));
    }

    @Test
    void getHeldActions_containsExactlyPressedActions() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.onKeyEvent(press(SC_ATTACK));
        Set<GameAction> held = this.listener.getHeldActions();
        assertEquals(2, held.size());
        assertTrue(held.contains(GameAction.MOVE_UP));
        assertTrue(held.contains(GameAction.ATTACK));
    }

    // --- releaseAll ---

    @Test
    void releaseAll_clearsAllHeldActions() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.onKeyEvent(press(SC_MOVE_DOWN));
        this.listener.releaseAll();
        assertTrue(this.listener.getHeldActions().isEmpty());
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
        assertFalse(this.listener.isHeld(GameAction.MOVE_DOWN));
    }

    @Test
    void releaseAll_afterRelease_pressClearsNormally() {
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        this.listener.releaseAll();
        this.listener.onKeyEvent(press(SC_MOVE_UP));
        assertTrue(this.listener.isHeld(GameAction.MOVE_UP));
        this.listener.onKeyEvent(release(SC_MOVE_UP));
        assertFalse(this.listener.isHeld(GameAction.MOVE_UP));
    }

    // --- getBindings ---

    @Test
    void getBindings_returnsSameBindingsInstance() {
        assertSame(this.bindings, this.listener.getBindings());
    }
}