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

            processInputs();
            playfield.update(delta);
            broadcast();

            long sleepMs = (nsPerTick - (System.nanoTime() - now)) / 1_000_000L;
            if (sleepMs > 0) {
                try { Thread.sleep(sleepMs); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processInputs() {
        long now = System.nanoTime();

        for (var entry : clientInputs.entrySet()) {
            if (now - entry.getValue().lastSeenNanos > 200_000_000L) {
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
                bb.putInt(100);
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
}