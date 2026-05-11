package io.github.animaexinani.engine.pool;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ObjectPoolTest {

    private static class TestObject {
        int value = 0;
    }

    private static final Consumer<TestObject> EMPTY_RESETTER = obj -> { };

    @Test
    void testAcquireCreatesNewWhenEmpty() {
        AtomicInteger creations = new AtomicInteger(0);
        Supplier<TestObject> supplier = () -> {
            creations.incrementAndGet();
            return new TestObject();
        };
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, supplier);

        PooledObject<TestObject> obj = pool.acquire();
        assertNotNull(obj);
        assertEquals(1, creations.get());
    }

    @Test
    void testAcquireResetsObject() {
        AtomicInteger resets = new AtomicInteger(0);
        Consumer<TestObject> resetter = obj -> {
            resets.incrementAndGet();
            obj.value = 0;
        };
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(resetter, TestObject::new);

        // First acquire should reset newly created object
        PooledObject<TestObject> obj1 = pool.acquire();
        assertEquals(1, resets.get());

        obj1.get().value = 42;
        pool.release(obj1);

        // Re-acquiring should reset again
        PooledObject<TestObject> obj2 = pool.acquire();
        assertEquals(2, resets.get());
        assertEquals(0, obj2.get().value);
        assertSame(obj1, obj2);
    }

    @Test
    void testReleaseAndReuse() {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        PooledObject<TestObject> obj1 = pool.acquire();
        pool.release(obj1);

        PooledObject<TestObject> obj2 = pool.acquire();
        assertSame(obj1, obj2);
    }

    @Test
    void testDoubleReleaseThrowsException() {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        PooledObject<TestObject> obj = pool.acquire();
        pool.release(obj);

        assertThrows(IllegalStateException.class, () -> pool.release(obj));
    }

    @Test
    void testAcquireCount() {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        int count = 5;
        Collection<PooledObject<TestObject>> objects = pool.acquire(count);
        assertEquals(count, objects.size());
        assertEquals(count, objects.stream().distinct().count());
    }

    @Test
    void testAcquireNegativeCountThrowsException() {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        assertThrows(IllegalArgumentException.class, () -> pool.acquire(-1));
    }

    @Test
    void testReleaseCollection() {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        Collection<PooledObject<TestObject>> objects = pool.acquire(3);
        pool.release(objects);

        // All should be reusable
        Collection<PooledObject<TestObject>> reused = pool.acquire(3);
        assertTrue(reused.containsAll(objects));
    }

    @Test
    void testReleaseCollectionWithDuplicatesThrowsException() {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        PooledObject<TestObject> obj = pool.acquire();
        pool.release(obj);

        Collection<PooledObject<TestObject>> objectsToRelease = Arrays.asList(obj);
        assertThrows(IllegalStateException.class, () -> pool.release(objectsToRelease));
    }

    @Test
    void testReleaseObjectFromDifferentPool() {
        ObjectPool<TestObject> pool1 = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);
        ObjectPool<TestObject> pool2 = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);

        PooledObject<TestObject> objFromPool1 = pool1.acquire();

        // pool2 should accept an object that wasn't taken from it,
        // because the PooledObject itself knows how to reset.
        assertDoesNotThrow(() -> pool2.release(objFromPool1));

        PooledObject<TestObject> reusedByPool2 = pool2.acquire();
        assertSame(objFromPool1, reusedByPool2);
    }

    @Test
    void testConcurrentAcquire() throws InterruptedException {
        ObjectPool<TestObject> pool = new BasicObjectPool<TestObject>(EMPTY_RESETTER, TestObject::new);
        int threadCount = 10;
        int acquisitionsPerThread = 100;
        int totalAcquisitions = threadCount * acquisitionsPerThread;

        Set<PooledObject<TestObject>> acquiredObjects = Collections.synchronizedSet(new HashSet<>());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>(threadCount));

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                List<PooledObject<TestObject>> acquired = new ArrayList<>(acquisitionsPerThread);
                try {
                    for (int j = 0; j < acquisitionsPerThread; j++) {
                        acquired.add(pool.acquire());
                    }

                    synchronized (acquiredObjects) {
                        acquiredObjects.addAll(acquired);
                    }
                } catch (Throwable t) {
                    synchronized (failures) {
                        failures.add(t);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Timed out waiting for threads to complete");
        executor.shutdown();

        assertEquals(0, failures.size(), () -> "Worker threads failed: " + failures);
        assertEquals(totalAcquisitions, acquiredObjects.size(), "All concurrently acquired objects must be unique");
    }
}
