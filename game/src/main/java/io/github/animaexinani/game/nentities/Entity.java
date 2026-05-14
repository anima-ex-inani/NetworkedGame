package io.github.animaexinani.game.nentities;

import java.util.Set;
import java.util.UUID;

import org.dyn4j.dynamics.PhysicsBody;

import io.github.animaexinani.game.util.Updatable;

/**
 * An entity within the game's playfield.
 */
public interface Entity extends Updatable {
    /**
     * An identifier used to track the entity between clients.
     * 
     * @return The entity's UUID
     */
    UUID id();

    /**
     * The type of entity this is.
     * 
     * @return The entity's type
     */
    EntityType type();

    /**
     * The physics body associated with this entity
     * 
     * @return The entity's physics body
     */
    PhysicsBody physicsBody();

    /**
     * Whether the entity is interacting with other entities in the playfield
     * 
     * @return <code>true</code> if the entity is active, <code>false</code>
     *         otherwise
     */
    boolean active();

    /**
     * Whether this entity should ignore collisions with the given entity.
     * 
     * @param entity The entity to check
     * @return <code>true</code> if the entity should ignore collisions with the
     *         given entity, <code>false</code> otherwise
     */
    boolean ignoresCollisionWith(Entity entity);
}
