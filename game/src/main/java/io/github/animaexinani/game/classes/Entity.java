package io.github.animaexinani.game.classes;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

public abstract class Entity implements Drawable {
    protected final Body body;
    protected final Geometry geometry;
    protected final Vector2[] baseCoords; // stores the shape's original layout

    protected int health;
    protected final int maxHealth;

    public Entity(Body body, Geometry geometry, Vector2[] baseCoords, int maxHealth) {
        this.body = body;
        this.geometry = geometry;
        this.baseCoords = baseCoords;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

        // link the physics body back to this java object for collision detection
        this.body.setUserData(this);
    }

    public void takeDamage(int damage) {
        if (this.health <= 0) return; // already dead
        this.health -= damage;
        if (this.health <= 0) {
            this.onDestroy();
        }
    }

    // a universal visual updated that works for any shape (Ship, Bullet, Asteriod)
    public void updateVisuals() {
        Transform transform = this.body.getTransform();
        Vertex[] verts = this.geometry.vertices();

        double cosA = Math.cos(transform.getRotationAngle());
        double sinA = Math.sin(transform.getRotationAngle());

        // loop through however many points this specific entity has
        for (int i = 0; i < this.baseCoords.length; i++) {
            double localX = this.baseCoords[i].x;
            double localY = this.baseCoords[i].y;

            double rotatedX = (localX * cosA) - (localY * sinA);
            double rotatedY = (localX * sinA) + (localY * cosA);

            float finalX = (float) (rotatedX + transform.getTranslationX());
            float finalY = (float) (rotatedY + transform.getTranslationY());

            verts[i] = new Vertex(new PointF(finalX, finalY), verts[i].uv(), verts[i].color());
        }

        this.geometry.vertices(verts);
    }

    // universal screen wrap logic
    public void wrapPosition(double screenWidth, double screenHeight) {
        Transform t = this.body.getTransform();
        double x = t.getTranslationX();
        double y = t.getTranslationY();
        boolean wrapped = false;
        if (x > screenWidth)  { x = 0; wrapped = true; }
        else if (x < 0)       { x = screenWidth; wrapped = true; }
        
        if (y > screenHeight) { y = 0; wrapped = true; }
        else if (y < 0)       { y = screenHeight; wrapped = true; }
        
        if (wrapped) {
            t.setTranslation(x, y);
        }
    }
    // getters for the GameWorld
    public Body getBody() { return this.body; }
    public boolean isDead() { return this.health <= 0; }

    // drawable Interface Implementation
    @Override public Vertex[] vertices() { return this.geometry.vertices(); }
    @Override public int[] indices() { return this.geometry.indices(); }
    @Override public Texture texture() { return this.geometry.texture(); }

    // methods that every child class MUST implement themselves
    public abstract void update(double dt, double screenWidth, double screenHeight);
    public abstract void onDestroy();
}