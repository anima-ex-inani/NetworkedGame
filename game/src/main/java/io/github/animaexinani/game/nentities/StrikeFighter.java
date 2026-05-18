package io.github.animaexinani.game.nentities;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import io.github.animaexinani.game.playfield.ServerPlayfield;
import io.github.animaexinani.game.util.UUIDGenerator;

public class StrikeFighter implements Ship, ScreenWrappable {
    private final UUID id;
    private final Body body;
    private int health;
    private int shield;
    
    private final List<DamageTakenEventListener> damageTakenListeners = new CopyOnWriteArrayList<>();
    private final List<ContactDamageDealtEventListener> contactDamageDealtListeners = new CopyOnWriteArrayList<>();

    private Duration fireCooldown = Duration.ZERO;
    private static final Duration FIRE_RATE = Duration.ofMillis(400);

    public StrikeFighter(double startX, double startY) {
        this.id = UUIDGenerator.generateV7Uuid();
        this.health = this.maxHealth();
        this.shield = this.maxShield();

        this.body = new Body();
        // triangle hitbox
        BodyFixture fixture = this.body.addFixture(Geometry.createPolygon(
            new org.dyn4j.geometry.Vector2(15.0, 0.0),
            new org.dyn4j.geometry.Vector2(-10.0, 10.0),
            new org.dyn4j.geometry.Vector2(-10.0, -10.0)
        ));
        fixture.setDensity(0.001); 
        this.body.setMass(MassType.NORMAL);
        this.body.setLinearDamping(1.0); // less damping, slides more and feels like a plane
        this.body.setAngularDamping(2.0);
        
        this.body.translate(startX, startY);
    }

    @Override
    public void update(Duration delta) {
        // AI Logic runs here
        // TODO: apply constant forward thrust
        // TODO: slowly apply torque to circle the center of the playfield
        // TODO: check distance forward to see if a player is in the cone of vision to shoot
        
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) {
            this.fireCooldown = this.fireCooldown.minus(delta);
        }
    }

    @Override
    public void fireBullet(ServerPlayfield playfield) {
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) return;
        
        // TODO: acquire bullet from pool, set damage to 10, and spawn into playfield
        
        this.fireCooldown = FIRE_RATE;
    }

    // stats & interface implementations
    @Override public int health() { return this.health; }
    @Override public int maxHealth() { return 60; }
    @Override public int shield() { return this.shield; }
    @Override public int maxShield() { return 0; }
    @Override public UUID id() { return this.id; }
    @Override public EntityType type() { return EntityType.STRIKE_FIGHTER; }
    @Override public PhysicsBody physicsBody() { return this.body; }
    @Override public boolean active() { return true; }
    @Override public boolean ignoresCollisionWith(Entity entity) { return entity.type().enemy(); } 
    @Override public int contactDamage() { return 10; }
    @Override public int minimumContactDamage() { return 5; }
    @Override public float contactDamageMultiplier(double impulse) { return 1.0f; }
    @Override public boolean dealsContactDamageTo(Damageable target) { return target.type().player(); }

    @Override
    public boolean takeDamage(int damage) {
        if (damage <= 0) return false;
        this.health = StrictMath.max(0, this.health - damage);
        boolean lethal = this.health == 0;
        for (var listener : this.damageTakenListeners) {
            listener.onDamageTaken(this, damage, 0, lethal);
        }
        return lethal;
    }

    @Override public boolean addDamageTakenListener(DamageTakenEventListener listener) { return this.damageTakenListeners.add(listener); }
    @Override public boolean removeDamageTakenListener(DamageTakenEventListener listener) { return this.damageTakenListeners.remove(listener); }
    @Override public boolean addContactDamageDealtListener(ContactDamageDealtEventListener listener) { return this.contactDamageDealtListeners.add(listener); }
    @Override public boolean removeContactDamageDealtListener(ContactDamageDealtEventListener listener) { return this.contactDamageDealtListeners.remove(listener); }
    @Override public void callContactDamageDealtListeners(Damageable target, int damage, boolean lethal, double impulse) {
        for (var listener : this.contactDamageDealtListeners) listener.onContactDamageDealt(this, target, damage, lethal, impulse);
    }
}