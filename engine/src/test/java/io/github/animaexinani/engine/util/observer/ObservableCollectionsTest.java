package io.github.animaexinani.engine.util.observer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ObservableCollectionsTest {

    @Test
    void testFromList_Add() {
        List<String> list = new ArrayList<>();
        ObservableList<String> observableList = ObservableCollections.fromList(list);

        AtomicInteger addedCount = new AtomicInteger(0);
        observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
            addedCount.addAndGet(addedItems.size());
            assertTrue(addedItems.contains("item1"));
        });

        observableList.add("item1");
        assertEquals(1, addedCount.get());
        assertEquals(1, list.size());
        assertEquals("item1", list.get(0));
    }

    @Test
    void testFromList_AddAtIndex() {
        List<String> list = new ArrayList<>();
        list.add("item0");
        ObservableList<String> observableList = ObservableCollections.fromList(list);

        AtomicInteger addedCount = new AtomicInteger(0);
        observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
            addedCount.addAndGet(addedItems.size());
            assertTrue(addedItems.contains("item1"));
        });

        observableList.add(0, "item1");
        assertEquals(1, addedCount.get());
        assertEquals(2, list.size());
        assertEquals("item1", list.get(0));
    }

    @Test
    void testFromList_Remove() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        ObservableList<String> observableList = ObservableCollections.fromList(list);

        AtomicInteger removedCount = new AtomicInteger(0);
        observableList.addListener(ElementsRemovedEventListener.class, removedItems -> {
            removedCount.addAndGet(removedItems.size());
            assertTrue(removedItems.contains("item1"));
        });

        observableList.remove("item1");
        assertEquals(1, removedCount.get());
        assertTrue(list.isEmpty());
    }

    @Test
    void testFromList_RemoveAtIndex() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        ObservableList<String> observableList = ObservableCollections.fromList(list);

        AtomicInteger removedCount = new AtomicInteger(0);
        observableList.addListener(ElementsRemovedEventListener.class, removedItems -> {
            removedCount.addAndGet(removedItems.size());
            assertTrue(removedItems.contains("item1"));
        });

        observableList.remove(0);
        assertEquals(1, removedCount.get());
        assertTrue(list.isEmpty());
    }

    @Test
    void testFromList_Set() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        ObservableList<String> observableList = ObservableCollections.fromList(list);

        AtomicInteger addedCount = new AtomicInteger(0);
        AtomicInteger removedCount = new AtomicInteger(0);

        observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
            addedCount.addAndGet(addedItems.size());
            assertTrue(addedItems.contains("item2"));
        });
        observableList.addListener(ElementsRemovedEventListener.class, removedItems -> {
            removedCount.addAndGet(removedItems.size());
            assertTrue(removedItems.contains("item1"));
        });

        observableList.set(0, "item2");
        assertEquals(1, addedCount.get());
        assertEquals(1, removedCount.get());
        assertEquals(1, list.size());
        assertEquals("item2", list.get(0));
    }
}
