package io.github.animaexinani.game.nentities;

import java.time.Duration;
import java.util.UUID;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.PhysicsBody;

/**
 * A placeholder entity used by the server
 */
public final class ServerNetworkEntity implements Entity {

    private final UUID id;
    private final EntityType type;
    private final Body dummyBody;

    public ServerNetworkEntity(UUID id, EntityType type) {
        this.id = id;
        this.type = type;
        this.dummyBody = new Body();
    }

    @Override public UUID id()           { return this.id; }
    @Override public EntityType type()   { return this.type; }
    @Override public PhysicsBody physicsBody() { return this.dummyBody; }
    @Override public boolean active()    { return false; }
    @Override public boolean ignoresCollisionWith(Entity entity) { return true; }
    @Override public void update(Duration deltaTime) {}
}