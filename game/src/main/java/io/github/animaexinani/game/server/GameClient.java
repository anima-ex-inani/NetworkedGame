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

    private void listenLoop() {
        byte[] buf = new byte[1400];

        while (this.running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                this.socket.receive(packet);

                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());

                long seq = bb.getLong();
                if (seq <= this.lastSequence) continue;
                this.lastSequence = seq;

                int count = bb.getInt();
                
                // set to monitor who is still alive
                Set<UUID> receivedIds = new HashSet<>();

                for (int i = 0; i < count; i++) {
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
        Set<GameAction> actions = inputListener.getHeldActions();
        if (this.socket == null) return;

        try {
            ByteBuffer bb = ByteBuffer.allocate(32);

            bb.putLong(this.myPlayerId.getMostSignificantBits());
            bb.putLong(this.myPlayerId.getLeastSignificantBits());

            int flags = 0;
            for (GameAction action : actions) {
                flags |= (1 << action.ordinal());
            }

            bb.putInt(flags);

            byte[] data = new byte[bb.position()];
            bb.flip();
            bb.get(data);

            DatagramPacket packet =
                    new DatagramPacket(data, data.length, this.serverAddress, this.serverPort);

            this.socket.send(packet);
            LOGGER.fine("Sent input packet to server");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "send failed", e);
        }
    }

    public void stop() {
        this.running = false;
        if (this.socket != null) this.socket.close();
    }
}
