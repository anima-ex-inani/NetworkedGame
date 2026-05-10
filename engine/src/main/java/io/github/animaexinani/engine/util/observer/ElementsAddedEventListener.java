package io.github.animaexinani.engine.util.observer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Listener for elements being added to an {@link ObservableCollection}.
 *
 * @param <E> the type of elements in the collection
 */
@FunctionalInterface
public interface ElementsAddedEventListener<E> extends CollectionChangedEventListener {
    /**
     * Called when elements are added to the collection.
     *
     * @param newElements the elements that were added
     */
    void onElementsAdded(@NotNull Collection<? extends E> newElements);
}
