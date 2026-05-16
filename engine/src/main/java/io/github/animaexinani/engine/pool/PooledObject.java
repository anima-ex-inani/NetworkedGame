package io.github.animaexinani.engine.pool;

/**
 * Represents an object taken from an {@link ObjectPool}.
 */
public interface PooledObject<O> {
    /**
     * Gets the underlying object within this instance.
     * 
     * @return The pooled object
     */
    O get();

    /**
     * Resets the pooled object to its default state.
     */
    void reset();
}
