package io.github.animaexinani.game.classes;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;

public class Asteroid extends Entity {
    
    private static final Vector2[] LOCAL_COORDS = {
        new Vector2(20.0, 0.0),
        new Vector2(10.0, 15.0),
        new Vector2(-10.0, 15.0),
        new Vector2(-20.0, 0.0),
        new Vector2(-10.0, -15.0),
        new Vector2(10.0, -15.0)
    };

    private static final Color ASTEROID_COLOR = new Color(0.6f, 0.6f, 0.6f, 1.0f);

    public Asteroid(float startX, float startY, double velocityX, double velocityY) {
        super(createBody(startX, startY, velocityX, velocityY), 
              new ConvexPolygon(LOCAL_COORDS, ASTEROID_COLOR), 
              3);
    }

    private static Body createBody(float x, float y, double vx, double vy) {
        Body body = new Body();
        Polygon shape = org.dyn4j.geometry.Geometry.createPolygon(LOCAL_COORDS);
        BodyFixture fixture = body.addFixture(shape);
        fixture.setDensity(0.005);
        
        body.setMass(MassType.NORMAL);
        body.translate(x, y);
        body.setLinearDamping(0.0);
        body.setAngularDamping(0.0); 
        
        body.setLinearVelocity(new Vector2(vx, vy));
        body.setAngularVelocity(Math.random() * 2.0 - 1.0); 
        return body;
    }

    public int getCollisionDamage() {
        return 1;
    }

    @Override
    public void update(double dt, double screenWidth, double screenHeight) {
        this.wrapPosition(screenWidth, screenHeight);
    }

    @Override
    public void onDestroy() {
        System.out.println("Asteroid shattered!");
    }
}