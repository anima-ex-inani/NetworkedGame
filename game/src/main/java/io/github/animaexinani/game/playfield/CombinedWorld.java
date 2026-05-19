package io.github.animaexinani.game.playfield;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.input.GameAction;
import io.github.animaexinani.engine.input.GameInputListener;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.game.collision.BulletDamageContactListener;
import io.github.animaexinani.game.collision.ContactDamageContactListener;
import io.github.animaexinani.game.collision.EntityCollisionListener;
import io.github.animaexinani.game.nentities.Entity;
import io.github.animaexinani.game.nentities.EntitySnapshot;
import io.github.animaexinani.game.nentities.EntityType;
import io.github.animaexinani.game.nentities.PlayerShip;
import io.github.animaexinani.game.nentities.ScreenWrappable;

/**
 * A combined client and server-side representation of the game's playfield.
 * 
 * This class can be used to represent the game's playfield on both the client
 * and server sides,
 * deduplicating state when the same machine operates as both the client and the
 * server.
 */
public class CombinedWorld implements ClientPlayfield, ServerPlayfield {
    static final class EntityData {
        private final @NotNull Entity entity;
        private @Nullable Drawable drawable;

        // interpolation variables
        public float targetX = 0f;
        public float targetY = 0f;
        public float targetRotation = 0f;

        public EntityData(@NotNull Entity entity, @Nullable Drawable drawable) {
            this.entity = Objects.requireNonNull(entity);
            this.drawable = drawable;
        }
    }

    private enum ModificationType {
        SPAWN, DESPAWN
    }

    private record Modification(@NotNull ModificationType type, @Nullable Entity entity, @Nullable UUID id) {
    }

    /**
     * A builder for creating {@link CombinedWorld} instances.
     */
    public static final class Builder {
        private final @NotNull List<@NotNull Entity> entities = new ArrayList<>();
        private @Nullable UUID localPlayerId;
        private @Nullable SizeF size;
        private final @NotNull Map<@NotNull EntityType, @NotNull Function<@NotNull Entity, @Nullable Drawable>> visualFactories = new HashMap<>();

        /**
         * Adds an entity to the world.
         * 
         * @param entity The entity to add
         * @return This builder
         */
        public @NotNull Builder withEntity(@NotNull Entity entity) {
            this.entities.add(Objects.requireNonNull(entity));
            return this;
        }

        /**
         * Adds multiple entities to the world.
         * 
         * @param entities The entities to add
         * @return This builder
         */
        public @NotNull Builder withEntities(@NotNull Collection<@NotNull Entity> entities) {
            this.entities.addAll(Objects.requireNonNull(entities));
            return this;
        }

        /**
         * Sets the local player ID.
         * 
         * @param localPlayerId The ID of the local player entity
         * @return This builder
         */
        public @NotNull Builder withLocalPlayerId(@NotNull UUID localPlayerId) {
            this.localPlayerId = Objects.requireNonNull(localPlayerId);
            return this;
        }

        /**
         * Sets the size of the world.
         * 
         * @param size The world size
         * @return This builder
         */
        public @NotNull Builder withSize(@NotNull SizeF size) {
            this.size = Objects.requireNonNull(size);
            return this;
        }

        /**
         * Registers a factory to create visual representations for entities of a given
         * type.
         * 
         * @param type    The entity type
         * @param factory The factory function
         * @return This builder
         */
        public @NotNull Builder withVisualFactory(@NotNull EntityType type,
                @NotNull Function<@NotNull Entity, @Nullable Drawable> factory) {
            this.visualFactories.put(Objects.requireNonNull(type), Objects.requireNonNull(factory));
            return this;
        }

        /**
         * Builds the {@link CombinedWorld} instance.
         * 
         * @return The created CombinedWorld
         * @throws IllegalStateException if required fields are not set
         */
        public @NotNull CombinedWorld build() {
            if (this.localPlayerId == null) {
                throw new IllegalStateException("Local player ID must be set");
            }

            var world = new CombinedWorld(this.entities, this.localPlayerId, this.size);
            this.visualFactories.forEach(world::registerVisualFactory);
            this.entities.forEach(entity -> {
                var visualFactory = this.visualFactories.get(entity.type());
                if (visualFactory == null) {
                    return;
                }

                var visual = visualFactory.apply(entity);
                if (visual != null) {
                    world.registerVisuals(entity.id(), visual);
                }
            });
            return world;
        }
    }

    private final @NotNull Map<@NotNull UUID, @NotNull EntityData> entities;
    private @Nullable Collection<@NotNull Entity> cachedEntityCollection;
    private final @NotNull Map<@NotNull EntityType, @NotNull Function<@NotNull Entity, @Nullable Drawable>> visualFactories;
    private final @NotNull List<@NotNull Modification> deferredModifications;
    private final @NotNull UUID localPlayerId;
    private final @NotNull World<PhysicsBody> physicsWorld;
    private final @NotNull SizeF size;

