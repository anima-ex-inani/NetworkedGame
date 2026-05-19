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

    private final ServerPlayfield playfield;
    private final io.github.animaexinani.engine.pool.BasicObjectPool<BasicBullet> bulletPool;

    public ScoutDrone(double startX, double startY, ServerPlayfield playfield) {
        this.playfield = playfield;
        this.id = UUIDGenerator.generateV7Uuid();
        this.health = this.maxHealth();
        this.shield = this.maxShield();

        this.body = new Body();
        this.bulletPool = new io.github.animaexinani.engine.pool.BasicObjectPool<>(BasicBullet::reset, BasicBullet::new);
        this.body.translate(startX, startY);
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

    private void retarget() {
        // Scan the playfield for any active player ship
        for (Entity entity : this.playfield.entities()) {
            if (entity.type().player()) { // Assuming type().player() returns true for players
                this.target = entity;
                break;
            }
        }
    }

    @Override
    public void update(Duration delta) {
        if (this.target != null && this.playfield.getEntity(this.target.id()) == null) {
            this.target = null;
        }

        if (this.target == null) {
            this.retarget();
        }

        if (this.target != null && this.target.active()) {
            var targetPos = this.target.physicsBody().getTransform().getTranslation();
            var myPos = this.body.getTransform().getTranslation();

            double dx = targetPos.x - myPos.x;
            double dy = targetPos.y - myPos.y;
            double distance = Math.hypot(dx, dy);
            
            // look at the target
            double targetAngle = Math.atan2(dy, dx);
            double currentAngle = this.body.getTransform().getRotationAngle();
            
            // normalize the angle difference to find the shortest turning direction
            double angleDiff = targetAngle - currentAngle;
            angleDiff = Math.atan2(Math.sin(angleDiff), Math.cos(angleDiff)); 
            
            // turn smoothly towards target
            this.body.setAngularVelocity(angleDiff * 3.0); 

            // maintain distance
            double thrust = 300.0; // drone speed
            if (distance > 350) {
                // too far, fly forward.
                this.body.applyForce(new org.dyn4j.geometry.Vector2(Math.cos(currentAngle) * thrust, Math.sin(currentAngle) * thrust));
            } else if (distance < 250) {
                // too close, fly backward.
                this.body.applyForce(new org.dyn4j.geometry.Vector2(-Math.cos(currentAngle) * thrust, -Math.sin(currentAngle) * thrust));
            }

            // shoot if pointing generally at the player (within ~11 degrees)
            if (Math.abs(angleDiff) < 0.2 && distance < 600) {
                this.fireBullet(this.playfield);
            }
        } else {
            // stop spinning if the target dies
            this.body.setAngularVelocity(0);
        }

        // handle cooldown
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) {
            this.fireCooldown = this.fireCooldown.minus(delta);
        }
    }

    @Override
    public void fireBullet(ServerPlayfield playfield) {
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) return;
        
        var transform = this.body.getTransform();
        double angle = transform.getRotationAngle();
        double x = transform.getTranslationX();
        double y = transform.getTranslationY();

        double spawnX = x + (Math.cos(angle) * 15.0);
        double spawnY = y + (Math.sin(angle) * 15.0);

        var pooledBullet = this.bulletPool.acquire();
        BasicBullet bullet = pooledBullet.get();
        // slower bullet (800 speed) that deals 5k damage
        bullet.activate(playfield, this, spawnX, spawnY, angle, 800.0, () -> this.bulletPool.release(pooledBullet));
        bullet.damage(5_000); 

        playfield.spawnEntity(bullet);
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
    @Override public int contactDamage() { return 25_000; }
    @Override public int minimumContactDamage() { return 10_000; }
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