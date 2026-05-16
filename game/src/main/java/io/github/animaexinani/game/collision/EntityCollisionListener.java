package io.github.animaexinani.game.collision;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.world.BroadphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;

import io.github.animaexinani.game.nentities.Entity;

public class EntityCollisionListener extends CollisionListenerAdapter<PhysicsBody, BodyFixture> {

    @Override
    public boolean collision(BroadphaseCollisionData<PhysicsBody, BodyFixture> collision) {
        PhysicsBody body1 = collision.getBody1();
        PhysicsBody body2 = collision.getBody2();

        Object data1 = body1.getUserData();
        Object data2 = body2.getUserData();

        if (data1 instanceof Entity entity1 && data2 instanceof Entity entity2) {
            return this.shouldCollide(entity1, entity2);
        }

        return true;
    }

    private boolean shouldCollide(Entity e1, Entity e2) {
        return !e1.ignoresCollisionWith(e2) && !e2.ignoresCollisionWith(e1);
    }
}
