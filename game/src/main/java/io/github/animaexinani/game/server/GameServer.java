package io.github.animaexinani.game.server;

import java.net.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.nentities.ScoutDrone;
import io.github.animaexinani.game.nentities.LivingEntity;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.nentities.Damageable;

public class GameServer {
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    private final CombinedWorld playfield;
    private final int port;
    private DatagramSocket socket;

    private volatile boolean running;
    private long snapshotSequence = 0;

    private record ClientConnection(InetAddress address, int port) {}
    private record TimedInput(Set<GameAction> actions, long lastSeenNanos) {}

    private final Set<ClientConnection> clients = ConcurrentHashMap.newKeySet();
    private final Map<UUID, TimedInput> clientInputs = new ConcurrentHashMap<>();
    private long lastSpawnTime = System.nanoTime();

    public GameServer(CombinedWorld playfield, int port) {
        this.playfield = playfield;
        this.port = port;
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
            running = true;

            new Thread(this::listenLoop, "ServerListener").start();
            new Thread(this::runLoop, "ServerLoop").start();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "server start failed", e);
        }
    }

    private void listenLoop() {
        byte[] buf = new byte[256];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                clients.add(new ClientConnection(packet.getAddress(), packet.getPort()));

                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());

                UUID playerId = new UUID(bb.getLong(), bb.getLong());
                int flags = bb.getInt();

                Set<GameAction> actions = EnumSet.noneOf(GameAction.class);
                for (GameAction a : GameAction.values()) {
                    if ((flags & (1 << a.ordinal())) != 0) {
                        actions.add(a);
                    }
                }

                clientInputs.put(playerId, new TimedInput(actions, System.nanoTime()));

            } catch (Exception e) {
                if (running) LOGGER.log(Level.WARNING, "listen failed", e);
            }
        }
    }

    private void runLoop() {
        long last = System.nanoTime();
        long nsPerTick = 1_000_000_000L / 60L;

        while (running) {
            long now = System.nanoTime();
            Duration delta = Duration.ofNanos(now - last);
            last = now;

            if (now - lastSpawnTime > 3_000_000_000L) { // every 3 seconds
                lastSpawnTime = now;
                
                // pick a random edge of the screen
                double margin = 100.0; 
                
                double spawnX;
                double spawnY;
                
                if (Math.random() > 0.5) {
                    // 50% chance to spawn on the TOP or BOTTOM inner edges
                    spawnX = margin + (Math.random() * (1920 - (2 * margin))); // Random X inside margins
                    spawnY = Math.random() > 0.5 ? margin : (1080 - margin);   // Fixed Top or Bottom Y
                } else {
                    // 50% chance to spawn on the LEFT or RIGHT inner edges
                    spawnX = Math.random() > 0.5 ? margin : (1920 - margin);   // Fixed Left or Right X
                    spawnY = margin + (Math.random() * (1080 - (2 * margin))); // Random Y inside margins
                }
                
                ScoutDrone drone = new ScoutDrone(spawnX, spawnY);
                
                // find a player to set as the drone's target
                if (!clients.isEmpty()) {
                    UUID firstPlayer = clientInputs.keySet().iterator().next();
                    drone.setTarget(playfield.getEntity(firstPlayer));
                }
                
                // spawn it with the death listener attached!
                this.spawnEntity(drone);
            }

            processInputs();
            playfield.preUpdate(delta); 
            playfield.update(delta);
            playfield.postUpdate(delta);
            broadcast();

            long sleepMs = (nsPerTick - (System.nanoTime() - now)) / 1_000_000L;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processInputs() {
        long now = System.nanoTime();
        var iter = clientInputs.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            long age = now - entry.getValue().lastSeenNanos;

            // Remove entries stale for more than 5 seconds (disconnected players)
            if (age > 5_000_000_000L) {
                iter.remove();
                continue;
            }

            // Skip processing inputs older than 200ms
            if (age > 200_000_000L) {
                continue;
            }

            Entity e = playfield.getEntity(entry.getKey());
            if (e instanceof PlayerShip ship) {
                ship.processActions(entry.getValue().actions, playfield);
            }
        }
    }

    private void broadcast() {
        try {
            var entities = playfield.entities();

            ByteBuffer bb = ByteBuffer.allocate(1400);
            bb.putLong(snapshotSequence++);
            bb.putInt(entities.size());

            for (Entity entity : entities) {
                var t = entity.physicsBody().getTransform();

                bb.putLong(entity.id().getMostSignificantBits());
                bb.putLong(entity.id().getLeastSignificantBits());
                bb.putInt(entity.type().ordinal());
                bb.putFloat((float) t.getTranslationX());
                bb.putFloat((float) t.getTranslationY());
                bb.putFloat((float) t.getRotationAngle());
                int currentHp = 100;
                if (entity instanceof LivingEntity living) {
                    currentHp = living.health();
                }
                bb.putInt(currentHp);
            }

            byte[] data = Arrays.copyOf(bb.array(), bb.position());

            for (ClientConnection client : clients) {
                socket.send(new DatagramPacket(
                        data, data.length,
                        client.address(), client.port()
                ));
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "broadcast failed", e);
        }
    }

    public void stop() {
        running = false;
        if (socket != null) socket.close();
    }

    public void spawnEntity(Entity entity) {
        // change LivingEntity to Damageable here!
        if (entity instanceof Damageable damageableEntity) {
            damageableEntity.addDamageTakenListener((target, healthDamage, shieldDamage, lethal) -> {
                if (lethal) {
                    System.out.println(target.type() + " was destroyed!");
                    this.playfield.despawnEntity(target.id());
                }
            });
        }
        
        // add it to the world
        this.playfield.spawnEntity(entity);
    }
}