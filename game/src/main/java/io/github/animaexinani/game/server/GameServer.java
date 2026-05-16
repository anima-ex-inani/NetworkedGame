package io.github.animaexinani.game.server;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.network.PlayerInputMessage;
import io.github.animaexinani.game.playfield.ServerPlayfield;

public class GameServer {
    private final ServerPlayfield playfield;
    
    // thread safe map to hold the latest inputs sent by each client
    private final Map<UUID, Set<GameAction>> clientInputs = new ConcurrentHashMap<>();
    
    private boolean running = false;
    private final int TICKS_PER_SECOND = 60;

    public GameServer(ServerPlayfield playfield) {
        this.playfield = playfield;
    }
    
    // called by the networking library whenever a packet arrives from a client.
    public void onInputMessageReceived(PlayerInputMessage message) {
        // just store the latest input state. We will apply it during the next tick.
        this.clientInputs.put(message.playerId, message.heldActions);
    }

    // starts the authoritative server loop in a dedicated background thread.
    public void start() {
        this.running = true;
        Thread serverThread = new Thread(this::runLoop, "Game-Server-Thread");
        serverThread.start();
    }

    public void stop() {
        this.running = false;
    }

    private void runLoop() {
        long lastTime = System.nanoTime();
        long nsPerTick = 1_000_000_000 / TICKS_PER_SECOND;

        while (this.running) {
            long now = System.nanoTime();
            Duration delta = Duration.ofNanos(now - lastTime);
            lastTime = now;

            // process all queued player actions
            this.processPlayerInputs(delta);

            // step the physics engine forward
            this.playfield.update(delta);

            // TODO: Broadcast the new game state to all clients here
            // this.broadcastGameState(); or something like that

            // sleep to maintain the target Tick Rate
            long timeTaken = System.nanoTime() - now;
            long sleepTimeMs = (nsPerTick - timeTaken) / 1_000_000;
            
            if (sleepTimeMs > 0) {
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void processPlayerInputs(Duration delta) {
        for (Map.Entry<UUID, Set<GameAction>> entry : this.clientInputs.entrySet()) {
            UUID playerId = entry.getKey();
            Set<GameAction> actions = entry.getValue();

            Entity entity = this.playfield.getEntity(playerId);
            
            if (entity instanceof PlayerShip playerShip) {
                // the server passes the network actions to the correct ship
                playerShip.processActions(actions, this.playfield);
            }
        }
    }
}