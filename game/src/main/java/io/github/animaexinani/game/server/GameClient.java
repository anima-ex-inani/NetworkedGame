package io.github.animaexinani.game.server;

import java.net.*;
import java.nio.ByteBuffer;
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
    private final GameInputListener inputListener;
    private final UUID myPlayerId;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private volatile boolean running;
    private long lastSequence = -1;

    public GameClient(CombinedWorld localWorld, GameInputListener inputListener, UUID myPlayerId) {
        this.localWorld = localWorld;
        this.inputListener = inputListener;
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

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());

                long seq = bb.getLong();
                if (seq <= lastSequence) continue;
                lastSequence = seq;

                int count = bb.getInt();

                for (int i = 0; i < count; i++) {
                    UUID id = new UUID(bb.getLong(), bb.getLong());
                    int typeOrdinal = bb.getInt();
                    float x = bb.getFloat();
                    float y = bb.getFloat();
                    float rot = bb.getFloat();
                    int health = bb.getInt();

                    var snap = new EntitySnapshot(
                            id,
                            io.github.animaexinani.game.nentities.EntityType.values()[typeOrdinal],
                            x, y, rot, health
                    );

                    localWorld.updateEntityTarget(snap);
                }

            } catch (Exception e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "receive failed", e);
                }
            }
        }
    }

    public void sendInputs() {
        Set<GameAction> actions = inputListener.getHeldActions();
        if (socket == null) return;

        try {
            ByteBuffer bb = ByteBuffer.allocate(32);

            bb.putLong(myPlayerId.getMostSignificantBits());
            bb.putLong(myPlayerId.getLeastSignificantBits());

            int flags = 0;
            for (GameAction action : actions) {
                flags |= (1 << action.ordinal());
            }

            bb.putInt(flags);

            byte[] data = new byte[bb.position()];
            bb.flip();
            bb.get(data);

            DatagramPacket packet =
                    new DatagramPacket(data, data.length, serverAddress, serverPort);

            socket.send(packet);
            System.out.println("Sent input packet to server");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "send failed", e);
        }
    }

    public void stop() {
        running = false;
        if (socket != null) socket.close();
    }
}