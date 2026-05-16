package io.github.animaexinani.game.entities;

import java.util.ArrayDeque;
import java.util.Queue;

import org.dyn4j.geometry.Vector2;

public class BulletPool {
    private final Queue<Bullet> available = new ArrayDeque<>();
    private final int capacity = 100;

    public BulletPool() {
        // pre-fill the pool with 100 dead bullets
        for (int i = 0; i < this.capacity; i++) {
            this.available.offer(new Bullet());
        }
    }

    // the Ship calls this to fire
    public Bullet obtain(double x, double y, double angle, double speed) {
        Bullet b = this.available.poll();
        
        // if the pool is empty, just make a new one
        if (b == null) {
            b = new Bullet(); 
        }
        
        b.activate(x, y, angle, speed);
        return b;
    }

    // the GameWorld calls this when a bullet dies to put it back in the pool
    public void recycle(Bullet b) {
        // clear momentum only
        b.getBody().setLinearVelocity(new Vector2(0,0));
        b.getBody().setAtRest(true);
        this.available.offer(b);
    }
}