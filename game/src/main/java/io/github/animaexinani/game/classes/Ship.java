package io.github.animaexinani.game.classes;
import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.rendering.drawable.Drawable;
import io.github.animaexinani.engine.rendering.drawable.Geometry;
import io.github.animaexinani.engine.texture.Texture;
import io.github.animaexinani.engine.vertex.Vertex;

import org.jetbrains.annotations.NotNull;

public class Ship implements Drawable {
    // state
    private float x;
    private float y;
    private float angle; // radians
    private float velocityX;
    private float velocityY;

    // physics constants
    private static final float THRUST_POWER = 1500.0f; 
    private static final float TURN_SPEED = 5.0f;    
    private static final float FRICTION = 0.98f;     
    private static final float MAX_SPEED = 720.0f;   
    
    // visuals
    private Geometry geometry;

    // define the ship's basic shape relative to its center (0,0)
        // [0] = Nose (pointing right), [1] = Back Left, [2] = Back Right
        private static final float[][] LOCAL_COORDS= {
            { 30.0f, 0.0f },
            { -15.0f, -15.0f },
            { -15.0f, 15.0f }
        };

    // Removed the Geometry parameter from the constructor
    public Ship(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.angle = 0.0f; 
        this.velocityX = 0.0f;
        this.velocityY = 0.0f;
        
        // Create the initial geometry directly inside the class
        Vertex[] vertices = new Vertex[] {
            new Vertex(new PointF(startX + 30.0f, startY), new Point(0, 0), new Color(1.0f, 0.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(startX - 15.0f, startY - 15.0f), new Point(0, 0), new Color(0.0f, 1.0f, 0.0f, 1.0f)),
            new Vertex(new PointF(startX - 15.0f, startY + 15.0f), new Point(0, 0), new Color(0.0f, 0.0f, 1.0f, 1.0f))
        };
        int[] indices = new int[] { 0, 1, 2 };
        
        this.geometry = new Geometry(vertices, indices, null);
    }

    public void turnLeft(float dt) {
        this.angle -= TURN_SPEED * dt;
    }

    public void turnRight(float dt) {
        this.angle += TURN_SPEED * dt;
    }

    public void applyThrust(float dt) {
        this.velocityX += (float) Math.cos(this.angle) * THRUST_POWER * dt;
        this.velocityY += (float) Math.sin(this.angle) * THRUST_POWER * dt;
    }

    public void update(float dt, float screenWidth, float screenHeight) {
        // apply friction and momentum
        float frictionThisFrame = (float) Math.pow(FRICTION, dt * 60.0f);
        this.velocityX *= frictionThisFrame;
        this.velocityY *= frictionThisFrame;

        // vector clamping math
        // calculate the length of the velocity vector by Pythagoras theory
        float currentSpeed = (float) Math.sqrt((this.velocityX * this.velocityX) + (this.velocityY * this.velocityY));
        
        // if we are going too fast, scale the X and Y down proportionally
        if (currentSpeed > MAX_SPEED) {
            float scale = MAX_SPEED / currentSpeed;
            this.velocityX *= scale;
            this.velocityY *= scale;
        }

        this.x += this.velocityX * dt;
        this.y += this.velocityY * dt;

        // screen wrap logic
        if (this.x > screenWidth) this.x = 0.0f;
        else if (this.x < 0.0f) this.x = screenWidth;
        
        if (this.y > screenHeight) this.y = 0.0f;
        else if (this.y < 0.0f) this.y = screenHeight;

        float cosA = (float) Math.cos(this.angle);
        float sinA = (float) Math.sin(this.angle);

        Vertex[] verts = new Vertex[this.geometry.vertexCount()];
        for (int i = 0; i < verts.length; i++) {
            float localX = LOCAL_COORDS[i][0];
            float localY = LOCAL_COORDS[i][1];

            float rotatedX = (localX * cosA) - (localY * sinA);
            float rotatedY = (localX * sinA) + (localY * cosA);

            Vertex original = this.geometry.vertexAt(i);
            verts[i] = new Vertex(new PointF(rotatedX + this.x, rotatedY + this.y), original.uv(), original.color());
        }
        this.geometry.setVertices(verts);
    }

    // implement the Drawable interface methods
    @Override
    public int vertexCount() {
        return this.geometry.vertexCount();
    }

    @Override
    public @NotNull Vertex vertexAt(int index) {
        return this.geometry.vertexAt(index);
    }

    @Override
    public int indexCount() {
        return this.geometry.indexCount();
    }

    @Override
    public int indexAt(int index) {
        return this.geometry.indexAt(index);
    }

    @Override
    public Texture texture() {
        return this.geometry.texture();
    }
}
