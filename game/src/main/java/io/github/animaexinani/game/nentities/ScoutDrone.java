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

public class ScoutDrone implements Ship, ScreenWrappable {
    private final UUID id;
    private final Body body;
    private int health;
    private int shield;
    
    private final List<DamageTakenEventListener> damageTakenListeners = new CopyOnWriteArrayList<>();
    private final List<ContactDamageDealtEventListener> contactDamageDealtListeners = new CopyOnWriteArrayList<>();

    // entity target for AI tracking
    private Entity target;
    private Duration fireCooldown = Duration.ZERO;
    private static final Duration FIRE_RATE = Duration.ofMillis(800);

    public ScoutDrone(double startX, double startY) {
        this.id = UUIDGenerator.generateV7Uuid();
        this.health = this.maxHealth();
        this.shield = this.maxShield();

        this.body = new Body();
        // create a simple circular hitbox for the drone
        BodyFixture fixture = this.body.addFixture(Geometry.createCircle(10.0));
        fixture.setDensity(0.0005); // lighter than player
        this.body.setMass(MassType.NORMAL);
        this.body.setLinearDamping(2.0);
        this.body.setAngularDamping(4.0);
        
        this.body.translate(startX, startY);
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    @Override
    public void update(Duration delta) {
        // AI Logic runs here every server tick
        if (this.target != null && this.target.active()) {
            // TODO: Calculate distance to target
            // TODO: If too close, apply negative thrust (back up)
            // TODO: If too far, apply positive thrust (move closer)
            // TODO: Rotate body to face target
        }

        // handle weapon cooldown
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) {
            this.fireCooldown = this.fireCooldown.minus(delta);
        }
    }

    @Override
    public void fireBullet(ServerPlayfield playfield) {
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) return;
        
        // TODO: acquire bullet from pool, set damage to 5, and spawn into playfield
        
        this.fireCooldown = FIRE_RATE;
    }

    // stats & interface implementations
    @Override public int health() { return this.health; }
    @Override public int maxHealth() { return 20; }
    @Override public int shield() { return this.shield; }
    @Override public int maxShield() { return 0; }
    @Override public UUID id() { return this.id; }
    @Override public EntityType type() { return EntityType.SCOUT_DRONE; }
    @Override public PhysicsBody physicsBody() { return this.body; }
    @Override public boolean active() { return true; }
    @Override public boolean ignoresCollisionWith(Entity entity) { return entity.type().enemy(); } // Ignore other enemies
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

    // boilerplate listener add/remove methods omitted for simplicity (same as PlayerShip)
    @Override public boolean addDamageTakenListener(DamageTakenEventListener listener) { return this.damageTakenListeners.add(listener); }
    @Override public boolean removeDamageTakenListener(DamageTakenEventListener listener) { return this.damageTakenListeners.remove(listener); }
    @Override public boolean addContactDamageDealtListener(ContactDamageDealtEventListener listener) { return this.contactDamageDealtListeners.add(listener); }
    @Override public boolean removeContactDamageDealtListener(ContactDamageDealtEventListener listener) { return this.contactDamageDealtListeners.remove(listener); }
    @Override public void callContactDamageDealtListeners(Damageable target, int damage, boolean lethal, double impulse) {
        for (var listener : this.contactDamageDealtListeners) listener.onContactDamageDealt(this, target, damage, lethal, impulse);
    }
}