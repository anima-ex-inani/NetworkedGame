package io.github.animaexinani.game.classes;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;

public abstract class Entity {
    protected final Body body;
    protected final ConvexPolygon polygon; 

    protected int health;
    protected final int maxHealth;
    protected double damageCooldown = 0.0; 

    public Entity(Body body, ConvexPolygon polygon, int maxHealth) {
        this.body = body;
        this.polygon = polygon;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

        this.body.setUserData(this);
    }

    public void takeDamage(int damage) {
        if (this.health <= 0) return; 
        if (this.damageCooldown > 0) return; 

        this.health -= damage;

        if (this.health <= 0) {
            this.onDestroy();
        }
    }

    public void updateVisuals() {
        // grab the physics state
        Transform physTransform = this.body.getTransform();
        
        float x = (float) physTransform.getTranslationX();
        float y = (float) physTransform.getTranslationY();
        float angle = (float) physTransform.getRotationAngle();
        
        // pass it directly to the visual polygon
        this.polygon.translation(new PointF(x, y));
        this.polygon.rotation(angle);
    }

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
    
    // getters
    public Body getBody() { return this.body; }
    public ConvexPolygon getPolygon() { return this.polygon; }
    public boolean isDead() { return this.health <= 0; }

    // abstract methods
    public abstract void update(double dt, double screenWidth, double screenHeight);
    public abstract void onDestroy();
}