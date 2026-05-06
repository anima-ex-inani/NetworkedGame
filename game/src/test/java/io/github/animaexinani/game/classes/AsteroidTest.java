package io.github.animaexinani.game.classes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsteroidTest {

    // --- construction ---

    @Test
    void constructor_createsSixVertexPolygon() {
        Asteroid a = new Asteroid(0.0f, 0.0f, 0.0, 0.0);
        // 6-sided hexagon: (6 - 2) * 3 = 12 indices
        assertEquals(12, a.getPolygon().indices().length);
    }

    @Test
    void constructor_isNotDeadInitially() {
        Asteroid a = new Asteroid(100.0f, 200.0f, 0.0, 0.0);
        assertFalse(a.isDead());
    }

    @Test
    void constructor_bodyPlacedAtStartPosition() {
        Asteroid a = new Asteroid(300.0f, 150.0f, 0.0, 0.0);
        assertEquals(300.0, a.getBody().getTransform().getTranslationX(), 1.0);
        assertEquals(150.0, a.getBody().getTransform().getTranslationY(), 1.0);
    }

    @Test
    void constructor_bodyHasGivenInitialVelocity() {
        Asteroid a = new Asteroid(0.0f, 0.0f, 50.0, -25.0);
        assertEquals(50.0, a.getBody().getLinearVelocity().x, 1e-5);
        assertEquals(-25.0, a.getBody().getLinearVelocity().y, 1e-5);
    }

    // --- getCollisionDamage ---

    @Test
    void getCollisionDamage_returnsOne() {
        Asteroid a = new Asteroid(0.0f, 0.0f, 0.0, 0.0);
        assertEquals(1, a.getCollisionDamage());
    }

    // --- update / wrapping ---

    @Test
    void update_wrapsPositionWhenOutsideScreenBounds() {
        // Place asteroid well beyond the right edge
        Asteroid a = new Asteroid(1100.0f, 300.0f, 0.0, 0.0);
        a.update(0.016, 1000.0, 600.0);
        double x = a.getBody().getTransform().getTranslationX();
        assertTrue(x >= 0 && x <= 1000.0, "Expected x wrapped but got: " + x);
    }

    @Test
    void update_doesNotWrapWhenInsideBounds() {
        Asteroid a = new Asteroid(400.0f, 300.0f, 0.0, 0.0);
        a.update(0.016, 1000.0, 600.0);
        assertEquals(400.0, a.getBody().getTransform().getTranslationX(), 1.0);
        assertEquals(300.0, a.getBody().getTransform().getTranslationY(), 1.0);
    }

    // --- damage ---

    @Test
    void threeDamageHits_killsAsteroid() {
        Asteroid a = new Asteroid(0.0f, 0.0f, 0.0, 0.0);
        a.takeDamage(1);
        a.takeDamage(1);
        assertFalse(a.isDead());
        a.takeDamage(1);
        assertTrue(a.isDead());
    }

    @Test
    void singleLethalHit_killsAsteroid() {
        Asteroid a = new Asteroid(0.0f, 0.0f, 0.0, 0.0);
        a.takeDamage(3);
        assertTrue(a.isDead());
    }

    // --- onDestroy does not throw ---

    @Test
    void onDestroy_doesNotThrow() {
        Asteroid a = new Asteroid(0.0f, 0.0f, 0.0, 0.0);
        assertDoesNotThrow(a::onDestroy);
    }
}