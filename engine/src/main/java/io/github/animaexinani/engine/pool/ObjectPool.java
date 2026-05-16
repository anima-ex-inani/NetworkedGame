package io.github.animaexinani.engine.pool;

import java.util.Collection;

/**
 * Represents a pool of objects which can be used to reduce GC pressure by
 * reusing previously
 * allocated objects.
 */
public interface ObjectPool<O> {
    /**
     * Acquires an object from the pool
     * 
     * @return The acquired object
     * 
     * @implSpec Acquired objects should be guaranteed to be in their default state.
     *           The caller must NOT need
     *           to call {@link PooledObject#reset()} after acquiring an object.
     */
    PooledObject<O> acquire();

    /**
     * Acquires multiple objects from the pool
     * 
     * @param count The number of objects to acquire
     * @return The acquired objects
     * @throws IllegalArgumentException if count is negative
     * 
     * @implSpec If count is 0, an empty collection should be returned.
     * @implSpec Acquired objects should be guaranteed to be in their default state.
     *           The caller must NOT need to call {@link PooledObject#reset()} after
     *           acquiring an object.
     */
    Collection<PooledObject<O>> acquire(int count);

    /**
     * Releases an object back to the pool
     * 
     * @param object The object to release
     * 
     * @apiNote The pooled object should NOT be used after being returned to the
     *          pool.
     */
    void release(PooledObject<O> object);

    /**
     * Releases multiple objects back to the pool
     * 
     * @param objects The objects to release
     * 
     * @apiNote The pooled objects should NOT be used after being returned to the
     *          pool.
     */
    void release(Collection<PooledObject<O>> objects);
}
