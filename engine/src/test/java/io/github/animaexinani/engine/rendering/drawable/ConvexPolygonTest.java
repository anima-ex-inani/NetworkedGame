package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.size.SizeF;
import io.github.animaexinani.engine.vertex.Vertex;
import org.dyn4j.geometry.Vector2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConvexPolygonTest {

    // A simple triangle centred at the origin
    private static final Vector2[] TRIANGLE = {
        new Vector2(0.0, 1.0),
        new Vector2(-1.0, -1.0),
        new Vector2(1.0, -1.0)
    };

    private static final Color TINT = Color.WHITE;

    private ConvexPolygon polygon;

    @BeforeEach
    void setUp() {
        this.polygon = new ConvexPolygon(TRIANGLE, TINT);
    }

    // --- indices ---

    @Test
    void indices_triangleHasThreeIndices() {
        int[] indices = this.polygon.indices();
        assertEquals(3, indices.length);
    }

    @Test
    void indices_triangleFan_firstTriangle() {
        int[] indices = this.polygon.indices();
        assertArrayEquals(new int[]{0, 1, 2}, indices);
    }

    @Test
    void indices_quadrilateral_hasSixIndices() {
        Vector2[] quad = {
            new Vector2(1, 1),
            new Vector2(-1, 1),
            new Vector2(-1, -1),
            new Vector2(1, -1)
        };
        ConvexPolygon p = new ConvexPolygon(quad, TINT);
        assertEquals(6, p.indices().length);
    }

    @Test
    void indices_alwaysAnchorToZero() {
        Vector2[] pentagon = new Vector2[5];
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI * 2 * i / 5;
            pentagon[i] = new Vector2(Math.cos(angle), Math.sin(angle));
        }
        ConvexPolygon p = new ConvexPolygon(pentagon, TINT);
        int[] indices = p.indices();
        // First index of every triangle should be 0
        assertEquals(0, indices[0]);
        assertEquals(0, indices[3]);
        assertEquals(0, indices[6]);
    }

    // --- texture ---

    @Test
    void texture_isAlwaysNull() {
        assertNull(this.polygon.texture());
    }

    // --- initial Transformable defaults ---

    @Test
    void defaultTranslation_isZero() {
        assertEquals(PointF.ZERO, this.polygon.translation());
    }

    @Test
    void defaultRotation_isZero() {
        assertEquals(0.0f, this.polygon.rotation());
    }

    @Test
    void defaultScale_isOne() {
        assertEquals(SizeF.ONE, this.polygon.scale());
    }

    @Test
    void defaultPivot_isZero() {
        assertEquals(PointF.ZERO, this.polygon.pivot());
    }

    // --- vertex caching / dirty flag ---

    @Test
    void vertices_returnsCachedResultOnSecondCall() {
        Vertex[] first = this.polygon.vertices();
        Vertex[] second = this.polygon.vertices();
        assertSame(first, second);
    }

    @Test
    void vertices_countMatchesLocalCoordCount() {
        assertEquals(TRIANGLE.length, this.polygon.vertices().length);
    }

    @Test
    void vertices_afterNoTransform_positionMatchesLocalCoords() {
        Vertex[] verts = this.polygon.vertices();
        // With identity transform, positions should equal local coords
        assertEquals((float) TRIANGLE[0].x, verts[0].position().x(), 1e-5f);
        assertEquals((float) TRIANGLE[0].y, verts[0].position().y(), 1e-5f);
    }

    @Test
    void vertices_colorMatchesTint() {
        Vertex[] verts = this.polygon.vertices();
        for (Vertex v : verts) {
            assertEquals(TINT, v.color());
        }
    }

    // --- translation ---

    @Test
    void settingTranslation_invalidatesCacheAndShiftsVertices() {
        PointF offset = new PointF(5.0f, 3.0f);
        this.polygon.translation(offset);
        Vertex[] verts = this.polygon.vertices();

        assertEquals((float) TRIANGLE[0].x + 5.0f, verts[0].position().x(), 1e-4f);
        assertEquals((float) TRIANGLE[0].y + 3.0f, verts[0].position().y(), 1e-4f);
    }

    @Test
    void settingSameTranslation_doesNotInvalidateCache() {
        // Prime the cache
        Vertex[] before = this.polygon.vertices();
        // Set same value
        this.polygon.translation(PointF.ZERO);
        Vertex[] after = this.polygon.vertices();
        assertSame(before, after);
    }

    @Test
    void translation_getter_returnsSetValue() {
        PointF pt = new PointF(2.0f, 4.0f);
        this.polygon.translation(pt);
        assertEquals(pt, this.polygon.translation());
    }

    @Test
    void translation_null_throws() {
        assertThrows(NullPointerException.class, () -> this.polygon.translation(null));
    }

    // --- rotation ---

    @Test
    void settingRotation_invalidatesCache() {
        Vertex[] before = this.polygon.vertices();
        this.polygon.rotation((float) Math.PI / 2);
        Vertex[] after = this.polygon.vertices();
        // Cache should have been recomputed - arrays won't be same reference
        // (vertices() always returns the same backing array, but content changes)
        // So test that the first vertex moved
        assertNotEquals(before[0].position().x(), after[0].position().x(), 1e-4);
    }

    @Test
    void settingSameRotation_doesNotInvalidateCache() {
        Vertex[] before = this.polygon.vertices();
        this.polygon.rotation(0.0f);
        Vertex[] after = this.polygon.vertices();
        assertSame(before, after);
    }

    @Test
    void rotation_getter_returnsSetValue() {
        float angle = 1.234f;
        this.polygon.rotation(angle);
        assertEquals(angle, this.polygon.rotation(), 1e-6f);
    }

    // --- scale ---

    @Test
    void settingScale_invalidatesCache() {
        Vertex[] before = this.polygon.vertices();
        this.polygon.scale(new SizeF(2.0f, 2.0f));
        Vertex[] after = this.polygon.vertices();
        assertNotEquals(before[0].position().x(), after[0].position().x(), 1e-4);
    }

    @Test
    void settingSameScale_doesNotInvalidateCache() {
        Vertex[] before = this.polygon.vertices();
        this.polygon.scale(SizeF.ONE);
        Vertex[] after = this.polygon.vertices();
        assertSame(before, after);
    }

    @Test
    void scale_null_throws() {
        assertThrows(NullPointerException.class, () -> this.polygon.scale(null));
    }

    // --- pivot ---

    @Test
    void settingPivot_invalidatesCache() {
        // prime the cache first with some rotation so pivot matters
        this.polygon.rotation(0.5f);
        Vertex[] before = this.polygon.vertices();
        this.polygon.pivot(new PointF(1.0f, 0.0f));
        Vertex[] after = this.polygon.vertices();
        assertNotEquals(before[0].position().x(), after[0].position().x(), 1e-4);
    }

    @Test
    void pivot_null_throws() {
        assertThrows(NullPointerException.class, () -> this.polygon.pivot(null));
    }

    @Test
    void pivot_getter_returnsSetValue() {
        PointF pivot = new PointF(1.0f, 2.0f);
        this.polygon.pivot(pivot);
        assertEquals(pivot, this.polygon.pivot());
    }
}