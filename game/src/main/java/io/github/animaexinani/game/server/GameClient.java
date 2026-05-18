package io.github.animaexinani.game.server;

import java.util.List;
import java.util.UUID;

import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.game.nentities.EntitySnapshot;
import io.github.animaexinani.game.network.PlayerInputMessage;
import io.github.animaexinani.game.playfield.CombinedWorld;

public class GameClient {
    private final CombinedWorld localWorld;
    private final GameInputListener inputListener;
    private final UUID myPlayerId;

    // add a constructor so NetworkedGame can pass these in
    public GameClient(CombinedWorld localWorld, GameInputListener inputListener, UUID myPlayerId) {
        this.localWorld = localWorld;
        this.inputListener = inputListener;
        this.myPlayerId = myPlayerId;
    }

    public void onSnapshotReceived(List<EntitySnapshot> snapshots) {
        for (EntitySnapshot snap : snapshots) {
            this.localWorld.updateEntityTarget(snap); 
        }
    }

    public void sendInputs() {
        var actions = this.inputListener.getHeldActions();
        if (!actions.isEmpty()) {
            PlayerInputMessage msg = new PlayerInputMessage(this.myPlayerId, actions);
            // TODO: network.sendUDP(msg);
        }
    }
}