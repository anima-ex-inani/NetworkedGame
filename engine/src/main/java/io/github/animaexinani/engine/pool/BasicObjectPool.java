package io.github.animaexinani.engine.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicObjectPool<O> implements ObjectPool<O> {
    static final class BasicPooledObject<O> implements PooledObject<O> {
        private final O instance;
        private final Consumer<O> resetter;

        @Override
        public O get() {
            return this.instance;
        }

        @Override
        public void reset() {
            this.resetter.accept(this.instance);
        }

        public BasicPooledObject(O instance, Consumer<O> resetter) {
            this.instance = instance;
            this.resetter = resetter;
        }
    }

    private final Set<PooledObject<O>> available = Collections.synchronizedSet(new HashSet<>());
    private final Supplier<O> supplier;
    private final Consumer<O> resetter;

    public BasicObjectPool(Consumer<O> resetter, Supplier<O> supplier) {
        this.supplier = supplier;
        this.resetter = resetter;
    }

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

        var newObject = new BasicPooledObject<>(this.supplier.get(), this.resetter);

        newObject.reset();
        return newObject;
    }

    @Override
    public Collection<PooledObject<O>> acquire(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be greater than or equal to 0");
        }

        Collection<PooledObject<O>> objects = new ArrayList<>(count);
        int i = 0;
        synchronized (this.available) {
            var iterator = this.available.iterator();
            while (iterator.hasNext() && i < count) {
                var object = iterator.next();
                iterator.remove();
                objects.add(object);
                i++;
            }
        }

        while (i < count) {
            var newObject = new BasicPooledObject<>(this.supplier.get(), this.resetter);
            newObject.reset();
            objects.add(newObject);
            i++;
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
            Set<PooledObject<O>> seen = new HashSet<>();
            for (var object : objects) {
                if (this.available.contains(object) || !seen.add(object)) {
                    throw new IllegalStateException("Object already in pool");
                }
            }

            this.available.addAll(objects);
        }
    }
}
