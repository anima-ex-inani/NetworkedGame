package io.github.animaexinani.engine.util.observer;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A collection that allows listeners to track changes.
 *
 * @param <E> the type of elements in this collection
 */
public interface ObservableCollection<E> extends Collection<E> {
    /**
     * Creates an {@link ObservableCollection} that wraps the specified collection.
     *
     * @param collection the collection to wrap
     * @param <E> the type of elements in the collection
     * @return an observable collection wrapping the specified collection
     */
    static <E> ObservableCollection<E> wrap(Collection<E> collection) {
        return new ObservableCollection<>() {
            private final Collection<E> backingCollection = collection;
            private final List<ElementsAddedEventListener<E>> addedListeners = new CopyOnWriteArrayList<>();
            private final List<ElementsRemovedEventListener<E>> removedListeners = new CopyOnWriteArrayList<>();

            @SuppressWarnings("unchecked")
			@Override
            public <T extends CollectionChangedEventListener<E>> boolean addListener(@NotNull Class<T> type, @NotNull T listener) {
                if (type.equals(ElementsAddedEventListener.class)) {
                    var addedListener = (ElementsAddedEventListener<E>) listener;
                    return this.addedListeners.add(addedListener);
                }
                if (type.equals(ElementsRemovedEventListener.class)) {
                    var removedListener = (ElementsRemovedEventListener<E>) listener;
                    return this.removedListeners.add(removedListener);
                }
                throw new IllegalArgumentException("Unsupported listener type: " + type.getName());
            }

            @SuppressWarnings("unchecked")
			@Override
            public <T extends CollectionChangedEventListener<E>> boolean removeListener(@NotNull Class<T> type, @NotNull T listener) {
                if (type.equals(ElementsAddedEventListener.class)) {
                    var addedListener = (ElementsAddedEventListener<E>) listener;
                    return this.addedListeners.remove(addedListener);
                }
                if (type.equals(ElementsRemovedEventListener.class)) {
                    var removedListener = (ElementsRemovedEventListener<E>) listener;
                    return this.removedListeners.remove(removedListener);
                }
                throw new IllegalArgumentException("Unsupported listener type: " + type.getName());
            }

            @Override
            public int size() {
                return this.backingCollection.size();
            }

            @Override
            public boolean isEmpty() {
                return this.backingCollection.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return this.backingCollection.contains(o);
            }

            @Override
            public @NotNull Iterator<E> iterator() {
                var backingIterator = this.backingCollection.iterator();
                return new Iterator<>() {
                    private E lastReturned = null;

                    @Override
                    public boolean hasNext() {
                        return backingIterator.hasNext();
                    }

                    @Override
                    public E next() {
                        this.lastReturned = backingIterator.next();
                        return this.lastReturned;
                    }

                    @Override
                    public void remove() {
                        backingIterator.remove();
                        var removedItems = Collections.singletonList(this.lastReturned);
                        removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                    }
                };
            }

            @Override
            public @NotNull Object @NotNull [] toArray() {
                return this.backingCollection.toArray();
            }

            @Override
            public @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                return this.backingCollection.toArray(a);
            }

            @Override
            public boolean add(E e) {
                var result = this.backingCollection.add(e);
                if (result) {
                    var addedItems = Collections.singletonList(e);
                    this.addedListeners.forEach(listener -> listener.onElementsAdded(addedItems));
                }

                return result;
            }

            @SuppressWarnings("unchecked")
			@Override
            public boolean remove(Object o) {
                var result = this.backingCollection.remove(o);
                if (result) {
                    var removedItems = Collections.singletonList((E) o);
                    this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                }

                return result;
            }

            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                return this.backingCollection.containsAll(c);
            }

            @Override
            public boolean addAll(@NotNull Collection<? extends E> c) {
                var addedItems = new ArrayList<E>(c.size());
                for (var item : c) {
                    if (this.backingCollection.add(item)) {
                        addedItems.add(item);
                    }
                }

                if (addedItems.isEmpty()) {
                    return false;
                }

                this.addedListeners.forEach(listener -> listener.onElementsAdded(Collections.unmodifiableCollection(addedItems)));
                return true;
            }

            @Override
            public boolean removeAll(@NotNull Collection<?> c) {
                var listCopy = new ArrayList<>(this.backingCollection);
                var result = this.backingCollection.removeAll(c);
                if (result) {
                    var remaining = new HashSet<>(this.backingCollection);
                    var removedItems = listCopy.stream().filter(item -> !remaining.contains(item)).toList();
                    this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                }

                return result;
            }

            @Override
            public boolean retainAll(@NotNull Collection<?> c) {
                var listCopy = new ArrayList<>(this.backingCollection);
                var result = this.backingCollection.retainAll(c);
                if (result) {
                    var remaining = new HashSet<>(this.backingCollection);
                    var removedItems = listCopy.stream().filter(item -> !remaining.contains(item)).toList();
                    this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                }

                return result;
            }

            @Override
            public void clear() {
                if (this.backingCollection.isEmpty()) {
                    return;
                }

                var removedItems = new ArrayList<>(this.backingCollection);
                this.backingCollection.clear();
                this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
            }
        };
    }

    /**
     * Adds a listener for changes to this collection.
     *
     * @param type the type of the listener to add
     * @param listener the listener to add
     * @param <T> the listener type
     * @return {@code true} if the listener was added, {@code false} otherwise
     * @throws IllegalArgumentException if the listener type is not supported
     */
    <T extends CollectionChangedEventListener<E>> boolean addListener(@NotNull Class<T> type, @NotNull T listener);

    /**
     * Removes a listener for changes to this collection.
     *
     * @param type the type of the listener to remove
     * @param listener the listener to remove
     * @param <T> the listener type
     * @return {@code true} if the listener was removed, {@code false} otherwise
     * @throws IllegalArgumentException if the listener type is not supported
     */
    <T extends CollectionChangedEventListener<E>> boolean removeListener(@NotNull Class<T> type, @NotNull T listener);
}