    private CombinedWorld(@NotNull Collection<@NotNull Entity> playerEntities, @NotNull UUID localPlayerId,
            @NotNull SizeF size) {
        this.physicsWorld = new World<>();
        this.physicsWorld.setGravity(new Vector2(0.0, 0.0));
        this.physicsWorld.getSettings().setMaximumTranslation(150.0);

        this.physicsWorld.addCollisionListener(new EntityCollisionListener());
        this.physicsWorld.addContactListener(new ContactDamageContactListener());
        this.physicsWorld.addContactListener(new BulletDamageContactListener());

        this.entities = new HashMap<>();
        this.visualFactories = new HashMap<>();
        this.deferredModifications = new ArrayList<>();
        for (Entity entity : Objects.requireNonNull(playerEntities)) {
            Objects.requireNonNull(entity);
            if (this.entities.containsKey(entity.id())) {
                throw new IllegalArgumentException("Duplicate entity ID: " + entity.id());
            }
            this.entities.put(entity.id(), new EntityData(entity, null));
            entity.physicsBody().setUserData(entity);
            this.physicsWorld.addBody(entity.physicsBody());
        }

         if (!this.entities.containsKey(localPlayerId)) {
            throw new IllegalArgumentException("Local player ID not found in player entities");
        }
        this.size = size;
        this.localPlayerId = Objects.requireNonNull(localPlayerId);
        this.cachedEntityCollection = this.entities.values().stream().map(data -> data.entity).toList();
    }

    /**
     * Registers a factory to create visual representations for entities of a given
     * type when they are spawned.
     * 
     * @param type    The entity type to associate with the factory
     * @param factory The factory function that creates a drawable for an entity
     */
    public void registerVisualFactory(@NotNull EntityType type,
            @NotNull Function<@NotNull Entity, @Nullable Drawable> factory) {
        this.visualFactories.put(Objects.requireNonNull(type), Objects.requireNonNull(factory));
    }

    @Override
    public @NotNull Collection<@NotNull Entity> entities() {
        if (this.cachedEntityCollection != null) {
            return this.cachedEntityCollection;
        }

        synchronized (this.entities) {
            this.cachedEntityCollection = this.entities.values().stream().map((data) -> data.entity).toList();
            return this.cachedEntityCollection;
        }
    }

    @Override
    public @Nullable Entity getEntity(@NotNull UUID id) {
        synchronized (this.entities) {
            var data = this.entities.get(id);
            return data == null ? null : data.entity;
        }
    }

    @Override
    public @NotNull SizeF size() {
        return this.size;
    }

    /**
     * @implNote Spawning an entity with the same ID as an existing entity will
     *           replace the existing entity. Its visual representation will be
     *           removed from the scene.
     */
    @Override
    public boolean spawnEntity(@NotNull Entity entity) {
        synchronized (this.deferredModifications) {
            this.deferredModifications.add(new Modification(ModificationType.SPAWN, entity, null));
        }
        return true;
    }

    @Override
    public boolean despawnEntity(@NotNull UUID id) {
        synchronized (this.deferredModifications) {
            this.deferredModifications.add(new Modification(ModificationType.DESPAWN, null, id));
        }
        return true;
    }

    private void internalSpawnEntity(@NotNull Entity entity) {
        synchronized (this.entities) {
            var id = entity.id();
            var data = this.entities.get(id);

            var visualFactory = this.visualFactories.get(entity.type());
            Drawable visual = visualFactory == null ? null : visualFactory.apply(entity);

            if (data == null) {
                entity.physicsBody().setUserData(entity);
                this.physicsWorld.addBody(entity.physicsBody());
                this.entities.put(id, new EntityData(entity, visual));
                this.cachedEntityCollection = null;
                return;
            }

            if (data.entity == entity) {
                return;
            }

            this.physicsWorld.removeBody(data.entity.physicsBody());
            entity.physicsBody().setUserData(entity);
            this.physicsWorld.addBody(entity.physicsBody());
            this.entities.put(id, new EntityData(entity, visual));
            this.cachedEntityCollection = null;
        }
    }

    private void internalDespawnEntity(@NotNull UUID id) {
        synchronized (this.entities) {
            var data = this.entities.remove(id);
            if (data != null) {
                this.physicsWorld.removeBody(data.entity.physicsBody());
                this.cachedEntityCollection = null;
            }
        }
    }

    public void updateEntityTarget(EntitySnapshot snap) {
        synchronized (this.entities) {
            var data = this.entities.get(snap.id());
            if (data != null) {
                data.targetX = snap.x();
                data.targetY = snap.y();
                data.targetRotation = snap.rotation();
            }
        }
    }

