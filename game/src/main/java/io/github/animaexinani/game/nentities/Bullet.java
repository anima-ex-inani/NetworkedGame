package io.github.animaexinani.game.nentities;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a bullet within the game's playfield
 */
public interface Bullet extends Entity {
    /**
     * Gets the owner of this bullet
     * 
     * @return The bullet's owner, or null if the bullet has no owner
     */
    @Nullable Entity owner();

    /**
     * Returns the amount of damage dealt by this entity.
     * 
     * @return the damage amount
     */
    int damage();

    /**
     * Returns the minimum damage that can be dealt by this entity.
     * 
     * @return the minimum damage amount
     */
    int minimumDamage();

    /**
     * Checks if this entity should deal damage to the given entity.
     * 
     * @param target the entity to check
     * @return true if this entity should deal damage to the given entity, false otherwise
     */
    boolean dealsDamageTo(Damageable target);
}
