package io.github.animaexinani.game.nentities;

/**
 * The type of an entity within the game's playfield
 * 
 * While the functionality of the entity is defined by the implementing type,
 * this type is used for serialization as well as determining on the client side
 * what visual to use for rendering the entity.
 */
public enum EntityType {
    /**
     * The entity refers to a player.
     */
    PLAYER,

    /**
     * The entity refers to an asteroid.
     */
    ASTEROID,

    /**
     * The entity refers to a bullet.
     */
    BULLET,

    SCOUT_DRONE,   

    STRIKE_FIGHTER;

    /**
     * Checks if the entity type refers to a player entity.
     * @return true if the entity type is PLAYER, false otherwise
     */
    public boolean player() {
        return this == PLAYER;
    }

    /**
     * Checks if the entity type refers to an obstacle entity.
     * @return true if the entity type is ASTEROID, false otherwise
     */
    public boolean obstacle() {
        return this.asteroid();
    }

    /**
     * Checks if the entity type refers to an asteroid entity.
     * @return true if the entity type is ASTEROID, false otherwise
     */
    public boolean asteroid() {
        return this == ASTEROID;
    }

    /**
     * Checks if the entity type refers to a bullet entity.
     * @return true if the entity type is BULLET, false otherwise
     */
    public boolean bullet() {
        return this == BULLET;
    }

    /**
     * Checks if the entity type refers to an enemy entity.
     * @return Currently always returns false until enemies are implemented
     */
    public boolean enemy() {
        return this == SCOUT_DRONE || this == STRIKE_FIGHTER;
    }
}
