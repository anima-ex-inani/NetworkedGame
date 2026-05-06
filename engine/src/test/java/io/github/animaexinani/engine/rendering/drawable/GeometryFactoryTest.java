package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.vertex.Vertex;
import org.dyn4j.geometry.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeometryFactoryTest {

    // --- vertex positions ---

    @Test
    void createConvexPolygon_vertexPositionsAreStartPlusLocalCoord() {
        Vector2[] coords = {
            new Vector2(1.0, 2.0),
            new Vector2(3.0, 4.0),
            new Vector2(5.0, 6.0)
        };
        float startX = 10.0f;
        float startY = 20.0f;

        Geometry g = GeometryFactory.createConvexPolygon(startX, startY, coords, Color.WHITE);
        Vertex[] verts = g.vertices();

        assertEquals(startX + 1.0f, verts[0].position().x(), 1e-5f);
        assertEquals(startY + 2.0f, verts[0].position().y(), 1e-5f);

        assertEquals(startX + 3.0f, verts[1].position().x(), 1e-5f);
        assertEquals(startY + 4.0f, verts[1].position().y(), 1e-5f);

        assertEquals(startX + 5.0f, verts[2].position().x(), 1e-5f);
        assertEquals(startY + 6.0f, verts[2].position().y(), 1e-5f);
    }

    @Test
    void createConvexPolygon_vertexColorMatchesInput() {
        Vector2[] coords = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(0, 1)
        };
        Color red = new Color(1.0f, 0.0f, 0.0f, 1.0f);

        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, red);
        for (Vertex v : g.vertices()) {
            assertEquals(red, v.color());
        }
    }

    @Test
    void createConvexPolygon_vertexCountMatchesInputCoordCount() {
        Vector2[] coords = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1),
            new Vector2(0, 1)
        };
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        assertEquals(4, g.vertices().length);
    }

    // --- triangle fan index generation ---

    @Test
    void createConvexPolygon_triangle_hasThreeIndices() {
        Vector2[] coords = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(0, 1)
        };
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        int[] indices = g.indices();
        assertEquals(3, indices.length);
        assertArrayEquals(new int[]{0, 1, 2}, indices);
    }

    @Test
    void createConvexPolygon_quadrilateral_hasSixIndices_triangleFan() {
        Vector2[] coords = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1),
            new Vector2(0, 1)
        };
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        int[] indices = g.indices();
        // (4 - 2) * 3 = 6 indices
        assertEquals(6, indices.length);
        // first triangle: [0, 1, 2]
        assertEquals(0, indices[0]);
        assertEquals(1, indices[1]);
        assertEquals(2, indices[2]);
        // second triangle: [0, 2, 3]
        assertEquals(0, indices[3]);
        assertEquals(2, indices[4]);
        assertEquals(3, indices[5]);
    }

    @Test
    void createConvexPolygon_hexagon_hasTwelveIndices() {
        Vector2[] coords = new Vector2[6];
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI * 2 * i / 6;
            coords[i] = new Vector2(Math.cos(angle), Math.sin(angle));
        }
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        // (6 - 2) * 3 = 12 indices
        assertEquals(12, g.indices().length);
    }

    @Test
    void createConvexPolygon_indicesAlwaysAnchorToZero() {
        Vector2[] coords = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2),
            new Vector2(-1, 1)
        };
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        int[] indices = g.indices();
        // every triangle starts with index 0
        assertEquals(0, indices[0]);
        assertEquals(0, indices[3]);
        assertEquals(0, indices[6]);
    }

    // --- texture is null ---

    @Test
    void createConvexPolygon_textureIsNull() {
        Vector2[] coords = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(0, 1)
        };
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        assertNull(g.texture());
    }

    // --- zero-offset start position ---

    @Test
    void createConvexPolygon_zeroStart_positionsEqualLocalCoords() {
        Vector2[] coords = {
            new Vector2(5.0, -3.0),
            new Vector2(10.0, 7.0),
            new Vector2(0.0, 4.0)
        };
        Geometry g = GeometryFactory.createConvexPolygon(0, 0, coords, Color.WHITE);
        assertEquals(5.0f, g.vertices()[0].position().x(), 1e-5f);
        assertEquals(-3.0f, g.vertices()[0].position().y(), 1e-5f);
    }
}