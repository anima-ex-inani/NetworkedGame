package io.github.animaexinani.game.nentities;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.jetbrains.annotations.NotNull;

import io.github.animaexinani.engine.point.PointF;

public class Asteroid implements Damageable, DealsContactDamage, ScreenWrappable {
    private static final Collection<PointF> ASTEROID_LOCAL_POINTS = List.of(
            new PointF(20.0f, 0.0f),
            new PointF(10.0f, 15.0f),
            new PointF(-10.0f, 15.0f),
            new PointF(-20.0f, 0.0f),
            new PointF(-10.0f, -15.0f),
            new PointF(10.0f, -15.0f));

    public static Collection<@NotNull PointF> getAsteroidLocalPointsForType(EntityType type) {
        return switch (type) {
            case EntityType.ASTEROID -> ASTEROID_LOCAL_POINTS;
            default -> throw new IllegalArgumentException("Invalid asteroid type");
        };
    }

    private int health = 500_000;
    private final UUID id;
    private final EntityType type;
    private final Body physicsBody;
    private final List<DamageTakenEventListener> listeners = new CopyOnWriteArrayList<>();
    private final List<ContactDamageDealtEventListener> contactDamageDealtListeners = new CopyOnWriteArrayList<>();

    private static final Map<EntityType, Supplier<Body>> ASTEROID_BODY_CREATORS = Map.of(
        EntityType.ASTEROID, () -> {
            var body = new Body();
            Polygon shape = Geometry.createPolygon(getAsteroidLocalPointsForType(EntityType.ASTEROID).stream()
                    .map(p -> new Vector2(p.x(), p.y())).toArray(Vector2[]::new));
            BodyFixture fixture = body.addFixture(shape);
            fixture.setDensity(0.005);

            body.setMass(MassType.NORMAL);
            body.setLinearDamping(0.0);
            body.setAngularDamping(0.0);

            return body;
        }
    );

    public Asteroid(EntityType asteroidType, float startX, float startY, double velocityX, double velocityY) {
        if (!asteroidType.asteroid()) {
            throw new IllegalArgumentException("Asteroid type must be an asteroid");
        }

        this.type = asteroidType;
        this.id = UUID.randomUUID();
        this.health = switch (asteroidType) {
            case EntityType.ASTEROID -> 50_000;
            default -> 1;
        };

        this.physicsBody = ASTEROID_BODY_CREATORS.get(asteroidType).get();
        this.physicsBody.getTransform().setTranslation(startX, startY);
        this.physicsBody.setLinearVelocity(velocityX, velocityY);
        this.physicsBody.setAngularVelocity(Math.random() * 2.0 - 1.0);
        this.physicsBody.setUserData(this);
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public EntityType type() {
        return this.type;
    }

    @Override
    public PhysicsBody physicsBody() {
        return this.physicsBody;
    }

    @Override
    public boolean active() {
        return this.health > 0;
    }

    @Override
    public boolean ignoresCollisionWith(Entity entity) {
        var entityType = entity.type();
        return entityType.asteroid() || entityType.enemy();
    }

    @Override
    public int contactDamage() {
        switch (this.type) {
            case EntityType.ASTEROID:
                return 5_000;
            default:
                return 1;
        }
    }

    @Override
    public int minimumContactDamage() {
        return 1;
    }

    @Override
    public float contactDamageMultiplier(double impulse) {
        return 1.0f;
    }

    @Override
    public boolean dealsContactDamageTo(Damageable target) {
        return target.type().player();
    }

    @Override
    public void takeDamage(int damage) {
        if (damage <= 0) {
            return;
        }

        int healthDamage = StrictMath.min(damage, this.health);
        this.health -= healthDamage;
        boolean lethal = this.health <= 0;

        for (var listener : this.listeners) {
            listener.onDamageTaken(this, healthDamage, 0, lethal);
        }
    }

    @Override
    public boolean addDamageTakenListener(DamageTakenEventListener listener) {
        return this.listeners.add(listener);
    }

    @Override
    public boolean removeDamageTakenListener(DamageTakenEventListener listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public boolean addContactDamageDealtListener(ContactDamageDealtEventListener listener) {
        return this.contactDamageDealtListeners.add(listener);
    }

    @Override
    public boolean removeContactDamageDealtListener(ContactDamageDealtEventListener listener) {
        return this.contactDamageDealtListeners.remove(listener);
    }
}
