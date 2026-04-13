package io.github.animaexinani.game.classes;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

public class Ship implements Drawable {
    // state
    private float x;
    private float y;
    private float angle; // radians
    private float velocityX;
    private float velocityY;

    // physics constants
    private static final float THRUST_POWER = 0.5f;
    private static final float TURN_SPEED = 0.05f;
    private static final float FRICTION = 0.98f;
    private static final float MAX_SPEED = 12.0f;
    
    // visuals
    private Geometry geometry;

    // Removed the Geometry parameter from the constructor
    public Ship(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.angle = 0.0f; 
        this.velocityX = 0.0f;
        this.velocityY = 0.0f;
        
        // Create the initial geometry directly inside the class
        Vertex[] vertices = new Vertex[] {
            new Vertex(new PointF(0, 0), new Point(0, 0), new Color(1.0f, 0.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(0, 0), new Point(0, 0), new Color(0.0f, 1.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(0, 0), new Point(0, 0), new Color(0.0f, 0.0f, 1.0f, 1.0f))
        };
        int[] indices = new int[] { 0, 1, 2 };
        
        this.geometry = new Geometry(vertices, indices, null);
    }

    public void turnLeft() {
        this.angle -= TURN_SPEED;
    }

    public void turnRight() {
        this.angle += TURN_SPEED;
    }

    public void applyThrust() {
        this.velocityX += (float) Math.cos(this.angle) * THRUST_POWER;
        this.velocityY += (float) Math.sin(this.angle) * THRUST_POWER;
    }

    public void update() {
        // apply friction and momentum
        this.velocityX *= FRICTION;
        this.velocityY *= FRICTION;

        // vector clamping math
        // calculate the length of the velocity vector by Pythagoras theory
        float currentSpeed = (float) Math.sqrt((this.velocityX * this.velocityX) + (this.velocityY * this.velocityY));
        
        // if we are going too fast, scale the X and Y down proportionally
        if (currentSpeed > MAX_SPEED) {
            float scale = MAX_SPEED / currentSpeed;
            this.velocityX *= scale;
            this.velocityY *= scale;
        }

        this.x += this.velocityX;
        this.y += this.velocityY;

        // screen wrap logic
        if (this.x > 960.0f) this.x = 0.0f;
        else if (this.x < 0.0f) this.x = 960.0f;
        
        if (this.y > 720.0f) this.y = 0.0f;
        else if (this.y < 0.0f) this.y = 720.0f;

        // update the Geometry Vertices
        Vertex[] verts = this.geometry.vertices();
        
        // define the ship's basic shape relative to its center (0,0)
        // [0] = Nose (pointing right), [1] = Back Left, [2] = Back Right
        float[][] localCoords = {
            { 30.0f, 0.0f },
            { -15.0f, -15.0f },
            { -15.0f, 15.0f }
        };

        float cosA = (float) Math.cos(this.angle);
        float sinA = (float) Math.sin(this.angle);

        for (int i = 0; i < 3; i++) {
            float localX = localCoords[i][0];
            float localY = localCoords[i][1];

            // apply 2D Rotation Matrix
            float rotatedX = (localX * cosA) - (localY * sinA);
            float rotatedY = (localX * sinA) + (localY * cosA);

            // translate to the ship's actual position on the screen
            float finalX = rotatedX + this.x;
            float finalY = rotatedY + this.y;

            // overwrite the vertex position, but keep its original color and UV mapping
            verts[i] = new Vertex(new PointF(finalX, finalY), verts[i].uv(), verts[i].color());
        }

        // tell the geometry object to save our new vertex positions
        this.geometry.vertices(verts);
    }

    // implement the Drawable interface methods
    @Override
    public Vertex[] vertices() {
        return this.geometry.vertices();
    }

    @Override
    public int[] indices() {
        return this.geometry.indices();
    }

    @Override
    public Texture texture() {
        return this.geometry.texture();
    }
}