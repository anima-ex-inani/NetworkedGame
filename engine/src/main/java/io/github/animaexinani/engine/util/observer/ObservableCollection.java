package io.github.animaexinani.engine.util.observer;

import org.jetbrains.annotations.NotNull;import java.util.Collection;

/**
 * A collection that allows listeners to track changes.
 *
 * @param <E> the type of elements in this collection
 */
public interface ObservableCollection<E> extends Collection<E> {
    /**
     * Adds a listener for changes to this collection.
     *
     * @param type the type of the listener to add
     * @param listener the listener to add
     * @param <T> the listener type
     * @return {@code true} if the listener was added, {@code false} otherwise
     * @throws IllegalArgumentException if the listener type is not supported
     */
    <T extends CollectionChangedEventListener> boolean addListener(@NotNull Class<T> type, @NotNull T listener);

    /**
     * Removes a listener for changes to this collection.
     *
     * @param type the type of the listener to remove
     * @param listener the listener to remove
     * @param <T> the listener type
     * @return {@code true} if the listener was removed, {@code false} otherwise
     * @throws IllegalArgumentException if the listener type is not supported
     */
    <T extends CollectionChangedEventListener> boolean removeListener(@NotNull Class<T> type, @NotNull T listener);
}