    // A simple Lerp function
    float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    public void interpolateVisuals(float frameTime) {
        for (var entityData : this.entities.values()) {
            if (entityData.drawable instanceof Transformable t) {
                float currentX = t.translation().x();
                float currentY = t.translation().y();
                
                // snap threashold: if distance is massive, instantly teleport
                if (Math.abs(currentX - entityData.targetX) > 100 || Math.abs(currentY - entityData.targetY) > 100) {
                    t.translation(new PointF(entityData.targetX, entityData.targetY));
                } else {
                    // otherwise, smoothly glide
                    float newX = lerp(currentX, entityData.targetX, 0.3f);
                    float newY = lerp(currentY, entityData.targetY, 0.3f);
                    t.translation(new PointF(newX, newY));
                }
                t.rotation(entityData.targetRotation);
            }
        }
    }

    public void removeStaleEntities(Set<UUID> activeServerIds) {
        synchronized (this.entities) {
            // find all entity IDs in the client that are NOT in the server's list
            List<UUID> toRemove = this.entities.keySet().stream()
                .filter(id -> !activeServerIds.contains(id))
                .toList();

            // despawn them locally
            for (UUID id : toRemove) {
                this.internalDespawnEntity(id); 
            }
        }
    }

    @Override
    public @NotNull Entity localPlayer() {
        EntityData playerData;
        synchronized (this.entities) {
            playerData = this.entities.get(this.localPlayerId);
        }

        if (playerData == null) {
            throw new IllegalStateException("Local player not found");
        }

        return playerData.entity;
    }

    @Override
    public void render(Renderer renderer) {
        synchronized (this.entities) {
            for (var entityData : this.entities.values()) {
                if (entityData.drawable == null) {
                    continue;
                }

                renderer.draw(entityData.drawable);
            }
        }
    }

    @Override
    public boolean registerVisuals(@NotNull UUID entityId, @Nullable Drawable drawable) {
        synchronized (this.entities) {
            var data = this.entities.get(entityId);
            if (data == null) {
                return false;
            }

            var oldDrawable = data.drawable;
            data.drawable = drawable;
            return oldDrawable != drawable;
        }
    }

    @Override
    public boolean removeVisuals(@NotNull UUID entityId) {
        synchronized (this.entities) {
            var data = this.entities.get(entityId);
            if (data == null) {
                return false;
            }

            var oldDrawable = data.drawable;
            data.drawable = null;
            return oldDrawable != null;
        }
    }

    @Override
    public void preUpdate(Duration delta) {
        synchronized (this.deferredModifications) {
            for (var modification : this.deferredModifications) {
                switch (modification.type) {
                    case SPAWN -> this.internalSpawnEntity(Objects.requireNonNull(modification.entity));
                    case DESPAWN -> this.internalDespawnEntity(Objects.requireNonNull(modification.id));
                }
            }
            this.deferredModifications.clear();
        }

        synchronized (this.entities) {
            for (var entityData : this.entities.values()) {
                entityData.entity.preUpdate(delta);
            }
        }
    }

    @Override
    public void handleInput(@NotNull GameInputListener input, @NotNull Duration delta) {
        var player = this.localPlayer();
        if (player instanceof PlayerShip ship) {
            // Grab the keys currently held down on the local keyboard
            Set<GameAction> heldActions = input.getHeldActions();
            
            // Pass them to the ship
            ship.processActions(heldActions, this); 
        }
    }

    @Override
    public void update(Duration delta) {
        this.physicsWorld.update(delta.toNanos() / 1_000_000_000.0);

        List<UUID> toRemove = new ArrayList<>();
        synchronized (this.entities) {
            var bounds = this.size();
            for (var entityData : this.entities.values()) {
                if (entityData.entity instanceof ScreenWrappable wrappable) {
                    wrappable.wrapToScreen(bounds);
                }
            }

            for (var entityData : this.entities.values()) {
                entityData.entity.update(delta);
                if (!entityData.entity.active()) {
                    toRemove.add(entityData.entity.id());
                }
            }
        }
        
        for (UUID id : toRemove) {
            this.despawnEntity(id);
        }
    }

    @Override
    public void postUpdate(Duration delta) {
        synchronized (this.entities) {
            for (var entityData : this.entities.values()) {
                entityData.entity.postUpdate(delta);

                if (entityData.drawable instanceof Transformable transformable) {
                    var entityBody = entityData.entity.physicsBody();
                    var entityTransform = entityBody.getTransform();

                    var xPosition = entityTransform.getTranslationX();
                    var yPosition = entityTransform.getTranslationY();
                    var angle = entityTransform.getRotationAngle();

                    transformable.translation(new PointF((float) xPosition, (float) yPosition));
                    transformable.rotation((float) angle);
                }
            }
        }
    }
}