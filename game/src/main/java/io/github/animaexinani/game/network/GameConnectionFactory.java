package io.github.animaexinani.game.network;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.game.nentities.Asteroid;
import io.github.animaexinani.game.nentities.ClientNetworkEntity;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.nentities.ServerNetworkEntity;
import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameClient;
import io.github.animaexinani.game.server.GameServer;
import io.github.animaexinani.game.util.UUIDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Factory for creating game connections.
 */
public class GameConnectionFactory {

    /**
     * Creates a local game connection (starts a local server).
     * @param sizeF the size of the playfield
     * @param port the port to host on
     * @return the game connection
     */
    public static GameConnection createLocalConnection(SizeF sizeF, int port) {
        // Server setup
        List<Entity> serverEntities = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            float x = rand.nextFloat() * sizeF.width();
            float y = rand.nextFloat() * sizeF.height();
            double vx = rand.nextDouble() * 100 - 50;
            double vy = rand.nextDouble() * 100 - 50;
            serverEntities.add(new Asteroid(EntityType.ASTEROID, x, y, vx, vy));
        }

        UUID serverId = new UUID(0L, 0L);
        ServerNetworkEntity serverDummy = new ServerNetworkEntity(serverId, EntityType.PLAYER);
        serverEntities.add(serverDummy);

        CombinedWorld serverWorld = new CombinedWorld.Builder()
                .withEntities(serverEntities)
                .withLocalPlayerId(serverId)
                .withSize(sizeF)
                .build();

        GameServer server = new GameServer(serverWorld, port, true);
        server.start();

        // Client setup (connecting to itself)
        GameConnection clientConn = createClientConnection(sizeF);
        clientConn.gameClient().connect("127.0.0.1", port);

        return new GameConnection(server, clientConn.gameClient(), clientConn.combinedWorld(), clientConn.myPlayerId());
    }

    /**
     * Creates a client game connection (ready to connect to a remote server).
     * @param sizeF the size of the playfield
     * @return the game connection
     */
    public static GameConnection createClientConnection(SizeF sizeF) {
        UUID myPlayerId = UUIDGenerator.generateV7Uuid();
        ClientNetworkEntity localDummy = new ClientNetworkEntity(myPlayerId, EntityType.PLAYER);

        CombinedWorld world = new CombinedWorld.Builder()
                .withEntity(localDummy)
                .withLocalPlayerId(myPlayerId)
                .withSize(sizeF)
                .withVisualFactory(EntityType.BULLET, entity -> new ConvexPolygon(
                        new PointF[]{
                            new PointF(5.0f, 0.0f),
                            new PointF(-2.0f, 2.5f),
                            new PointF(-2.0f, -2.5f)
                        }, Color.WHITE))
                .withVisualFactory(EntityType.ASTEROID, entity -> new ConvexPolygon(
                        Asteroid.getAsteroidLocalPointsForType(EntityType.ASTEROID)
                                .toArray(PointF[]::new),
                        new Color(0.6f, 0.6f, 0.6f, 1.0f)))
                .withVisualFactory(EntityType.PLAYER, entity -> {
                    Color c = new Color(0.0f, 1.0f, 0.0f, 1.0f);
                    if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                })
                .withVisualFactory(EntityType.SCOUT_DRONE, entity -> {
                    Color c = new Color(1.0f, 1.0f, 0.0f, 1.0f);
                    if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                })
                .withVisualFactory(EntityType.STRIKE_FIGHTER, entity -> {
                    Color c = new Color(1.0f, 0.0f, 1.0f, 1.0f);
                    if (entity instanceof ClientNetworkEntity ce) c = ce.getVisualColor(c);
                    return new ConvexPolygon(PlayerShip.LOCAL_COORDS.toArray(PointF[]::new), c);
                })
                .build();

        GameClient client = new GameClient(world, myPlayerId);
        return new GameConnection(null, client, world, myPlayerId);
    }
}
