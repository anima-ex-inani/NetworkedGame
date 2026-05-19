package io.github.animaexinani.game.nentities;

import java.time.Duration;
import java.util.UUID;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.PhysicsBody;

import io.github.animaexinani.engine.color.Color;

public class ClientNetworkEntity implements LivingEntity {
    private final UUID id;
    private final EntityType type;
    private final Body dummyBody;

    private int health = 100_000;
    private int shield = 100_000;
    
    // flashing State
    public float flashTimer = 0f;
    public Color flashColor = null;

    public ClientNetworkEntity(UUID id, EntityType type) {
        this.id = id;
        this.type = type;
        this.dummyBody = new Body(); 
    }

    // a helper method the visual factories will use
    public Color getVisualColor(Color defaultColor) {
        if (this.flashTimer > 0 && this.flashColor != null) {
            return this.flashColor;
        }
        return defaultColor;
    }

    public void setHealth(int h) { this.health = h; }
    public void setShield(int s) { this.shield = s; }
    
    @Override public int health() { return this.health; }
    @Override public int shield() { return this.shield; }
    @Override public int maxHealth() { return 100_000; }
    @Override public int maxShield() { return 100_000; }
    
    @Override public UUID id() { return id; }
    @Override public EntityType type() { return type; }
    @Override public PhysicsBody physicsBody() { return dummyBody; }
    @Override public boolean active() { return true; }
    @Override public boolean ignoresCollisionWith(Entity entity) { return true; }
    @Override public void update(Duration delta) {}
    @Override public void preUpdate(Duration delta) {}
    @Override public void postUpdate(Duration delta) {}
}