package io.github.animaexinani.game.classes;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.drawable.GeometryFactory;

public class Ship extends Entity {
    private static final double THRUST_POWER = 750.0; 
    private static final double TURN_TORQUE = 1500.0; 
    private static final double MAX_SPEED = 500.0;
    private static final double FIRE_COOLDOWN_SECONDS = 0.50;
    private double fireCooldown = 0.0;

    private static final Vector2[] LOCAL_COORDS = {
        new Vector2(30.0, 0.0),
        new Vector2(-15.0, 15.0),
        new Vector2(-15.0, -15.0)
    };

    private static final Color SHIP_COLOR = new Color(0.0f, 1.0f, 0.0f, 1.0f);

    public Ship(float startX, float startY) {
        super(createBody(startX, startY), 
              GeometryFactory.createConvexPolygon(startX, startY, LOCAL_COORDS, SHIP_COLOR), 
              LOCAL_COORDS, 5);
    }

    // factory method to keep the constructor clean
    private static Body createBody(float x, float y) {
        Body body = new Body();
        Polygon shipShape = org.dyn4j.geometry.Geometry.createPolygon(LOCAL_COORDS);
        BodyFixture fixture = body.addFixture(shipShape);
        fixture.setDensity(0.001);  // ship weight

        body.setMass(MassType.NORMAL);
        body.translate(x, y);
        body.setLinearDamping(0.2);
        body.setAngularDamping(2.0);
        return body;
    }

    public void turnLeft(float dt) { this.body.applyTorque(-TURN_TORQUE); }
    public void turnRight(float dt) { this.body.applyTorque(TURN_TORQUE); }

    public void applyThrust(float dt) {
        double angle = this.body.getTransform().getRotationAngle();
        Vector2 force = new Vector2(Math.cos(angle), Math.sin(angle)).multiply(THRUST_POWER);
        this.body.applyForce(force);
    }

    public void fire(GameWorld world) {
        if (this.fireCooldown > 0) return; // gun is still reloading

            org.dyn4j.geometry.Transform t = this.body.getTransform();
            double angle = t.getRotationAngle();
            double x = t.getTranslationX();
            double y = t.getTranslationY();
            
            // spawn the bullet slightly in front of the ship's nose
            double spawnX = x + (Math.cos(angle) * 35.0);
            double spawnY = y + (Math.sin(angle) * 35.0);

            // obtain a bullet and add it to the physics world
            Bullet b = world.bulletPool.obtain(spawnX, spawnY, angle, 1500.0);
            world.addEntity(b);
            
            this.fireCooldown = FIRE_COOLDOWN_SECONDS;
    }

    @Override
    public void update(double dt, double screenWidth, double screenHeight) {
        // decrease the reload timer
        if (this.fireCooldown > 0) {
            this.fireCooldown -= dt;
        }

        // decrease i-frame timer
        if (this.damageCooldown > 0) {
            this.damageCooldown -= dt;
        }

        Vector2 velocity = this.body.getLinearVelocity();
        if (velocity.getMagnitude() > MAX_SPEED) {
            velocity.normalize();
            velocity.multiply(MAX_SPEED);
            this.body.setLinearVelocity(velocity);
        }

        this.wrapPosition(screenWidth, screenHeight);
    }

    @Override
    public void takeDamage(int damage) {
        if (this.health <= 0) return; 
        
        // ship-specific I-frame check
        if (this.damageCooldown > 0) return; 

        this.health -= damage;
        
        // give the ship 1 second of invincibility
        this.damageCooldown = 1.0; 

        if (this.health <= 0) {
            this.onDestroy();
        }
    }

    @Override
    public void onDestroy() {
        System.out.println("Ship Destroyed! Game Over!");
    }
}