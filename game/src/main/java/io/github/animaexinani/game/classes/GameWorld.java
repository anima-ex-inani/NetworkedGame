package io.github.animaexinani.game.classes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import io.github.animaexinani.game.collision.GameCollisionListener;

public class GameWorld {
    private final World<Body> physicsWorld;

    private final List<Entity> entities = new ArrayList<>();

    // add the BulletPool
    public final BulletPool bulletPool = new BulletPool();

    public GameWorld() {
        this.physicsWorld = new World<>();
        this.physicsWorld.setGravity(new Vector2(0.0, 0.0));
        this.physicsWorld.getSettings().setMaximumTranslation(150.0);

        // attach collision listener
        this.physicsWorld.addCollisionListener(new GameCollisionListener());
    }

    public void addEntity(Entity e) {
        this.entities.add(e);
        this.physicsWorld.addBody(e.getBody());
    }

    public void removeEntity(Entity e) {
        this.entities.remove(e);
        this.physicsWorld.removeBody(e.getBody());
    }

    public void update(double dt, double screenWidth, double screenHeight) {
        // calculate the physics for all bodies
        this.physicsWorld.update(dt);                          
        
        // trigger custom game logic and sync visual graphics
        for (Entity e : this.entities) {
            e.update(dt, screenWidth, screenHeight);      
            e.updateVisuals();                            
        }
        
        // clean up the dead entities safely
        for (Iterator<Entity> it = this.entities.iterator(); it.hasNext(); ) {
            Entity e = it.next();
            if (e.isDead()) {
                it.remove();
                this.physicsWorld.removeBody(e.getBody());
                if (e instanceof Bullet) this.bulletPool.recycle((Bullet) e);
            }
        }
    }

    // expose the list so NetworkedGame's Render Loop can draw them
    public List<Entity> getEntities() {
        return this.entities;
    }
    
    // expose the dyn4j world so we can attach collision listeners later
    public World<Body> getPhysicsWorld() {
        return this.physicsWorld;
    }
}