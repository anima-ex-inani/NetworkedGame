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
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

public class Ship implements Drawable {
    // physics state (no more x, y, angle, velicityX, velocityY)
    private final Body body;

    // physics constants
    private static final double THRUST_POWER = 5000.0; 
    private static final double TURN_TORQUE = 2500.0; 
    private static final double MAX_SPEED = 720.0;
    
    // visuals
    private Geometry geometry;

    // LOCAL_COORDS now used for BOTH visuals and the physical hitbox
    private static final float[][] LOCAL_COORDS= {
        { 30.0f, 0.0f },
        { -15.0f, 15.0f },
        { -15.0f, -15.0f }
    };

    // Removed the Geometry parameter from the constructor
    public Ship(float startX, float startY) {
        // Create the dyn4j Body
        this.body = new Body();

        // Create the physical shape uisng dyna4j's Geometry
        Polygon shipShape = org.dyn4j.geometry.Geometry.createPolygon(
            new Vector2(LOCAL_COORDS[0][0], LOCAL_COORDS[0][1]),
            new Vector2(LOCAL_COORDS[1][0], LOCAL_COORDS[1][1]),
            new Vector2(LOCAL_COORDS[2][0], LOCAL_COORDS[2][1])
        );

        // Attach the shape of the body by a fixture
        BodyFixture fixture = this.body.addFixture(shipShape);
        fixture.setDensity(0.001); // weight of the ship

        // Finalize physics body
        this.body.setMass(MassType.NORMAL);
        this.body.translate(startX, startY);

        // LinearDamping for friction
        this.body.setLinearDamping(0.2);
        this.body.setAngularDamping(2.0); // stops the ship from spinning forever

        // Create the initial visual geometry
        Vertex[] vertices = new Vertex[] {
            new Vertex(new PointF (startX + 30.0f, startY), new Point(0, 0), new Color(1.0f, 0.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(startX - 15.0f, startY - 15.0f), new Point(0, 0), new Color(0.0f, 1.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(startX - 15.0f, startY + 15.0f), new Point(0, 0), new Color(0.0f, 0.0f, 1.0f, 1.0f))
        };
        int[] indices = new int[] { 0, 1, 2 };
        this.geometry = new Geometry(vertices, indices, null);
    }

    // Expose the body so the game loop can add it to the physics world
    public Body getBody() {
        return this.body;
    }

    public void turnLeft(float dt) {
        // Apply counter clockwise rotational force
        this.body.applyTorque(-TURN_TORQUE);
    }

    public void turnRight(float dt) {
        // Apply clockwise rotational force
        this.body.applyTorque(TURN_TORQUE);
    }

    public void applyThrust(float dt) {
        // Get the direction the nose is currently pointing
        double angle = this.body.getTransform().getRotationAngle();
        // Create a force vector pushing in that direction
        Vector2 force = new Vector2(Math.cos(angle), Math.sin(angle)).multiply(THRUST_POWER);
        this.body.applyForce(force);
    }

    public void update(float dt, float screenWidth, float screenHeight) {
        // Vector clamping math
        Vector2 velocity = this.body.getLinearVelocity();
        if (velocity.getMagnitude() > MAX_SPEED) {
            velocity.normalize();         // Modifies the vector's length to 1
            velocity.multiply(MAX_SPEED); // Scales it up to the max speed
            this.body.setLinearVelocity(velocity);
        }

        // Screen wrap logic
        Transform transform = this.body.getTransform();
        double currentX = transform.getTranslationX();
        double currentY = transform.getTranslationY();
        boolean wrapped = false;

        if (currentX > screenWidth) { currentX = 0.0; wrapped = true; }
        else if (currentX < 0.0) { currentX = screenWidth; wrapped = true; }
        
        if (currentY > screenHeight) { currentY = 0.0; wrapped = true; }
        else if (currentY < 0.0) { currentY = screenHeight; wrapped = true; }

        if (wrapped) {
            // Teleport the physics body to the other side of the screen
            transform.setTranslation(currentX, currentY);
        }

        // Update the visual Geometry Vertices based on the Physics Body
        Vertex[] verts = this.geometry.vertices();
        double cosA = Math.cos(transform.getRotationAngle());
        double sinA = Math.sin(transform.getRotationAngle());

        for (int i = 0; i < 3; i++) {
            float localX = LOCAL_COORDS[i][0];
            float localY = LOCAL_COORDS[i][1];

            double rotatedX = (localX * cosA) - (localY * sinA);
            double rotatedY = (localX * sinA) + (localY * cosA);

            float finalX = (float) (rotatedX + transform.getTranslationX());
            float finalY = (float) (rotatedY + transform.getTranslationY());

            verts[i] = new Vertex(new PointF(finalX, finalY), verts[i].uv(), verts[i].color());
        }

        this.geometry.vertices(verts);
    }

    @Override
    public Vertex[] vertices() { return this.geometry.vertices(); }
    @Override
    public int[] indices() { return this.geometry.indices(); }
    @Override
    public Texture texture() { return this.geometry.texture(); }
}