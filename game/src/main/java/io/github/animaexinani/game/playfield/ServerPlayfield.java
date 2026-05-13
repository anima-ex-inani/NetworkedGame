package io.github.animaexinani.game.playfield;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.game.nentities.Entity;

/**
 * An authoritative playfield managed by the server.
 * This interface provides methods for modifying the game state, such as spawning and despawning entities.
 */
public interface ServerPlayfield extends Playfield {
    /**
     * Spawns a new entity into the playfield.
     * 
     * @param entity The entity to spawn
     * @return <code>true</code> if the entity collection was modified, <code>false</code> otherwise
     */
    boolean spawnEntity(@NotNull Entity entity);

    /**
     * Removes an entity from the playfield by its unique identifier.
     * 
     * @param id The UUID of the entity to despawn
     * @return <code>true</code> if the entity collection was modified, <code>false</code> otherwise
     */
    boolean despawnEntity(@NotNull UUID id);
}
