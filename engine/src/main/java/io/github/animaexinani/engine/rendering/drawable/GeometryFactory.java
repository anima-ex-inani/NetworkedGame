package io.github.animaexinani.engine.rendering.drawable;

import org.dyn4j.geometry.Vector2;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.vertex.Vertex;

public class GeometryFactory {

    // automatically generates a Geometry object for any convex polygon.
     
    public static Geometry createConvexPolygon(float startX, float startY, Vector2[] localCoords, Color color) {
        int numVertices = localCoords.length;
        Vertex[] vertices = new Vertex[numVertices];

        // generate Vertices based on local coordinates and starting position
        for (int i = 0; i < numVertices; i++) {
            float px = startX + (float) localCoords[i].x;
            float py = startY + (float) localCoords[i].y;
            vertices[i] = new Vertex(new PointF(px, py), new Point(0, 0), color);
        }

        // generate Indices using a Triangle Fan algorithm
        // a polygon with N vertices requires (N - 2) triangles.
        int numTriangles = numVertices - 2;
        int[] indices = new int[numTriangles * 3];

        for (int i = 0; i < numTriangles; i++) {
            indices[i * 3]     = 0;         // always anchor to the first vertex
            indices[i * 3 + 1] = i + 1;     // next vertex
            indices[i * 3 + 2] = i + 2;     // vertex after that
        }

        return new Geometry(vertices, indices, null);
    }
}