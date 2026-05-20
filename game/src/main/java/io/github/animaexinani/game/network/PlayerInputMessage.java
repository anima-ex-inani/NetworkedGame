package io.github.animaexinani.game.network;

import java.util.Set;
import java.util.UUID;

import io.github.animaexinani.engine.input.GameAction;

// client to server
public class PlayerInputMessage {
    public final UUID playerId;
    public final Set<GameAction> heldActions;

    public PlayerInputMessage(UUID playerId, Set<GameAction> heldActions) {
        this.playerId = playerId;
        this.heldActions = heldActions;
    }
}