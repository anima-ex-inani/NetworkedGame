package io.github.animaexinani.game.entities;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;

import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;

public abstract class Entity {
    protected final Body body;
    protected final ConvexPolygon polygon; 

    protected int health;
    protected final int maxHealth;

    public Entity(Body body, ConvexPolygon polygon, int maxHealth) {
        this.body = body;
        this.polygon = polygon;
        this.maxHealth = maxHealth;
        this.health = maxHealth;

        this.body.setUserData(this);
    }

    public void takeDamage(int damage) {
        if (this.health <= 0) return; 
        this.health -= damage;
        if (this.health <= 0) {
            this.onDestroy();
        }
    }

    public void updateVisuals() {
        Transform physTransform = this.body.getTransform();
        
        float x = (float) physTransform.getTranslationX();
        float y = (float) physTransform.getTranslationY();
        float angle = (float) physTransform.getRotationAngle();
        
        // setting these fields safely flips the dirty flag inside the polygon only if the coordinates changed
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
    
    public Body getBody() { return this.body; }
    public ConvexPolygon getPolygon() { return this.polygon; }
    public boolean isDead() { return this.health <= 0; }

    public abstract void update(double dt, double screenWidth, double screenHeight);
    public abstract void onDestroy();
}