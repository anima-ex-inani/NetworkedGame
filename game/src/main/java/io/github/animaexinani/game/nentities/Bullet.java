package io.github.animaexinani.game.nentities;

/**
 * Represents a bullet within the game's playfield
 */
public interface Bullet extends Entity {
    /**
     * Gets the owner of this bullet
     * @return The bullet's owner
     */
    Entity owner();

    /**
     * Gets the damage of this bullet
     * @return The bullet's damage
     */
    int damage();
}
