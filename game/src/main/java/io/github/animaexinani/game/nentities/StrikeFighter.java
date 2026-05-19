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

    // state machine
    private enum AIState { APPROACHING, STRAFING, RETREATING }
    private AIState currentState = AIState.APPROACHING;
    private int burstShotsFired = 0;
    private Duration stateTimer = Duration.ZERO;

    private Duration fireCooldown = Duration.ZERO;
    private static final Duration FIRE_RATE = Duration.ofMillis(100);

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

    private void retarget() {
        // Scan the playfield for any active player ship
        for (Entity entity : this.playfield.entities()) {
            if (entity.type().player()) {
                this.target = entity;
                this.currentState = AIState.APPROACHING; // Reset state for the new target
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

        double currentAngle = this.body.getTransform().getRotationAngle();
        var myPos = this.body.getTransform().getTranslation();

        // constant forward thrust
        double thrust = 500.0;
        this.body.applyForce(new org.dyn4j.geometry.Vector2(Math.cos(currentAngle) * thrust, Math.sin(currentAngle) * thrust));

        // Speed limit
        org.dyn4j.geometry.Vector2 velocity = this.body.getLinearVelocity();
        double maxSpeed = 700.0; 
        if (velocity.getMagnitude() > maxSpeed) {
            velocity.normalize();
            velocity.multiply(maxSpeed);
            this.body.setLinearVelocity(velocity);
        }

        // state machine AI
        if (this.target != null && this.target.active()) {
            var targetPos = this.target.physicsBody().getTransform().getTranslation();
            double dx = targetPos.x - myPos.x;
            double dy = targetPos.y - myPos.y;
            double distanceToTarget = Math.hypot(dx, dy);
            double angleToTarget = Math.atan2(dy, dx);

            switch (this.currentState) {
                case APPROACHING:
                    // turn towards player
                    double angleDiff = angleToTarget - currentAngle;
                    angleDiff = Math.atan2(Math.sin(angleDiff), Math.cos(angleDiff)); 
                    this.body.setAngularVelocity(angleDiff * 2.5);

                    // if close enough, start the strafing run
                    if (distanceToTarget < 600) {
                        this.currentState = AIState.STRAFING;
                        this.burstShotsFired = 0;
                    }
                    break;

                case STRAFING:
                    // stop turning to keep flying in a straight line during the gun run
                    this.body.setAngularVelocity(0);

                    // fire rapidly (fireBullet automatically respects the 75ms FIRE_RATE cooldown)
                    if (this.fireCooldown.isZero() || this.fireCooldown.isNegative()) {
                        this.fireBullet(this.playfield);
                        this.burstShotsFired++;
                    }

                    // Once 5 shots are fired, bail out
                    if (this.burstShotsFired >= 5) {
                        this.currentState = AIState.RETREATING;
                        this.stateTimer = Duration.ofSeconds(2); // fly away for 2 seconds
                    }
                    break;

                case RETREATING:
                    // calculate the angle pointing directly AWAY from the player
                    double retreatAngle = angleToTarget + Math.PI; 
                    double retreatDiff = retreatAngle - currentAngle;
                    retreatDiff = Math.atan2(Math.sin(retreatDiff), Math.cos(retreatDiff)); 
                    
                    // turn away
                    this.body.setAngularVelocity(retreatDiff * 2.0);

                    // count down the retreat timer
                    this.stateTimer = this.stateTimer.minus(delta);
                    if (this.stateTimer.isZero() || this.stateTimer.isNegative()) {
                        this.currentState = AIState.APPROACHING; // go back in for another pass
                    }
                    break;
            }
        }

        // decrement fire cooldown
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
    @Override public int contactDamage() { return 25_000; }
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