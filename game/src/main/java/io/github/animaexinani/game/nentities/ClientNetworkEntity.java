package io.github.animaexinani.game.nentities;

import java.time.Duration;
import java.util.UUID;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.PhysicsBody;

/**
 * A lightweight, "dummy" entity used purely by the Client to hold 
 * an ID and a Type so the renderer has something to draw.
 */
public class ClientNetworkEntity implements Entity {
    private final UUID id;
    private final EntityType type;
    private final Body dummyBody;

    public ClientNetworkEntity(UUID id, EntityType type) {
        this.id = id;
        this.type = type;
        this.dummyBody = new Body(); // blank body just for graphics to hook onto
    }

    @Override public UUID id() { return id; }
    @Override public EntityType type() { return type; }
    @Override public PhysicsBody physicsBody() { return dummyBody; }
    
    @Override public boolean active() { return true; }
    @Override public boolean ignoresCollisionWith(Entity entity) { return true; }
    
    // the client doesn't run physics, so these do nothing
    @Override public void update(Duration delta) {}
    @Override public void preUpdate(Duration delta) {}
    @Override public void postUpdate(Duration delta) {}
}