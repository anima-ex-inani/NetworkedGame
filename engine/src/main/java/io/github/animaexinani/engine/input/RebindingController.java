package io.github.animaexinani.engine.input;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.events.KeyEvent;
import io.github.animaexinani.engine.listeners.KeyboardListener;

public final class RebindingController implements KeyboardListener {

    @FunctionalInterface
    public interface RebindCallback {
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

    public void startRebinding(@NotNull GameAction action) {
        this.pendingAction = action;
    }

    public void cancelRebinding() {
        this.pendingAction = null;
    }

    public boolean isWaitingForInput() {
        return this.pendingAction != null;
    }

    public Optional<GameAction> getPendingAction() {
        return Optional.ofNullable(this.pendingAction);
    }

    public void setCallback(@Nullable RebindCallback callback) {
        this.callback = callback;
    }

    public @NotNull InputBindings getBindings() {
        return this.bindings;
    }

    @Override
    public void onKeyEvent(@NotNull KeyEvent event) {
        if (this.pendingAction == null) return;
        if (event.action() != KeyEvent.Action.PRESS) return;

        var action   = this.pendingAction;
        var scancode = event.scancode();

        this.bindings.bind(scancode, action);
        this.pendingAction = null;

        if (this.callback != null) {
            this.callback.onRebound(action, scancode);
        }
    }
}