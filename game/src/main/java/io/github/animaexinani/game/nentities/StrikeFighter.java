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

    private final ServerPlayfield playfield;
    private final io.github.animaexinani.engine.pool.BasicObjectPool<BasicBullet> bulletPool;

    public StrikeFighter(double startX, double startY, ServerPlayfield playfield) {
        this.playfield = playfield;
        this.id = UUIDGenerator.generateV7Uuid();
        this.health = this.maxHealth();
        this.shield = this.maxShield();

        this.body = new Body();
        this.bulletPool = new io.github.animaexinani.engine.pool.BasicObjectPool<>(BasicBullet::reset, BasicBullet::new);
        this.body.translate(startX, startY);
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

    private Entity target;
    public void setTarget(Entity target) { this.target = target; }

    @Override
    public void update(Duration delta) {
        double currentAngle = this.body.getTransform().getRotationAngle();
        var myPos = this.body.getTransform().getTranslation();

        // constant forward thrust fly like a plane
        double thrust = 500.0;
        this.body.applyForce(new org.dyn4j.geometry.Vector2(Math.cos(currentAngle) * thrust, Math.sin(currentAngle) * thrust));

        org.dyn4j.geometry.Vector2 velocity = this.body.getLinearVelocity();
        double maxSpeed = 450.0; // keeps it fast, but well below the 1200 bullet speed
        if (velocity.getMagnitude() > maxSpeed) {
            velocity.normalize();
            velocity.multiply(maxSpeed);
            this.body.setLinearVelocity(velocity);
        }

        // slowly orbit the center of the screen (960, 540)
        double angleToCenter = Math.atan2(540.0 - myPos.y, 960.0 - myPos.x);
        // by targeting 90 degrees offset from the center, the ship flies in a circle!
        double targetAngle = angleToCenter + (Math.PI / 2.0); 
        
        double angleDiff = targetAngle - currentAngle;
        angleDiff = Math.atan2(Math.sin(angleDiff), Math.cos(angleDiff)); 
        
        this.body.setAngularVelocity(angleDiff * 1.5); // turns much slower than the drone

        // attacking with cone of vision check
        if (this.target != null && this.target.active()) {
            var targetPos = this.target.physicsBody().getTransform().getTranslation();
            double dx = targetPos.x - myPos.x;
            double dy = targetPos.y - myPos.y;
            double angleToTarget = Math.atan2(dy, dx);
            double distToTarget = Math.hypot(dx, dy);
            
            double targetAngleDiff = angleToTarget - currentAngle;
            targetAngleDiff = Math.atan2(Math.sin(targetAngleDiff), Math.cos(targetAngleDiff)); 
            
            // if the player is within a ~30 degree cone in front of the ship, open fire!
            if (Math.abs(targetAngleDiff) < 0.5 && distToTarget < 600) {
                this.fireBullet(this.playfield);
            }
        }

        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) {
            this.fireCooldown = this.fireCooldown.minus(delta);
        }
    }

    @Override
    public void fireBullet(ServerPlayfield playfield) {
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) return;
        
        var transform = this.body.getTransform();
        double angle = transform.getRotationAngle();
        
        double spawnX = transform.getTranslationX() + (Math.cos(angle) * 20.0);
        double spawnY = transform.getTranslationY() + (Math.sin(angle) * 20.0);

        var pooledBullet = this.bulletPool.acquire();
        BasicBullet bullet = pooledBullet.get();
        // faster bullet (1200 speed) that deals 10k damage
        bullet.activate(playfield, this, spawnX, spawnY, angle, 1200.0, () -> this.bulletPool.release(pooledBullet));
        bullet.damage(10_000); 

        playfield.spawnEntity(bullet);
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
    @Override public int contactDamage() { return 40_000; }
    @Override public int minimumContactDamage() { return 15_000; }
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