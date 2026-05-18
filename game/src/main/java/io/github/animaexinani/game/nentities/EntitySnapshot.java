package io.github.animaexinani.game.nentities;

import java.util.UUID;

/*
a lean representation of an entity at a specific moment in time
the server will look at the real Entity, extract the necessary numbers,
and put them in a snapshot to mail to the clients.
*/
public record EntitySnapshot(UUID id, EntityType type, float x, float y, float rotation, int health) {}