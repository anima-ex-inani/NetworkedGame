package io.github.animaexinani.game.nentities;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.pool.BasicObjectPool;
import io.github.animaexinani.engine.pool.PooledObject;
import io.github.animaexinani.game.playfield.ServerPlayfield;
import io.github.animaexinani.game.util.UUIDGenerator;

public class PlayerShip implements Ship, ScreenWrappable {
    private final UUID id;
    private final Body body;
    private int health;
    private int shield;
    private final List<DamageTakenEventListener> damageTakenListeners;

    private Duration fireCooldown;
    private static final Duration FIRE_COOLDOWN_RATE = Duration.ofMillis(500);
    private final BasicObjectPool<BasicBullet> bulletPool;

    public static final double THRUST_POWER = 750.0;
    public static final double TURN_TORQUE = 1500.0;
    public static final double MAX_SPEED = 500.0;

    public static final @NotNull Collection<@NotNull PointF> LOCAL_COORDS = List.of(
            new PointF(30.0f, 0.0f),
            new PointF(-15.0f, 15.0f),
            new PointF(-15.0f, -15.0f));

    public PlayerShip() {
        this.id = UUIDGenerator.generateV7Uuid();
        this.damageTakenListeners = new CopyOnWriteArrayList<>();
        this.health = this.maxHealth();
        this.shield = this.maxShield();

        this.fireCooldown = Duration.ZERO;
        this.bulletPool = new BasicObjectPool<>(BasicBullet::reset, BasicBullet::new);

        this.body = new Body();

        Polygon shipShape = new Polygon(LOCAL_COORDS.stream().map(coord -> new Vector2(coord.x(), coord.y())).toArray(Vector2[]::new));
        BodyFixture fixture = this.body.addFixture(shipShape);
        fixture.setDensity(0.001);  // ship weight
        this.body.setMass(MassType.NORMAL);
        this.body.setLinearDamping(0.2);
        this.body.setAngularDamping(2.0);
    }

    @Override
    public int health() {
        return this.health;
    }

    public void health(int value) {
        this.health = StrictMath.clamp(value, 0, this.maxHealth());
    }

    @Override
    public int maxHealth() {
        return 100_000;
    }

    @Override
    public int shield() {
        return this.shield;
    }

    public void shield(int value) {
        this.shield = StrictMath.clamp(value, 0, this.maxShield());
    }

    @Override
    public int maxShield() {
        return 100_000;
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public EntityType type() {
        return EntityType.PLAYER;
    }

    @Override
    public PhysicsBody physicsBody() {
        return this.body;
    }

    @Override
    public boolean active() {
        return true;
    }

    @Override
    public boolean ignoresCollisionWith(Entity entity) {
        return entity.type().player();
    }

    @Override
    public int contactDamage() {
        return 10_000;
    }

    @Override
    public int minimumContactDamage() {
        return 1_000;
    }

    @Override
    public float contactDamageMultiplier(double impulse) {
        return 1.0f;
    }

    @Override
    public boolean dealsContactDamageTo(Damageable target) {
        var targetType = target.type();
        return !targetType.player();
    }

    @Override
    public void takeDamage(int damage) {
        int shieldDamage = StrictMath.min(this.shield, damage);
        this.shield -= shieldDamage;
        int healthDamage = damage - shieldDamage;
        this.health = StrictMath.max(0, this.health - healthDamage);
        boolean lethal = this.health == 0;

        for (var listener : this.damageTakenListeners) {
            listener.onDamageTaken(this, healthDamage, shieldDamage, lethal);
        }
    }

    @Override
    public boolean addDamageTakenListener(DamageTakenEventListener listener) {
        return this.damageTakenListeners.add(listener);
    }

    @Override
    public boolean removeDamageTakenListener(DamageTakenEventListener listener) {
        return this.damageTakenListeners.remove(listener);
    }

    @Override
    public void update(Duration delta) {
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) {
            this.fireCooldown = this.fireCooldown.minus(delta);
            if (this.fireCooldown.isNegative()) {
                this.fireCooldown = Duration.ZERO;
            }
        }

        Vector2 velocity = this.body.getLinearVelocity();
        if (velocity.getMagnitude() > MAX_SPEED) {
            velocity.normalize();
            velocity.multiply(MAX_SPEED);
            this.body.setLinearVelocity(velocity);
        }
    }

    @Override
    public void fireBullet(ServerPlayfield playfield) {
        if (!this.fireCooldown.isZero() && !this.fireCooldown.isNegative()) {
            return;
        }

        var transform = this.body.getTransform();
        double angle = transform.getRotationAngle();
        double x = transform.getTranslationX();
        double y = transform.getTranslationY();

        // Spawn bullet slightly in front of the ship
        double spawnX = x + (Math.cos(angle) * 35.0);
        double spawnY = y + (Math.sin(angle) * 35.0);

        PooledObject<BasicBullet> pooledBullet = this.bulletPool.acquire();
        BasicBullet bullet = pooledBullet.get();
        bullet.activate(playfield, this, spawnX, spawnY, angle, 1500.0, () -> this.bulletPool.release(pooledBullet));

        playfield.spawnEntity(bullet);
        this.fireCooldown = FIRE_COOLDOWN_RATE;
    }
}
