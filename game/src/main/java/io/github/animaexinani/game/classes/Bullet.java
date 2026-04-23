package io.github.animaexinani.game.classes;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.vertex.Vertex;

public class Bullet extends Entity {
    private static final double LIFE_SECONDS = 2.0;
    private double timeAlive = 0;
    private int damage = 1;

    // a tiny triangle
    private static final Vector2[] LOCAL_COORDS = {
        new Vector2(5.0, 0.0),   // nose 
        new Vector2(-3.0, 2.0),  // back left
        new Vector2(-3.0, -2.0)  // back right
    };

    // bullets spawn "dead" by default so they can wait in the pool
    public Bullet() {
        super(createBody(), createGeometry(), LOCAL_COORDS, 1);
        this.health = 0; // starts dead
    }

    private static Body createBody() {
        Body body = new Body();
        Polygon shape = org.dyn4j.geometry.Geometry.createPolygon(LOCAL_COORDS);
        BodyFixture fixture = body.addFixture(shape);
        fixture.setDensity(0.0001); // very light
        
        body.setMass(MassType.NORMAL);
        body.setBullet(true); // enables continuous collision detection!
        return body;
    }

    private static Geometry createGeometry() {
        Vertex[] vertices = new Vertex[3];
        Color yellow = new Color(1.0f, 1.0f, 0.0f, 1.0f);
        
        for (int i=0; i<3; i++) {
            vertices[i] = new Vertex(new PointF(0,0), new Point(0,0), yellow);
        }
        
        int[] indices = new int[] { 0, 1, 2 };
        return new Geometry(vertices, indices, null);
    }

    // called by the pool when fired
    public void activate(double x, double y, double angle, double speed) {
        this.health = this.maxHealth;
        this.timeAlive = 0;

        // direct transform update
        Transform t = this.body.getTransform();
        t.setTranslation(x, y);
        t.setRotation(angle);

        this.body.clearForce();
        this.body.clearTorque();

        Vector2 velocity = new Vector2(Math.cos(angle), Math.sin(angle)).multiply(speed);
        this.body.setLinearVelocity(velocity);
        this.body.setAngularVelocity(0.0);
        this.body.setAtRest(false);

        this.updateVisuals();
    }

    public int getDamage() { return this.damage; }

    @Override
    public void update(double dt, double screenWidth, double screenHeight) {
        if (this.health <= 0) return; // ignore if dead in the pool

        this.timeAlive += dt;
        if (this.timeAlive > LIFE_SECONDS) {
            this.health = 0; // auto-die after 2 seconds
        }
        
        this.wrapPosition(screenWidth, screenHeight);
    }

    @Override
    public void onDestroy() {}
}