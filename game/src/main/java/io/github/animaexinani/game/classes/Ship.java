package io.github.animaexinani.game.classes;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.vertex.Vertex;

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

    public Ship(float startX, float startY) {
        // pass everything to the entity constructor
        super(createBody(startX, startY), createGeometry(startX, startY), LOCAL_COORDS, 100);
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

    // factory method to keep the constructor clean
    private static Geometry createGeometry(float x, float y) {
        Color shipColor = new Color(0.0f, 1.0f, 0.0f, 1.0f); // green, fully opaque
        Vertex[] vertices = new Vertex[] {
            new Vertex(new PointF(x + 30.0f, y), new Point(0, 0), shipColor),
            new Vertex(new PointF(x - 15.0f, y + 15.0f), new Point(0, 0), shipColor),
            new Vertex(new PointF(x - 15.0f, y - 15.0f), new Point(0, 0), shipColor)
        };
        int[] indices = new int[] { 0, 1, 2 };
        return new Geometry(vertices, indices, null);
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

        Vector2 velocity = this.body.getLinearVelocity();
        if (velocity.getMagnitude() > MAX_SPEED) {
            velocity.normalize();
            velocity.multiply(MAX_SPEED);
            this.body.setLinearVelocity(velocity);
        }

        this.wrapPosition(screenWidth, screenHeight);
    }

    @Override
    public void onDestroy() {
        System.out.println("Ship Destroyed! Game Over!");
    }
}