package io.github.animaexinani.game.playfield;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.Renderer;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.transformable.Transformable;
import io.github.animaexinani.game.collision.EntityCollisionListener;
import io.github.animaexinani.game.collision.ContactDamageContactListener;
import io.github.animaexinani.game.nentities.Entity;

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

        public EntityData(@NotNull Entity entity, @Nullable Drawable drawable) {
            this.entity = Objects.requireNonNull(entity);
            this.drawable = drawable;
        }
    }

    private final @NotNull Map<@NotNull UUID, @NotNull EntityData> entities;
    private @Nullable Collection<@NotNull Entity> cachedEntityCollection;
    private final @NotNull UUID localPlayerId;
    private final @NotNull World<PhysicsBody> physicsWorld;

    public CombinedWorld(@NotNull Collection<@NotNull Entity> playerEntities, @NotNull UUID localPlayerId) {
        this.physicsWorld = new World<>();
        this.physicsWorld.setGravity(new Vector2(0.0, 0.0));
        this.physicsWorld.getSettings().setMaximumTranslation(150.0);

        this.physicsWorld.addCollisionListener(new EntityCollisionListener());
        this.physicsWorld.addContactListener(new ContactDamageContactListener());

        this.entities = new HashMap<>();
        for (Entity entity : Objects.requireNonNull(playerEntities)) {
            Objects.requireNonNull(entity);
            this.entities.put(entity.id(), new EntityData(entity, null));
            entity.physicsBody().setUserData(entity);
            this.physicsWorld.addBody(entity.physicsBody());
        }

        if (!this.entities.containsKey(localPlayerId)) {
            throw new IllegalArgumentException("Local player ID not found in player entities");
        }
        this.localPlayerId = Objects.requireNonNull(localPlayerId);
        this.cachedEntityCollection = playerEntities;
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

    /**
     * @implNote Spawning an entity with the same ID as an existing entity will
     *           replace the existing entity. Its visual representation will be
     *           removed from the scene.
     */
    @Override
    public boolean spawnEntity(@NotNull Entity entity) {
        synchronized (this.entities) {
            var id = entity.id();
            var data = this.entities.get(id);
            if (data == null) {
                entity.physicsBody().setUserData(entity);
                this.physicsWorld.addBody(entity.physicsBody());
                this.entities.put(id, new EntityData(entity, null));
                this.cachedEntityCollection = null;
                return true;
            }

            if (data.entity == entity) {
                return false;
            }

            this.physicsWorld.removeBody(data.entity.physicsBody());
            entity.physicsBody().setUserData(entity);
            this.physicsWorld.addBody(entity.physicsBody());
            this.entities.put(id, new EntityData(entity, null));
            this.cachedEntityCollection = null;
            return true;
        }
    }

    @Override
    public boolean despawnEntity(@NotNull UUID id) {
        synchronized (this.entities) {
            var data = this.entities.remove(id);
            if (data != null) {
                this.physicsWorld.removeBody(data.entity.physicsBody());
                this.cachedEntityCollection = null;
                return true;
            }
            return false;
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
    public void update(Duration delta) {
        this.physicsWorld.update(delta.toMillis() / 1000.0);

        synchronized (this.entities) {
            List<UUID> deadEntities = new ArrayList<>();

            for (var entityData : this.entities.values()) {
                entityData.entity.update(delta);
                if (!entityData.entity.active()) {
                    deadEntities.add(entityData.entity.id());
                }
            }

            for (UUID id : deadEntities) {
                var data = this.entities.remove(id);
                if (data != null) {
                    this.physicsWorld.removeBody(data.entity.physicsBody());
                    this.cachedEntityCollection = null;
                }
            }
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
