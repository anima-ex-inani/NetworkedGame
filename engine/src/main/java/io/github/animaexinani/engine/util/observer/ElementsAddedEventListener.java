package io.github.animaexinani.engine.util.observer;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@FunctionalInterface
public interface ElementsAddedEventListener<E> extends CollectionChangedEventListener {
    void onElementsAdded(@NotNull Collection<? extends E> newElements);
}
