package io.github.animaexinani.engine.util.observer;

import java.util.List;

/**
 * A list that allows listeners to track changes.
 *
 * @param <E> the type of elements in this list
 */
public interface ObservableList<E> extends ObservableCollection<E>, List<E> {
}
