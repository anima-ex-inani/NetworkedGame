package io.github.animaexinani.game.collision;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.world.NarrowphaseCollisionData;
import org.dyn4j.world.listener.CollisionListenerAdapter;

import io.github.animaexinani.game.classes.Asteroid;
import io.github.animaexinani.game.classes.Bullet;
import io.github.animaexinani.game.classes.Ship;

public class GameCollisionListener extends CollisionListenerAdapter<Body, BodyFixture> {
    
    @Override
    public boolean collision(NarrowphaseCollisionData<Body, BodyFixture> collision) {
        Body body1 = collision.getBody1();
        Body body2 = collision.getBody2();

        Object data1 = body1.getUserData();
        Object data2 = body2.getUserData();

        // Ship vs Asteroid
        if (data1 instanceof Ship && data2 instanceof Asteroid) {
            ((Ship) data1).takeDamage(((Asteroid) data2).getCollisionDamage());
        } else if (data2 instanceof Ship && data1 instanceof Asteroid) {
            ((Ship) data2).takeDamage(((Asteroid) data1).getCollisionDamage());
        }

        // Bullet vs Asteroid
        if (data1 instanceof Bullet && data2 instanceof Asteroid) {
            ((Bullet) data1).takeDamage(1);
            ((Asteroid) data2).takeDamage(((Bullet) data1).getDamage());
        } else if (data2 instanceof Bullet && data1 instanceof Asteroid) {
            ((Bullet) data2).takeDamage(1);
            ((Asteroid) data1).takeDamage(((Bullet) data2).getDamage());
        }

        // Ship vs Bullet
        if (data1 instanceof Ship && data2 instanceof Bullet) {
            ((Ship) data1).takeDamage(((Bullet) data2).getDamage());
            ((Bullet) data2).takeDamage(1);   // destroy the bullet
        } else if (data2 instanceof Ship && data1 instanceof Bullet) {
            ((Ship) data2).takeDamage(((Bullet) data1).getDamage());
            ((Bullet) data1).takeDamage(1);
        }

        return true;
    }
}