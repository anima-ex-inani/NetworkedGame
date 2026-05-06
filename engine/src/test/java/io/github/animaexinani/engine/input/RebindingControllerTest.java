package io.github.animaexinani.engine.input;

import io.github.animaexinani.engine.events.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RebindingControllerTest {

    private InputBindings bindings;
    private RebindingController controller;

    @BeforeEach
    void setUp() {
        this.bindings = InputBindings.defaultBindings();
        this.controller = new RebindingController(this.bindings);
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
    void initiallyNotWaitingForInput() {
        assertFalse(this.controller.isWaitingForInput());
    }

    @Test
    void initiallyNoPendingAction() {
        assertTrue(this.controller.getPendingAction().isEmpty());
    }

    // --- startRebinding ---

    @Test
    void startRebinding_setsWaitingForInput() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        assertTrue(this.controller.isWaitingForInput());
    }

    @Test
    void startRebinding_setsPendingAction() {
        this.controller.startRebinding(GameAction.ATTACK);
        Optional<GameAction> pending = this.controller.getPendingAction();
        assertTrue(pending.isPresent());
        assertEquals(GameAction.ATTACK, pending.get());
    }

    @Test
    void startRebinding_replacesExistingPendingAction() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.startRebinding(GameAction.MOVE_DOWN);
        assertEquals(GameAction.MOVE_DOWN, this.controller.getPendingAction().get());
    }

    // --- cancelRebinding ---

    @Test
    void cancelRebinding_clearsWaitingState() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.cancelRebinding();
        assertFalse(this.controller.isWaitingForInput());
    }

    @Test
    void cancelRebinding_clearsPendingAction() {
        this.controller.startRebinding(GameAction.ATTACK);
        this.controller.cancelRebinding();
        assertTrue(this.controller.getPendingAction().isEmpty());
    }

    @Test
    void cancelRebinding_whenNotWaiting_doesNotThrow() {
        assertDoesNotThrow(() -> this.controller.cancelRebinding());
    }

    @Test
    void cancelRebinding_doesNotModifyBindings() {
        int originalScancode = this.bindings.getScancodeFor(GameAction.MOVE_UP).orElseThrow();
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.cancelRebinding();
        assertEquals(originalScancode, this.bindings.getScancodeFor(GameAction.MOVE_UP).orElseThrow());
    }

    // --- onKeyEvent with PRESS ---

    @Test
    void onKeyEvent_press_whileWaiting_updatesBindings() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.onKeyEvent(press(99));
        assertEquals(GameAction.MOVE_UP, this.bindings.resolve(99).get());
    }

    @Test
    void onKeyEvent_press_whileWaiting_clearsWaitingState() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.onKeyEvent(press(99));
        assertFalse(this.controller.isWaitingForInput());
        assertTrue(this.controller.getPendingAction().isEmpty());
    }

    @Test
    void onKeyEvent_press_whenNotWaiting_doesNotChangeBindings() {
        int originalScancode = this.bindings.getScancodeFor(GameAction.MOVE_UP).orElseThrow();
        this.controller.onKeyEvent(press(99));
        assertEquals(originalScancode, this.bindings.getScancodeFor(GameAction.MOVE_UP).orElseThrow());
    }

    // --- onKeyEvent with RELEASE / REPEAT ---

    @Test
    void onKeyEvent_release_whileWaiting_doesNotBind() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.onKeyEvent(release(99));
        // should still be waiting, binding should not have changed
        assertTrue(this.controller.isWaitingForInput());
        assertTrue(this.bindings.resolve(99).isEmpty());
    }

    @Test
    void onKeyEvent_repeat_whileWaiting_doesNotBind() {
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.onKeyEvent(repeat(99));
        assertTrue(this.controller.isWaitingForInput());
        assertTrue(this.bindings.resolve(99).isEmpty());
    }

    // --- callback ---

    @Test
    void callback_invokedOnSuccessfulRebind() {
        AtomicReference<GameAction> callbackAction = new AtomicReference<>();
        AtomicInteger callbackScancode = new AtomicInteger(-1);

        this.controller.setCallback((action, scancode) -> {
            callbackAction.set(action);
            callbackScancode.set(scancode);
        });

        this.controller.startRebinding(GameAction.ATTACK);
        this.controller.onKeyEvent(press(55));

        assertEquals(GameAction.ATTACK, callbackAction.get());
        assertEquals(55, callbackScancode.get());
    }

    @Test
    void callback_notInvokedWhenNotWaiting() {
        AtomicInteger callCount = new AtomicInteger(0);
        this.controller.setCallback((action, scancode) -> callCount.incrementAndGet());

        this.controller.onKeyEvent(press(55));
        assertEquals(0, callCount.get());
    }

    @Test
    void callback_notInvokedOnRelease() {
        AtomicInteger callCount = new AtomicInteger(0);
        this.controller.setCallback((action, scancode) -> callCount.incrementAndGet());

        this.controller.startRebinding(GameAction.MOVE_LEFT);
        this.controller.onKeyEvent(release(55));
        assertEquals(0, callCount.get());
    }

    @Test
    void setCallback_null_noCallbackInvoked() {
        this.controller.setCallback(null);
        this.controller.startRebinding(GameAction.ATTACK);
        assertDoesNotThrow(() -> this.controller.onKeyEvent(press(99)));
    }

    // --- full rebind flow ---

    @Test
    void fullRebindFlow_changesScancodeForAction() {
        // MOVE_UP currently at scancode 26
        this.controller.startRebinding(GameAction.MOVE_UP);
        this.controller.onKeyEvent(press(50));

        // old scancode should be gone
        assertTrue(this.bindings.resolve(26).isEmpty());
        // new scancode should resolve to MOVE_UP
        assertEquals(GameAction.MOVE_UP, this.bindings.resolve(50).get());
    }
}