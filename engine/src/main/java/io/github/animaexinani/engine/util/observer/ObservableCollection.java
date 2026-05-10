package io.github.animaexinani.engine.util.observer;

import org.jetbrains.annotations.NotNull;import java.util.Collection;

public interface ObservableCollection<E> extends Collection<E> {
    <T extends CollectionChangedEventListener> boolean addListener(@NotNull Class<T> type, @NotNull T listener);

    <T extends CollectionChangedEventListener> boolean removeListener(@NotNull Class<T> type, @NotNull T listener);
}
