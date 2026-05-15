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
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.game.playfield.ServerPlayfield;
import io.github.animaexinani.game.util.UUIDGenerator;

public class BasicBullet implements Bullet {
    private UUID id;
    private final Body body;
    private Entity owner;
    private ServerPlayfield playfield;
    private Runnable onDespawn;
    private Duration timeAlive;
    private Duration maxLifetime;
    private boolean active;
    private final List<DamageDealtEventListener> damageDealtListeners = new CopyOnWriteArrayList<>();

    private static final Vector2[] LOCAL_COORDS = {
            new Vector2(5.0, 0.0),
            new Vector2(-2.0, 2.5),
            new Vector2(-2.0, -2.5)
    };

    public BasicBullet() {
        this.id = UUIDGenerator.generateV7Uuid();
        this.body = new Body();
        Polygon shape = Geometry.createPolygon(LOCAL_COORDS);
        BodyFixture fixture = this.body.addFixture(shape);
        fixture.setDensity(0.0001);
        this.body.setMass(MassType.NORMAL);
        this.body.setBullet(true); // Continuous collision detection
        this.active = false;
    }

    public void activate(@NotNull ServerPlayfield playfield, @NotNull Entity owner, double x, double y, double angle, double speed, @NotNull Runnable onDespawn) {
        this.id = UUIDGenerator.generateV7Uuid();
        this.playfield = playfield;
        this.owner = owner;
        this.onDespawn = onDespawn;
        this.timeAlive = Duration.ZERO;
        this.maxLifetime = Duration.ofSeconds(2); // 2 seconds
        this.active = true;

        this.body.getTransform().setTranslation(x, y);
        this.body.getTransform().setRotation(angle);

        this.body.setLinearVelocity(new Vector2(Math.cos(angle), Math.sin(angle)).multiply(speed));
        this.body.setAngularVelocity(0.0);
        this.body.setAtRest(false);
        this.damageDealtListeners.add((_, _, _, _) -> this.despawn());
    }

    public void reset() {
        this.active = false;
        this.body.setLinearVelocity(0, 0);
        this.body.setAngularVelocity(0);
        this.body.clearForce();
        this.body.clearTorque();
        this.damageDealtListeners.clear();
        this.owner = null;
        this.playfield = null;
        this.onDespawn = null;
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public EntityType type() {
        return EntityType.BULLET;
    }

    @Override
    public PhysicsBody physicsBody() {
        return this.body;
    }

    @Override
    public boolean active() {
        return this.active;
    }

    @Override
    public boolean ignoresCollisionWith(Entity entity) {
        // Bullet ignores its owner
        return entity == this.owner;
    }

    @Override
    public int damage() {
        return 10_000;
    }

    @Override
    public int minimumDamage() {
        return 1;
    }

    @Override
    public boolean dealsDamageTo(Damageable target) {
        // Bullet deals damage to anything that isn't its owner (if its owner is damageable)
        return target != this.owner;
    }
    
    @Override
    public boolean addDamageDealtListener(DamageDealtEventListener listener) {
        return this.damageDealtListeners.add(listener);
    }
    
    @Override
    public boolean removeDamageDealtListener(DamageDealtEventListener listener) {
        return this.damageDealtListeners.remove(listener);
    }
    
    @Override
    public void callDamageDealtListeners(Damageable target, int damage, boolean lethal) {
        for (var listener : this.damageDealtListeners) {
            listener.onDamageDealt(this, target, damage, lethal);
        }
    }

    @Override
    public @Nullable Entity owner() {
        return this.owner;
    }

    @Override
    public void update(Duration delta) {
        if (!this.active) {
            return;
        }

        this.timeAlive = this.timeAlive.plus(delta);
        if (this.timeAlive.compareTo(this.maxLifetime) >= 0) {
            this.despawn();
        }
    }

    private void despawn() {
        if (!this.active) {
            return;
        }
        if (this.playfield != null) {
            this.playfield.despawnEntity(this.id);
        }
        if (this.onDespawn != null) {
            this.onDespawn.run();
        }
        this.active = false;
    }
}
