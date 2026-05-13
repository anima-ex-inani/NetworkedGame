package io.github.animaexinani.game.playfield;

import java.util.Collection;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.util.Updatable;

/**
 * A shared interface representing the game's playfield, containing all active entities.
 * This interface is used by both client and server implementations.
 */
public interface Playfield extends Updatable {
    /**
     * Gets an unmodifiable collection of all entities currently in the playfield.
     * 
     * @return All entities in the playfield
     */
    @NotNull Collection<@NotNull Entity> entities();

    /**
     * Retrieves an entity by its unique identifier.
     * 
     * @param id The UUID of the entity
     * @return The entity, or <code>null</code> if not found
     */
    @Nullable Entity getEntity(UUID id);
}
