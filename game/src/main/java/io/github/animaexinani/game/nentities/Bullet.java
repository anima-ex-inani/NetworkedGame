package io.github.animaexinani.game.nentities;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a bullet within the game's playfield
 */
public interface Bullet extends Entity {
    /**
     * Gets the owner of this bullet
     * 
     * @return The bullet's owner
     */
    @Nullable Entity owner();

    /**
     * Gets the damage of this bullet
     * 
     * @return The bullet's damage
     */
    int damage();

    /**
     * Gets the entity types that this bullet should ignore. If the bullet collides
     * with an entity of one of these types, it will not deal damage.
     * 
     * @return The entity types that this bullet should ignore
     */
    @NotNull Set<@NotNull EntityType> ignoredEntities();
}
