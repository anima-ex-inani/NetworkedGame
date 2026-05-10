package io.github.animaexinani.engine.util.observer;

import java.util.Collection;

@FunctionalInterface
public interface ElementsRemovedEventListener extends CollectionChangedEventListener {
    void onElementsRemoved(Collection<?> removedElements);
}
