package io.github.animaexinani.engine.input;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.listeners.KeyboardListener;


public final class RebindingController implements KeyboardListener {

    // Called when a rebind completes.
    @FunctionalInterface
    public interface RebindCallback {
        /**
         * @param action   the action that was just rebound
         * @param scancode the new scancode it was bound to
         */
        void onRebound(@NotNull GameAction action, int scancode);
    }

    @NotNull
    private final InputBindings bindings;

    @Nullable
    private GameAction pendingAction = null;

    @Nullable
    private RebindCallback callback = null;

    public RebindingController(@NotNull InputBindings bindings) {
        this.bindings = bindings;
    }

    // -------------------------------------------------------------------------
    // Control API
    // -------------------------------------------------------------------------

    /**
     * Enters "listening" mode: the next key press will be bound to
     * {@code action}.
     */
    public void startRebinding(@NotNull GameAction action) {
        this.pendingAction = action;
    }

    /**
     * Cancels a pending rebind without changing anything.
     */
    public void cancelRebinding() {
        this.pendingAction = null;
    }

    /**
     * @return {@code true} while waiting for the player to press a key.
     *         Use this to show a "Press any key…" prompt in your UI.
     */
    public boolean isWaitingForInput() {
        return pendingAction != null;
    }

    /**
     * Returns the action currently awaiting a new binding, if any.
     */
    public Optional<GameAction> getPendingAction() {
        return Optional.ofNullable(pendingAction);
    }

    /**
     * Sets an optional callback that is invoked every time a rebind completes.
     */
    public void setCallback(@Nullable RebindCallback callback) {
        this.callback = callback;
    }

    // -------------------------------------------------------------------------
    // KeyboardListener
    // -------------------------------------------------------------------------

    @Override
    public void onKeyEvent(@NotNull KeyEvent event) {
        if (pendingAction == null) return;
        if (event.action() != KeyEvent.Action.PRESS) return;

        var action   = pendingAction;
        var scancode = event.scancode();

        bindings.bind(scancode, action);
        pendingAction = null;

        if (callback != null) {
            callback.onRebound(action, scancode);
        }
    }
}