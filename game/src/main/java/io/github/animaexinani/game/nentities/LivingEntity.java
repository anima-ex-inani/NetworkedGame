package io.github.animaexinani.game.nentities;

/**
 * Represents an entity that can take damage.
 */
public interface LivingEntity extends Entity {
    /**
     * Gets the current health of this entity.
     * 
     * @return The entity's health
     */
    int health();

    /**
     * Gets the maximum health of this entity
     * 
     * @return The entity's maximum health
     */
    int maxHealth();

    /**
     * Gets the current shield of this entity
     * 
     * @return The entity's shield
     */
    int shield();

    /**
     * Gets the maximum shield of this entity
     * 
     * @return The entity's maximum shield
     */
    int maxShield();

    /**
     * Whether this entity is alive
     * 
     * @return <code>true</code> if the entity is alive, <code>false</code>
     *         otherwise
     */
    default boolean alive() {
        return this.active() && this.health() > 0;
    }
}
