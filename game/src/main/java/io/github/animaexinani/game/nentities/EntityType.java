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
}
