package io.github.animaexinani.engine.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a pool of objects which can be used to reduce GC pressure by
 * reusing previously
 * allocated objects.
 */
public interface ObjectPool<O> {
    /**
     * Creates a new object pool using the provided resetter and supplier.
     * 
     * @param <O>      The type of object to pool
     * @param resetter A function that will be called to reset objects from the
     *                 pool.
     * @param supplier A function that will be called to create a new object if the
     *                 pool is empty.
     * @return A new object pool
     */
    static <O> ObjectPool<O> create(Consumer<O> resetter, Supplier<O> supplier) {
        return new ObjectPool<O>() {
            private final Set<PooledObject<O>> available = Collections.synchronizedSet(new HashSet<>());

            @Override
            public PooledObject<O> acquire() {
                synchronized (this.available) {
                    var iterator = this.available.iterator();
                    if (iterator.hasNext()) {
                        var object = iterator.next();
                        iterator.remove();
                        object.reset();
                        return object;
                    }
                }

                var newObject = new PooledObject<O>() {
                    private final O instance = supplier.get();

                    @Override
                    public O get() {
                        return this.instance;
                    }

                    @Override
                    public void reset() {
                        resetter.accept(this.instance);
                    }
                };

                newObject.reset();
                return newObject;
            }

            @Override
            public Collection<PooledObject<O>> acquire(int count) {
                if (count < 0) {
                    throw new IllegalArgumentException("Count must be greater than or equal to 0");
                }

                Collection<PooledObject<O>> objects = new ArrayList<>(count);
                synchronized (this.available) {
                    var iterator = this.available.iterator();
                    int i = 0;
                    while (iterator.hasNext() && i < count) {
                        var object = iterator.next();
                        iterator.remove();
                        objects.add(object);
                        i++;
                    }

                    while (i < count) {
                        objects.add(new PooledObject<O>() {
                            private final O instance = supplier.get();

                            @Override
                            public O get() {
                                return this.instance;
                            }

                            @Override
                            public void reset() {
                                resetter.accept(this.instance);
                            }
                        });
                        i++;
                    }
                }

                objects.forEach(PooledObject::reset);
                return Collections.unmodifiableCollection(objects);
            }

            @Override
            public void release(PooledObject<O> object) {
                synchronized (this.available) {
                    if (this.available.contains(object)) {
                        throw new IllegalStateException("Object already in pool");
                    }
                    this.available.add(object);
                }
            }

            @Override
            public void release(Collection<PooledObject<O>> objects) {
                synchronized (this.available) {
                    for (var object : objects) {
                        if (this.available.contains(object)) {
                            throw new IllegalStateException("Object already in pool");
                        }
                    }

                    this.available.addAll(objects);
                }
            }
        };
    }

    /**
     * Acquires an object from the pool
     * 
     * @return The acquired object
     * 
     * @implSpec
     *           Acquired objects should be guaranteed to be in their default state.
     *           The caller must NOT need
     *           to call {@link PooledObject#reset()} after acquiring an object.
     */
    PooledObject<O> acquire();

    /**
     * Acquires multiple objects from the pool
     * 
     * @param count The number of objects to acquire
     * @return The acquired objects
     * 
     * @implSpec
     *           Acquired objects should be guaranteed to be in their default state.
     *           The caller must NOT need
     *           to call {@link PooledObject#reset()} after acquiring an object.
     */
    Collection<PooledObject<O>> acquire(int count);

    /**
     * Releases an object back to the pool
     * 
     * @param object The object to release
     * 
     * @apiNote
     *          The pooled object should NOT be used after being returned to the
     *          pool.
     */
    void release(PooledObject<O> object);

    /**
     * Releases multiple objects back to the pool
     * 
     * @param objects The objects to release
     * 
     * @apiNote
     *          The pooled objects should NOT be used after being returned to the
     *          pool.
     */
    void release(Collection<PooledObject<O>> objects);
}
