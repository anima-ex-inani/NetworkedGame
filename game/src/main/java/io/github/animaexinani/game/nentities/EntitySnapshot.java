package io.github.animaexinani.game.nentities;
import java.util.UUID;

// a lean representation of an entity at a specific moment in time
public class EntitySnapshot {
    public final UUID id;
    public final EntityType type;
    public final float x;
    public final float y;
    public final float rotation;
    public final int health;

    public EntitySnapshot(UUID id, EntityType type, float x, float y, float rotation, int health) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.health = health;
    }
}
