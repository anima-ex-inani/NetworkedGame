package io.github.animaexinani.game.classes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletPoolTest {

    private BulletPool pool;

    @BeforeEach
    void setUp() {
        this.pool = new BulletPool();
    }

    // --- obtain ---

    @Test
    void obtain_returnsBulletThatIsAlive() {
        Bullet b = this.pool.obtain(0, 0, 0, 100);
        assertFalse(b.isDead());
    }

    @Test
    void obtain_setsPosition() {
        Bullet b = this.pool.obtain(50.0, 75.0, 0, 100);
        assertEquals(50.0, b.getBody().getTransform().getTranslationX(), 1e-4);
        assertEquals(75.0, b.getBody().getTransform().getTranslationY(), 1e-4);
    }

    @Test
    void obtain_setsVelocityProportionalToSpeed() {
        double speed = 200.0;
        Bullet b = this.pool.obtain(0, 0, 0, speed);
        double vx = b.getBody().getLinearVelocity().x;
        double vy = b.getBody().getLinearVelocity().y;
        double actual = Math.sqrt(vx * vx + vy * vy);
        assertEquals(speed, actual, 1e-3);
    }

    @Test
    void obtain_returnsDifferentBulletsEachCall() {
        Bullet b1 = this.pool.obtain(0, 0, 0, 100);
        Bullet b2 = this.pool.obtain(0, 0, 0, 100);
        assertNotSame(b1, b2);
    }

    @Test
    void obtain_worksWhenPoolIsEmpty() {
        // Drain the pool
        for (int i = 0; i < 100; i++) {
            this.pool.obtain(0, 0, 0, 100);
        }
        // Should still return a valid bullet (creates a new one)
        Bullet extra = this.pool.obtain(0, 0, 0, 100);
        assertNotNull(extra);
        assertFalse(extra.isDead());
    }

    // --- recycle ---

    @Test
    void recycle_stopsBodyMovement() {
        Bullet b = this.pool.obtain(0, 0, 0, 200);
        this.pool.recycle(b);
        assertEquals(0.0, b.getBody().getLinearVelocity().x, 1e-5);
        assertEquals(0.0, b.getBody().getLinearVelocity().y, 1e-5);
    }

    @Test
    void recycle_thenObtain_reusesTheSameBulletInstance() {
        Bullet b1 = this.pool.obtain(0, 0, 0, 100);
        // Drain remaining pool so the recycled bullet is next
        for (int i = 0; i < 99; i++) {
            this.pool.obtain(0, 0, 0, 100);
        }
        // Now recycle b1 back into the empty pool
        this.pool.recycle(b1);
        // Next obtain should return b1
        Bullet b2 = this.pool.obtain(0, 0, 0, 100);
        assertSame(b1, b2);
    }

    @Test
    void recycle_recycledBulletIsActivatedCorrectlyOnNextObtain() {
        Bullet b1 = this.pool.obtain(10.0, 20.0, 0, 100);
        // Drain all remaining bullets so b1 is next after recycle
        for (int i = 0; i < 99; i++) {
            this.pool.obtain(0, 0, 0, 100);
        }
        this.pool.recycle(b1);
        Bullet b2 = this.pool.obtain(99.0, 88.0, 0, 50);
        assertEquals(99.0, b2.getBody().getTransform().getTranslationX(), 1e-4);
        assertEquals(88.0, b2.getBody().getTransform().getTranslationY(), 1e-4);
        assertFalse(b2.isDead());
    }
}