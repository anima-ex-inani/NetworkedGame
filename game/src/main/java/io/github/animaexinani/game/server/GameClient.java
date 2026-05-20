package io.github.animaexinani.game.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.game.nentities.EntitySnapshot;
import io.github.animaexinani.game.playfield.CombinedWorld;

public class GameClient {
    private static final Logger LOGGER = Logger.getLogger(GameClient.class.getName());

    private final CombinedWorld localWorld;
    private final UUID myPlayerId;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private volatile boolean running;
    private long lastSequence = -1;
    private long lastPacketTimeNanos = -1;

    public GameClient(CombinedWorld localWorld, UUID myPlayerId) {
        this.localWorld = localWorld;
        this.myPlayerId = myPlayerId;
    }

    public void connect(String host, int port) {
        try {
            this.socket = new DatagramSocket();
            this.serverAddress = InetAddress.getByName(host);
            this.serverPort = port;
            this.running = true;

            Thread t = new Thread(this::listenLoop, "GameClientListener");
            t.setDaemon(true);
            t.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "connect failed", e);
        }
    }

    /**
     * Returns whether the client has received at least one snapshot from the server.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return this.lastSequence > -1;
    }

    /**
     * Returns the system time in nanoseconds when the last packet was received.
     * @return the last packet time in nanos, or -1 if no packet has been received
     */
    public long getLastPacketTimeNanos() {
        return this.lastPacketTimeNanos;
    }

    private void listenLoop() {
        // Increase buffer size to handle full snapshot packets (up to 64 entities)
        byte[] buf = new byte[4096];

        while (this.running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);

                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());

                if (bb.remaining() < 12) continue; // Basic header check

                long seq = bb.getLong();
                if (seq <= this.lastSequence) continue;
                this.lastSequence = seq;
                this.lastPacketTimeNanos = System.nanoTime();

                int count = bb.getInt();
                
                // set to monitor who is still alive
                Set<UUID> receivedIds = new HashSet<>();

                for (int i = 0; i < count; i++) {
                    if (bb.remaining() < 40) break; // Ensure we have enough data for one entity

                    UUID id = new UUID(bb.getLong(), bb.getLong());
                    int typeOrdinal = bb.getInt();

                    float x = bb.getFloat();
                    float y = bb.getFloat();
                    float rot = bb.getFloat();
                    int health = bb.getInt();
                    int shield = bb.getInt();
                    
                    var entityTypes = io.github.animaexinani.game.nentities.EntityType.values();
                    if (typeOrdinal < 0 || typeOrdinal >= entityTypes.length) {
                        LOGGER.warning("Invalid entity type ordinal: " + typeOrdinal);
                        continue;
                    }

                    receivedIds.add(id);

                    var snap = new EntitySnapshot(
                            id, entityTypes[typeOrdinal], x, y, rot, health, shield
                    );
                    
                    if (this.localWorld.getEntity(id) == null) {
                        var freshEntity = new io.github.animaexinani.game.nentities.ClientNetworkEntity(id, entityTypes[typeOrdinal]);
                        freshEntity.setHealth(health);
                        freshEntity.setShield(shield);
                        var t = freshEntity.physicsBody().getTransform();
                        t.setTranslation(x, y);
                        t.setRotation(rot);
                        this.localWorld.spawnEntity(freshEntity);
                    }

                    this.localWorld.updateEntityTarget(snap);
                }
                this.localWorld.removeStaleEntities(receivedIds);

            } catch (Exception e) {
                if (this.running) {
                    LOGGER.log(Level.WARNING, "receive failed", e);
                }
            }
        }
    }

    public void sendInputs(GameInputListener inputListener) {
        if (this.socket == null) return;
        
        Set<GameAction> actions = inputListener.getHeldActions();
        int flags = 0;
        for (GameAction action : actions) {
            flags |= (1 << action.ordinal());
        }
        
        this.sendPacket(flags);
    }

    private void sendPacket(int flags) {
        if (this.socket == null || this.serverAddress == null) return;

        try {
            ByteBuffer bb = ByteBuffer.allocate(20);
            bb.putLong(this.myPlayerId.getMostSignificantBits());
            bb.putLong(this.myPlayerId.getLeastSignificantBits());
            bb.putInt(flags);

            byte[] data = bb.array();
            DatagramPacket packet = new DatagramPacket(data, bb.position(), this.serverAddress, this.serverPort);
            this.socket.send(packet);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "send failed", e);
        }
    }

    public void stop() {
        if (this.running) {
            // Send a disconnect notification (bit 31)
            this.sendPacket(0x80000000);
            this.running = false;
            if (this.socket != null) this.socket.close();
        }
    }
}
