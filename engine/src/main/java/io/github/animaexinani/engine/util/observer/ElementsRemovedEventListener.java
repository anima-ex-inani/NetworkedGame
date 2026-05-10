package io.github.animaexinani.engine.util.observer;

import java.util.Collection;

/**
 * Listener for elements being removed from an {@link ObservableCollection}.
 */
@FunctionalInterface
public interface ElementsRemovedEventListener<E> extends CollectionChangedEventListener<E> {
    /**
     * Called when elements are removed from the collection.
     *
     * @param removedElements the elements that were removed
     */
    void onElementsRemoved(Collection<? extends E> removedElements);
}
