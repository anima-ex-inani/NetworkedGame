package io.github.animaexinani.game.classes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletTest {

    private Bullet bullet;

    @BeforeEach
    void setUp() {
        this.bullet = new Bullet();
    }

    // --- initial state ---

    @Test
    void newBullet_isDeadByDefault() {
        assertTrue(this.bullet.isDead());
    }

    @Test
    void newBullet_getDamage_returnsOne() {
        assertEquals(1, this.bullet.getDamage());
    }

    // --- activate ---

    @Test
    void activate_makesAlive() {
        this.bullet.activate(0, 0, 0, 100);
        assertFalse(this.bullet.isDead());
    }

    @Test
    void activate_setsBodyPosition() {
        this.bullet.activate(200.0, 300.0, 0, 100);
        assertEquals(200.0, this.bullet.getBody().getTransform().getTranslationX(), 1e-4);
        assertEquals(300.0, this.bullet.getBody().getTransform().getTranslationY(), 1e-4);
    }

    @Test
    void activate_setsBodyRotation() {
        double angle = Math.PI / 4;
        this.bullet.activate(0, 0, angle, 100);
        assertEquals(angle, this.bullet.getBody().getTransform().getRotationAngle(), 1e-4);
    }

    @Test
    void activate_setsLinearVelocityAlongAngle() {
        double angle = 0; // pointing right
        double speed = 100.0;
        this.bullet.activate(0, 0, angle, speed);
        assertEquals(speed * Math.cos(angle), this.bullet.getBody().getLinearVelocity().x, 1e-4);
        assertEquals(speed * Math.sin(angle), this.bullet.getBody().getLinearVelocity().y, 1e-4);
    }

    @Test
    void activate_resetsTimeAlive() {
        // First activate and age it a bit via update
        this.bullet.activate(0, 0, 0, 100);
        this.bullet.update(1.9, 1000, 600); // close to timeout but alive
        assertFalse(this.bullet.isDead());

        // Activate again - should reset timer so bullet is alive again
        this.bullet.activate(0, 0, 0, 100);
        this.bullet.update(0.5, 1000, 600);
        assertFalse(this.bullet.isDead());
    }

    // --- update / lifetime ---

    @Test
    void update_afterTwoSeconds_bulletDies() {
        this.bullet.activate(0, 0, 0, 100);
        this.bullet.update(2.1, 1000, 600);
        assertTrue(this.bullet.isDead());
    }

    @Test
    void update_beforeTwoSeconds_bulletStaysAlive() {
        this.bullet.activate(0, 0, 0, 100);
        this.bullet.update(1.9, 1000, 600);
        assertFalse(this.bullet.isDead());
    }

    @Test
    void update_whenDead_doesNothing() {
        // bullet is dead by default; update should be a no-op
        assertDoesNotThrow(() -> this.bullet.update(0.016, 1000, 600));
        assertTrue(this.bullet.isDead());
    }

    @Test
    void update_exactlyAtTwoSeconds_bulletStaysAlive() {
        this.bullet.activate(0, 0, 0, 100);
        // Accumulate just up to the limit (not exceeding)
        this.bullet.update(2.0, 1000, 600);
        // timeAlive == 2.0 which is NOT > 2.0, so alive
        assertFalse(this.bullet.isDead());
    }

    // --- onDestroy does not throw ---

    @Test
    void onDestroy_doesNotThrow() {
        assertDoesNotThrow(this.bullet::onDestroy);
    }

    // --- polygon / body exposure ---

    @Test
    void getPolygon_isNotNull() {
        assertNotNull(this.bullet.getPolygon());
    }

    @Test
    void getBody_isNotNull() {
        assertNotNull(this.bullet.getBody());
    }
}