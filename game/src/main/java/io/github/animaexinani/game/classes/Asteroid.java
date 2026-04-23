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

public class Asteroid extends Entity {
    // a simple 6-sided hexagon shape
    private static final Vector2[] LOCAL_COORDS = {
        new Vector2(20.0, 0.0),
        new Vector2(10.0, 15.0),
        new Vector2(-10.0, 15.0),
        new Vector2(-20.0, 0.0),
        new Vector2(-10.0, -15.0),
        new Vector2(10.0, -15.0)
    };

    public Asteroid(float startX, float startY, double velocityX, double velocityY) {
        super(createBody(startX, startY, velocityX, velocityY), createGeometry(startX, startY), LOCAL_COORDS, 3);
    }

    private static Body createBody(float x, float y, double vx, double vy) {
        Body body = new Body();
        Polygon shape = org.dyn4j.geometry.Geometry.createPolygon(LOCAL_COORDS);
        BodyFixture fixture = body.addFixture(shape);
        fixture.setDensity(0.005); // asteroids are a bit heavier than the ship
        
        body.setMass(MassType.NORMAL);
        body.translate(x, y);
        // zero damping so asteroids float forever in space
        body.setLinearDamping(0.0);
        body.setAngularDamping(0.0); 
        
        // give it an initial push and a slight spin
        body.setLinearVelocity(new Vector2(vx, vy));
        body.setAngularVelocity(Math.random() * 2.0 - 1.0); 
        return body;
    }

    private static Geometry createGeometry(float x, float y) {
        Vertex[] vertices = new Vertex[6];
        Color gray = new Color(0.6f, 0.6f, 0.6f, 1.0f);
        for (int i = 0; i < 6; i++) {
            vertices[i] = new Vertex(new PointF(x + (float)LOCAL_COORDS[i].x, y + (float)LOCAL_COORDS[i].y), new Point(0,0), gray);
        }
        int[] indices = new int[] { 0, 1, 2, 0, 2, 3, 0, 3, 4, 0, 4, 5 }; // Triangulate the hexagon
        return new Geometry(vertices, indices, null);
    }

    public int getCollisionDamage() {
        return 1; // do 1 damage to the ship on impact
    }

    @Override
    public void update(double dt, double screenWidth, double screenHeight) {
        this.wrapPosition(screenWidth, screenHeight);
    }

    @Override
    public void onDestroy() {
        // TODO: Spawn smaller asteroids or particle effects later!
        System.out.println("Asteroid shattered!");
    }
}