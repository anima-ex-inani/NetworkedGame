package io.github.animaexinani.engine.util.observer;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A list that allows listeners to track changes.
 *
 * @param <E> the type of elements in this list
 */
public interface ObservableList<E> extends ObservableCollection<E>, List<E> {
    /**
     * Creates an {@link ObservableList} that wraps the specified list.
     *
     * @param list the list to wrap
     * @param <E> the type of elements in the list
     * @return an observable list wrapping the specified list
     */
    static <E> ObservableList<E> wrap(List<E> list) {
        return new ObservableList<>() {
            private final List<E> backingList = list;
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
                    @SuppressWarnings("unchecked") var addedListener = (ElementsAddedEventListener<E>) listener;
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
                return this.backingList.size();
            }

            @Override
            public boolean isEmpty() {
                return this.backingList.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return this.backingList.contains(o);
            }

            @Override
            public @NotNull Iterator<E> iterator() {
                return this.backingList.iterator();
            }

            @Override
            public @NotNull Object @NotNull [] toArray() {
                return this.backingList.toArray();
            }

            @Override
            public @NotNull <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                return this.backingList.toArray(a);
            }

            @Override
            public boolean add(E e) {
                var result = this.backingList.add(e);
                if (result) {
                    var addedItems = Collections.singletonList(e);
                    this.addedListeners.forEach(listener -> listener.onElementsAdded(addedItems));
                }
                return result;
            }

            @SuppressWarnings("unchecked")
			@Override
            public boolean remove(Object o) {
                var result = this.backingList.remove(o);
                if (result) {
                    var removedItems = Collections.singletonList((E) o);
                    this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                }
                return result;
            }

            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                return this.backingList.containsAll(c);
            }

            @Override
            public boolean addAll(@NotNull Collection<? extends E> c) {
                var result = this.backingList.addAll(c);
                if (result) {
                    var addedItems = new ArrayList<>(c);
                    this.addedListeners.forEach(listener -> listener.onElementsAdded(addedItems));
                }
                return result;
            }

            @Override
            public boolean addAll(int index, @NotNull Collection<? extends E> c) {
                var result = this.backingList.addAll(index, c);
                if (result) {
                    var addedItems = new ArrayList<>(c);
                    this.addedListeners.forEach(listener -> listener.onElementsAdded(addedItems));
                }
                return result;
            }

            @Override
            public boolean removeAll(@NotNull Collection<?> c) {
                var listCopy = new ArrayList<>(this.backingList);
                var result = this.backingList.removeAll(c);
                if (result) {
                    var removedItems = listCopy.stream().filter(item -> !this.backingList.contains(item)).toList();
                    this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                }
                return result;
            }

            @Override
            public boolean retainAll(@NotNull Collection<?> c) {
                var listCopy = new ArrayList<>(this.backingList);
                var result = this.backingList.retainAll(c);
                if (result) {
                    var removedItems = listCopy.stream().filter(item -> !this.backingList.contains(item)).toList();
                    this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
                }
                return result;
            }

            @Override
            public void clear() {
                var removedItems = new ArrayList<>(this.backingList);
                this.backingList.clear();
                this.removedListeners.forEach(listener -> listener.onElementsRemoved(removedItems));
            }

            @Override
            public E get(int index) {
                return this.backingList.get(index);
            }

            @Override
            public E set(int index, E element) {
                var oldElement = this.backingList.set(index, element);
                this.removedListeners.forEach(listener -> listener.onElementsRemoved(Collections.singletonList(oldElement)));
                this.addedListeners.forEach(listener -> listener.onElementsAdded(Collections.singletonList(element)));
                return oldElement;
            }

            @Override
            public void add(int index, E element) {
                this.backingList.add(index, element);
                this.addedListeners.forEach(listener -> listener.onElementsAdded(Collections.singletonList(element)));
            }

            @Override
            public E remove(int index) {
                var removedElement = this.backingList.remove(index);
                this.removedListeners.forEach(listener -> listener.onElementsRemoved(Collections.singletonList(removedElement)));
                return removedElement;
            }

            @Override
            public int indexOf(Object o) {
                return this.backingList.indexOf(o);
            }

            @Override
            public int lastIndexOf(Object o) {
                return this.backingList.lastIndexOf(o);
            }

            @Override
            public @NotNull ListIterator<E> listIterator() {
                return this.backingList.listIterator();
            }

            @Override
            public @NotNull ListIterator<E> listIterator(int index) {
                return this.backingList.listIterator(index);
            }

            @Override
            public @NotNull List<E> subList(int fromIndex, int toIndex) {
                return this.backingList.subList(fromIndex, toIndex);
            }
        };
    }
}
