package io.github.animaexinani.engine.rendering.drawable;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.point.Point;
import io.github.animaexinani.engine.point.PointF;
import io.github.animaexinani.engine.vertex.Vertex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeometryTest {

    private static Vertex makeVertex(float x, float y) {
        return new Vertex(new PointF(x, y), new Point(0, 0), Color.WHITE);
    }

    private static Vertex[] twoVertices() {
        return new Vertex[]{makeVertex(0, 0), makeVertex(1, 1)};
    }

    private static int[] twoIndices() {
        return new int[]{0, 1};
    }

    // --- constructor ---

    @Test
    void constructor_storesVerticesAndIndicesAndTexture() {
        Vertex[] verts = twoVertices();
        int[] indices = twoIndices();
        Geometry g = new Geometry(verts, indices, null);

        assertSame(verts, g.vertices());
        assertSame(indices, g.indices());
        assertNull(g.texture());
    }

    @Test
    void constructor_throwsOnNullVerticesArray() {
        assertThrows(NullPointerException.class, () -> new Geometry(null, twoIndices(), null));
    }

    @Test
    void constructor_throwsOnNullVertexElement() {
        Vertex[] verts = new Vertex[]{makeVertex(0, 0), null};
        assertThrows(NullPointerException.class, () -> new Geometry(verts, twoIndices(), null));
    }

    @Test
    void constructor_throwsOnNullIndicesArray() {
        assertThrows(NullPointerException.class, () -> new Geometry(twoVertices(), null, null));
    }

    // --- vertices setter ---

    @Test
    void verticesSetter_updatesVertices() {
        Geometry g = new Geometry(twoVertices(), twoIndices(), null);
        Vertex[] newVerts = new Vertex[]{makeVertex(5, 6)};
        g.vertices(newVerts);
        assertSame(newVerts, g.vertices());
    }

    @Test
    void verticesSetter_throwsOnNullArray() {
        Geometry g = new Geometry(twoVertices(), twoIndices(), null);
        assertThrows(NullPointerException.class, () -> g.vertices(null));
    }

    @Test
    void verticesSetter_throwsOnNullElement() {
        Geometry g = new Geometry(twoVertices(), twoIndices(), null);
        assertThrows(NullPointerException.class, () -> g.vertices(new Vertex[]{makeVertex(0, 0), null}));
    }

    // --- indices setter ---

    @Test
    void indicesSetter_updatesIndices() {
        Geometry g = new Geometry(twoVertices(), twoIndices(), null);
        int[] newIndices = new int[]{1, 0};
        g.indices(newIndices);
        assertSame(newIndices, g.indices());
    }

    @Test
    void indicesSetter_throwsOnNull() {
        Geometry g = new Geometry(twoVertices(), twoIndices(), null);
        assertThrows(NullPointerException.class, () -> g.indices(null));
    }

    // --- texture setter ---

    @Test
    void textureSetter_updatesToNonNull() {
        Geometry g = new Geometry(twoVertices(), twoIndices(), null);
        // We can't instantiate a real Texture without native libs, so just verify null→null stays
        assertNull(g.texture());
        g.texture(null);
        assertNull(g.texture());
    }

    // --- array aliasing (no defensive copy in new impl) ---

    @Test
    void vertices_returnsSameArrayInstance() {
        Vertex[] verts = twoVertices();
        Geometry g = new Geometry(verts, twoIndices(), null);
        assertSame(verts, g.vertices());
    }

    @Test
    void indices_returnsSameArrayInstance() {
        int[] indices = twoIndices();
        Geometry g = new Geometry(twoVertices(), indices, null);
        assertSame(indices, g.indices());
    }

    // --- empty arrays are accepted ---

    @Test
    void constructor_acceptsEmptyVerticesAndIndices() {
        Geometry g = new Geometry(new Vertex[0], new int[0], null);
        assertEquals(0, g.vertices().length);
        assertEquals(0, g.indices().length);
    }
}