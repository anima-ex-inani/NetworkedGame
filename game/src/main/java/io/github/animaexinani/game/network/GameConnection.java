package io.github.animaexinani.game.network;

import io.github.animaexinani.game.playfield.CombinedWorld;
import io.github.animaexinani.game.server.GameClient;
import io.github.animaexinani.game.server.GameServer;

import java.util.UUID;

/**
 * Encapsulates an active game session, including the server (if local),
 * the client, the world, and the local player ID.
 * @param gameServer the game server (null if joining a remote game)
 * @param gameClient the game client
 * @param combinedWorld the combined world
 * @param myPlayerId the local player's UUID
 */
public record GameConnection(
    GameServer gameServer,
    GameClient gameClient,
    CombinedWorld combinedWorld,
    UUID myPlayerId
) {}
