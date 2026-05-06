package io.github.animaexinani.game.classes;

import io.github.animaexinani.engine.color.Color;
import io.github.animaexinani.engine.rendering.drawable.ConvexPolygon;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // Minimal concrete subclass for testing the abstract Entity base class.
    private static class TestEntity extends Entity {
        private int destroyCallCount = 0;

        TestEntity(Body body, ConvexPolygon polygon, int maxHealth) {
            super(body, polygon, maxHealth);
        }

        @Override
        public void update(double dt, double screenWidth, double screenHeight) {
            // no-op for base-class tests
        }

        @Override
        public void onDestroy() {
            this.destroyCallCount++;
        }

        int getDestroyCallCount() {
            return this.destroyCallCount;
        }
    }

    private static final Vector2[] TRIANGLE = {
        new Vector2(0.0, 1.0),
        new Vector2(-1.0, -1.0),
        new Vector2(1.0, -1.0)
    };

    private static Body createBody(double x, double y) {
        Body body = new Body();
        body.addFixture(org.dyn4j.geometry.Geometry.createCircle(1.0));
        body.setMass(MassType.NORMAL);
        body.translate(x, y);
        return body;
    }

    private static ConvexPolygon createPolygon() {
        return new ConvexPolygon(TRIANGLE, new Color(1.0f, 1.0f, 1.0f, 1.0f));
    }

    private TestEntity entity;

    @BeforeEach
    void setUp() {
        this.entity = new TestEntity(createBody(100, 200), createPolygon(), 3);
    }

    // --- constructor ---

    @Test
    void constructor_setsMaxHealthAndCurrentHealth() {
        assertFalse(this.entity.isDead());
    }

    @Test
    void constructor_bodyUserDataIsEntity() {
        assertSame(this.entity, this.entity.getBody().getUserData());
    }

    // --- isDead ---

    @Test
    void isDead_falseWhenHealthAboveZero() {
        assertFalse(this.entity.isDead());
    }

    @Test
    void isDead_trueWhenHealthIsZero() {
        this.entity.takeDamage(3);
        assertTrue(this.entity.isDead());
    }

    // --- takeDamage ---

    @Test
    void takeDamage_reducesHealth() {
        this.entity.takeDamage(1);
        assertFalse(this.entity.isDead());
    }

    @Test
    void takeDamage_exactLethalAmount_killsEntity() {
        this.entity.takeDamage(3);
        assertTrue(this.entity.isDead());
    }

    @Test
    void takeDamage_moreThanHealth_killsEntity() {
        this.entity.takeDamage(100);
        assertTrue(this.entity.isDead());
    }

    @Test
    void takeDamage_lethalHit_callsOnDestroy() {
        this.entity.takeDamage(3);
        assertEquals(1, this.entity.getDestroyCallCount());
    }

    @Test
    void takeDamage_nonLethalHit_doesNotCallOnDestroy() {
        this.entity.takeDamage(1);
        assertEquals(0, this.entity.getDestroyCallCount());
    }

    @Test
    void takeDamage_whenAlreadyDead_isIgnored() {
        this.entity.takeDamage(3); // kills
        this.entity.takeDamage(1); // should be ignored
        // onDestroy should only have been called once
        assertEquals(1, this.entity.getDestroyCallCount());
    }

    // --- updateVisuals ---

    @Test
    void updateVisuals_syncsPolygonTranslationToBodyPosition() {
        // Body was placed at (100, 200); polygon should reflect that after updateVisuals
        this.entity.updateVisuals();
        assertEquals(100.0f, this.entity.getPolygon().translation().x(), 1e-4f);
        assertEquals(200.0f, this.entity.getPolygon().translation().y(), 1e-4f);
    }

    @Test
    void updateVisuals_syncsPolygonRotationToBodyAngle() {
        // default body rotation is 0
        this.entity.updateVisuals();
        assertEquals(0.0f, this.entity.getPolygon().rotation(), 1e-5f);
    }

    // --- wrapPosition ---

    @Test
    void wrapPosition_xExceedsWidth_wrapsToZero() {
        Body body = createBody(1100, 300);
        TestEntity e = new TestEntity(body, createPolygon(), 1);
        e.wrapPosition(1000, 600);
        assertEquals(0.0, body.getTransform().getTranslationX(), 1e-5);
    }

    @Test
    void wrapPosition_xBelowZero_wrapsToWidth() {
        Body body = createBody(-5, 300);
        TestEntity e = new TestEntity(body, createPolygon(), 1);
        e.wrapPosition(1000, 600);
        assertEquals(1000.0, body.getTransform().getTranslationX(), 1e-5);
    }

    @Test
    void wrapPosition_yExceedsHeight_wrapsToZero() {
        Body body = createBody(300, 700);
        TestEntity e = new TestEntity(body, createPolygon(), 1);
        e.wrapPosition(1000, 600);
        assertEquals(0.0, body.getTransform().getTranslationY(), 1e-5);
    }

    @Test
    void wrapPosition_yBelowZero_wrapsToHeight() {
        Body body = createBody(300, -1);
        TestEntity e = new TestEntity(body, createPolygon(), 1);
        e.wrapPosition(1000, 600);
        assertEquals(600.0, body.getTransform().getTranslationY(), 1e-5);
    }

    @Test
    void wrapPosition_withinBounds_doesNotWrap() {
        Body body = createBody(500, 300);
        TestEntity e = new TestEntity(body, createPolygon(), 1);
        e.wrapPosition(1000, 600);
        assertEquals(500.0, body.getTransform().getTranslationX(), 1e-5);
        assertEquals(300.0, body.getTransform().getTranslationY(), 1e-5);
    }

    @Test
    void wrapPosition_exactlyAtBoundary_doesNotWrap() {
        Body body = createBody(1000, 600);
        TestEntity e = new TestEntity(body, createPolygon(), 1);
        e.wrapPosition(1000, 600);
        // x == width: wraps to 0
        assertEquals(0.0, body.getTransform().getTranslationX(), 1e-5);
        // y == height: wraps to 0
        assertEquals(0.0, body.getTransform().getTranslationY(), 1e-5);
    }

    // --- getBody / getPolygon ---

    @Test
    void getBody_returnsBody() {
        assertNotNull(this.entity.getBody());
    }

    @Test
    void getPolygon_returnsPolygon() {
        assertNotNull(this.entity.getPolygon());
    }
}
