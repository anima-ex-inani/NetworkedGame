package io.github.animaexinani.game.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.game.nentities.Asteroid;
import io.github.animaexinani.game.nentities.Damageable;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.LivingEntity;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.nentities.ScoutDrone;
import io.github.animaexinani.game.nentities.StrikeFighter;
import io.github.animaexinani.game.playfield.CombinedWorld;

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

    /**
     * Tracks which player UUIDs have already had a PlayerShip spawned for them,
     * so we never create duplicates even if packets arrive out of order on startup.
     */
    private final Set<UUID> spawnedPlayers = ConcurrentHashMap.newKeySet();

    private static final int MAX_ENTITIES = 64;

    private long lastSpawnTime = System.nanoTime();
    private long lastAsteroidSpawnTime = System.nanoTime();

    public GameServer(CombinedWorld playfield, int port) {
        this.playfield = playfield;
        this.port = port;
    }

    public void start() {
        try {
            this.socket = new DatagramSocket(this.port);
            this.running = true;

            new Thread(this::listenLoop, "ServerListener").start();
            new Thread(this::runLoop, "ServerLoop").start();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "server start failed", e);
        }
    }

    private void listenLoop() {
        byte[] buf = new byte[256];

        while (this.running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);

                this.clients.add(new ClientConnection(packet.getAddress(), packet.getPort()));

                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());

                UUID playerId = new UUID(bb.getLong(), bb.getLong());
                int flags = bb.getInt();

                Set<GameAction> actions = EnumSet.noneOf(GameAction.class);
                for (GameAction a : GameAction.values()) {
                    if ((flags & (1 << a.ordinal())) != 0) {
                        actions.add(a);
                    }
                }

                this.clientInputs.put(playerId, new TimedInput(actions, System.nanoTime()));

                // Spawn a PlayerShip the very first time we hear from this client.
                // spawnedPlayers guards against races if two packets arrive simultaneously.
                if (this.spawnedPlayers.add(playerId)) {
                    this.spawnPlayerShip(playerId);
                }

            } catch (Exception e) {
                if (this.running) LOGGER.log(Level.WARNING, "listen failed", e);
            }
        }
    }

    /**
     * Creates and registers a {@link PlayerShip} for the given client UUID.
     */
    private void spawnPlayerShip(UUID playerId) {
        PlayerShip ship = new PlayerShip(playerId);
        ship.physicsBody().translate(960.0, 540.0);
        this.spawnEntity(ship);
        LOGGER.info("Spawned PlayerShip for new client " + playerId);
    }

    private void runLoop() {
        long last = System.nanoTime();
        long nsPerTick = 1_000_000_000L / 60L;

        while (this.running) {
            long now = System.nanoTime();
            Duration delta = Duration.ofNanos(now - last);
            last = now;

            // Asteroid spawner
            if (now - this.lastAsteroidSpawnTime > 10_000_000_000L) { // every 10 seconds
                this.lastAsteroidSpawnTime = now;

                float spawnX = 0f;
                float spawnY = 0f;
                
                // random speed between 50 and 150
                double vx = 50.0 + (Math.random() * 100.0);
                double vy = 50.0 + (Math.random() * 100.0);

                // pick a random edge: 0=Top, 1=Bottom, 2=Left, 3=Right
                int edge = (int) (Math.random() * 4);
                switch (edge) {
                    case 0 -> { // Top Edge
                        spawnX = (float) (Math.random() * 1920);
                        spawnY = 10f; // Just inside the top edge
                    }
                    case 1 -> { // Bottom Edge
                        spawnX = (float) (Math.random() * 1920);
                        spawnY = 1070f; // Just inside the bottom edge
                        vy = -vy; // Invert Y velocity so it flies UP
                    }
                    case 2 -> { // Left Edge
                        spawnX = 10f; // Just inside left edge
                        spawnY = (float) (Math.random() * 1080);
                    }
                    case 3 -> { // Right Edge
                        spawnX = 1910f; // Just inside right edge
                        spawnY = (float) (Math.random() * 1080);
                        vx = -vx; // Invert X velocity so it flies LEFT
                    }
                }

                // create and spawn the asteroid
                Asteroid asteroid = new Asteroid(EntityType.ASTEROID, spawnX, spawnY, vx, vy);
                this.spawnEntity(asteroid);
            }

            if (now - this.lastSpawnTime > 3_000_000_000L) { // every 3 seconds
                this.lastSpawnTime = now;
                
                // calculate the spawn coordinates
                double margin = 100.0; 
                double spawnX;
                double spawnY;
                
                if (Math.random() > 0.5) {
                    spawnX = margin + (Math.random() * (1920 - (2 * margin))); 
                    spawnY = Math.random() > 0.5 ? margin : (1080 - margin);   
                } else {
                    spawnX = Math.random() > 0.5 ? margin : (1920 - margin);   
                    spawnY = margin + (Math.random() * (1080 - (2 * margin))); 
                }
                
                // pick which enemy to spawn
                Entity spawnedEnemy;
                
                if (Math.random() < 0.20) {
                    StrikeFighter fighter = new StrikeFighter(spawnX, spawnY, this.playfield);
                    
                    if (!this.clientInputs.isEmpty()) {
                        UUID firstPlayer = this.clientInputs.keySet().iterator().next();
                        fighter.setTarget(this.playfield.getEntity(firstPlayer));
                    }
                    spawnedEnemy = fighter;
                    
                } else { 
                    ScoutDrone drone = new ScoutDrone(spawnX, spawnY, this.playfield);
                    
                    if (!this.clientInputs.isEmpty()) {
                        UUID firstPlayer = this.clientInputs.keySet().iterator().next();
                        drone.setTarget(this.playfield.getEntity(firstPlayer));
                    }
                    spawnedEnemy = drone;
                }

                // add it to the world
                this.spawnEntity(spawnedEnemy);
            }

            this.processInputs();
            this.playfield.preUpdate(delta);
            this.playfield.update(delta);
            this.playfield.postUpdate(delta);
            this.broadcast();

            long sleepMs = (nsPerTick - (System.nanoTime() - now)) / 1_000_000L;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processInputs() {
        long now = System.nanoTime();
        var iter = this.clientInputs.entrySet().iterator();
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

            Entity e = this.playfield.getEntity(entry.getKey());
            if (e instanceof PlayerShip ship) {
                ship.processActions(entry.getValue().actions, this.playfield);
            }
        }
    }


    // payload sizes
    private static final int HEADER_BYTES = 12;
    private static final int ENTITY_BYTES = 46;


    private void broadcast() {
        try {
            var entities = this.playfield.entities();

            // get needed bytes
            int needed = HEADER_BYTES + ENTITY_BYTES * entities.size();

            ByteBuffer bb = ByteBuffer.allocate(needed);
            bb.putLong(this.snapshotSequence++);
            
            int countPos = bb.position();
            bb.putInt(0);

            int written = 0;
            for (Entity entity : entities) {
                var t = entity.physicsBody().getTransform();

                bb.putLong(entity.id().getMostSignificantBits());
                bb.putLong(entity.id().getLeastSignificantBits());
                bb.putInt(entity.type().ordinal());
                bb.putFloat((float) t.getTranslationX());
                bb.putFloat((float) t.getTranslationY());
                bb.putFloat((float) t.getRotationAngle());

                int currentHp = 100;
                int currentShield = 0;
                
                if (entity instanceof LivingEntity living) {
                    currentHp = living.health();
                    currentShield = living.shield();
                }
                bb.putInt(currentHp);
                bb.putInt(currentShield);
                written++;
            }

            bb.putInt(countPos, written);

            byte[] data = Arrays.copyOf(bb.array(), bb.position());

            for (ClientConnection client : this.clients) {
                this.socket.send(new DatagramPacket(
                        data, data.length,
                        client.address(), client.port()
                ));
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "broadcast failed", e);
        }
    }

    public void stop() {
        this.running = false;
        if (this.socket != null) this.socket.close();
    }

    /**
     * Attaches a death-listener to damageable entities and queues them for
     * addition to the world on the next {@code preUpdate}.
     */
    public void spawnEntity(Entity entity) {
        if (this.playfield.entities().size() >= MAX_ENTITIES) {
            LOGGER.warning("Entity cap reached (" + MAX_ENTITIES + ")");
            return;
        }
        if (entity instanceof Damageable damageable) {
            damageable.addDamageTakenListener((target, healthDamage, shieldDamage, lethal) -> {
                if (lethal) {
                    LOGGER.info(target.type() + " destroyed!");
                    this.playfield.despawnEntity(target.id());
                }
            });
        }
        this.playfield.spawnEntity(entity);
    }
}