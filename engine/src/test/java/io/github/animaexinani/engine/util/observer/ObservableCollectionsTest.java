package io.github.animaexinani.engine.util.observer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ObservableCollectionsTest {

    // Existing tests...

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

    // Additional robust tests for ObservableCollection

    @Nested
    @DisplayName("ObservableCollection Basic Operations")
    class ObservableCollectionBasicOperations {

        @Test
        @DisplayName("Test adding single element to empty collection triggers add listener")
        void testAddSingleElementToEmptyCollection() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            var itemToAdd = "item";

            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> {
                assertEquals(1, addedItems.size());
                for (var addedItem : addedItems) {
                    assertEquals(itemToAdd, addedItem);
                }
            });

            var result = observableCollection.add(itemToAdd);
            assertTrue(result);
            assertEquals(1, observableCollection.size());
        }

        @Test
        @DisplayName("Test adding duplicate element does not trigger add listener")
        void testAddDuplicateElement() {
            var itemToAdd = "item";

            var backingCollection = new HashSet<String>();
            backingCollection.add(itemToAdd);

            var observableCollection = ObservableCollections.fromCollection(backingCollection);
            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> {
                fail("Add listener should not be triggered");
            });

            var result = observableCollection.add(itemToAdd);
            assertFalse(result);
            assertEquals(1, observableCollection.size());

            var backingCollection2 = new ArrayList<String>();
            backingCollection2.add(itemToAdd);

            var observableCollection2 = ObservableCollections.fromCollection(backingCollection2);
            observableCollection2.addListener(ElementsAddedEventListener.class, addedItems -> {
                assertEquals(1, addedItems.size());
                for (var addedItem : addedItems) {
                    assertEquals(itemToAdd, addedItem);
                }
            });

            var result2 = observableCollection2.add(itemToAdd);
            assertTrue(result2);
            assertEquals(2, observableCollection2.size());
        }

        @Test
        @DisplayName("Test removing existing element triggers remove listener")
        void testRemoveExistingElement() {
            var itemToRemove = "item";
            var backingCollection = new ArrayList<String>();
            backingCollection.add(itemToRemove);
            var observableCollection = ObservableCollections.fromCollection(backingCollection);

            AtomicInteger removedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.addAndGet(removedItems.size());
                assertTrue(removedItems.contains(itemToRemove));
            });

            var result = observableCollection.remove(itemToRemove);
            assertTrue(result);
            assertEquals(1, removedCount.get());
            assertTrue(observableCollection.isEmpty());
        }

        @Test
        @DisplayName("Test removing non-existing element does not trigger remove listener")
        void testRemoveNonExistingElement() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());

            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> {
                fail("Remove listener should not be triggered");
            });

            var result = observableCollection.remove("non-existing");
            assertFalse(result);
            assertTrue(observableCollection.isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Test handling null and empty elements")
        void testNullAndEmptyElements(String value) {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            AtomicInteger addedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.addAndGet(addedItems.size());
                assertTrue(addedItems.contains(value));
            });

            var result = observableCollection.add(value);
            assertTrue(result);
            assertEquals(1, addedCount.get());
            assertTrue(observableCollection.contains(value));

            AtomicInteger removedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.addAndGet(removedItems.size());
                assertTrue(removedItems.contains(value));
            });

            var removeResult = observableCollection.remove(value);
            assertTrue(removeResult);
            assertEquals(1, removedCount.get());
            assertFalse(observableCollection.contains(value));
        }
    }

    @Nested
    @DisplayName("ObservableCollection Bulk Operations")
    class ObservableCollectionBulkOperations {

        @Test
        @DisplayName("Test addAll with multiple elements triggers add listener once")
        void testAddAllMultipleElements() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            var itemsToAdd = List.of("item1", "item2", "item3");

            AtomicInteger addedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.incrementAndGet();
                assertEquals(3, addedItems.size());
                assertTrue(addedItems.containsAll(itemsToAdd));
            });

            var result = observableCollection.addAll(itemsToAdd);
            assertTrue(result);
            assertEquals(1, addedCount.get());
            assertEquals(3, observableCollection.size());
        }

        @Test
        @DisplayName("Test addAll with empty collection does not trigger add listener")
        void testAddAllEmptyCollection() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());

            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> {
                fail("Add listener should not be triggered");
            });

            var result = observableCollection.addAll(List.of());
            assertFalse(result);
            assertTrue(observableCollection.isEmpty());
        }

        @Test
        @DisplayName("Test removeAll removes specified elements and triggers remove listener")
        void testRemoveAllSpecifiedElements() {
            var items = new ArrayList<>(List.of("item1", "item2", "item3"));
            var observableCollection = ObservableCollections.fromCollection(items);
            var itemsToRemove = List.of("item1", "item3");

            AtomicInteger removedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.incrementAndGet();
                assertEquals(2, removedItems.size());
                assertTrue(removedItems.containsAll(itemsToRemove));
            });

            var result = observableCollection.removeAll(itemsToRemove);
            assertTrue(result);
            assertEquals(1, removedCount.get());
            assertEquals(1, observableCollection.size());
            assertTrue(observableCollection.contains("item2"));
        }

        @Test
        @DisplayName("Test retainAll keeps only specified elements and triggers remove listener")
        void testRetainAllSpecifiedElements() {
            var items = new ArrayList<>(List.of("item1", "item2", "item3"));
            var observableCollection = ObservableCollections.fromCollection(items);
            var itemsToRetain = List.of("item2");

            AtomicInteger removedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.incrementAndGet();
                assertEquals(2, removedItems.size());
                assertTrue(removedItems.contains("item1"));
                assertTrue(removedItems.contains("item3"));
            });

            var result = observableCollection.retainAll(itemsToRetain);
            assertTrue(result);
            assertEquals(1, removedCount.get());
            assertEquals(1, observableCollection.size());
            assertTrue(observableCollection.contains("item2"));
        }

        @Test
        @DisplayName("Test clear removes all elements and triggers remove listener")
        void testClearAllElements() {
            var items = new ArrayList<>(List.of("item1", "item2"));
            var observableCollection = ObservableCollections.fromCollection(items);

            AtomicInteger removedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.incrementAndGet();
                assertEquals(2, removedItems.size());
                assertTrue(removedItems.contains("item1"));
                assertTrue(removedItems.contains("item2"));
            });

            observableCollection.clear();
            assertEquals(1, removedCount.get());
            assertTrue(observableCollection.isEmpty());
        }
    }

    @Nested
    @DisplayName("ObservableCollection Listener Management")
    class ObservableCollectionListenerManagement {

        @Test
        @DisplayName("Test adding multiple add listeners")
        void testMultipleAddListeners() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            AtomicInteger count1 = new AtomicInteger(0);
            AtomicInteger count2 = new AtomicInteger(0);

            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> count1.incrementAndGet());
            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> count2.incrementAndGet());

            observableCollection.add("item");
            assertEquals(1, count1.get());
            assertEquals(1, count2.get());
        }

        @Test
        @DisplayName("Test adding multiple remove listeners")
        void testMultipleRemoveListeners() {
            var items = new ArrayList<>(List.of("item"));
            var observableCollection = ObservableCollections.fromCollection(items);
            AtomicInteger count1 = new AtomicInteger(0);
            AtomicInteger count2 = new AtomicInteger(0);

            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> count1.incrementAndGet());
            observableCollection.addListener(ElementsRemovedEventListener.class, removedItems -> count2.incrementAndGet());

            observableCollection.remove("item");
            assertEquals(1, count1.get());
            assertEquals(1, count2.get());
        }

        @Test
        @DisplayName("Test removing listeners prevents further notifications")
        void testRemoveListener() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            AtomicInteger count1 = new AtomicInteger(0);
            ElementsAddedEventListener<String> listener = addedItems -> count1.incrementAndGet();

            observableCollection.addListener(ElementsAddedEventListener.class, listener);
            observableCollection.add("item1");
            assertEquals(1, count1.get());

            observableCollection.removeListener(ElementsAddedEventListener.class, listener);
            observableCollection.add("item2");
            assertEquals(1, count1.get());
        }

        @Test
        @DisplayName("Test adding unsupported listener type throws exception")
        void testUnsupportedListenerType() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            assertThrows(IllegalArgumentException.class, () -> {
                observableCollection.addListener(CollectionChangedEventListener.class, new CollectionChangedEventListener() {});
            });
        }

        @Test
        @DisplayName("Test removing unsupported listener type throws exception")
        void testRemoveUnsupportedListenerType() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            assertThrows(IllegalArgumentException.class, () -> {
                observableCollection.removeListener(CollectionChangedEventListener.class, new CollectionChangedEventListener() {});
            });
        }
    }

    @Nested
    @DisplayName("ObservableCollection Edge Cases")
    class ObservableCollectionEdgeCases {

        @Test
        @DisplayName("Test operations on empty collection")
        void testEmptyCollectionOperations() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());

            assertFalse(observableCollection.remove("item"));
            assertFalse(observableCollection.removeAll(List.of("item")));
            assertFalse(observableCollection.retainAll(List.of("item")));
            assertEquals(0, observableCollection.size());
            assertTrue(observableCollection.isEmpty());
        }

        @Test
        @DisplayName("Test concurrent modifications during iteration")
        void testConcurrentModifications() {
            // Using a collection that supports concurrent modification for this test
            var observableCollection = ObservableCollections.fromCollection(new java.util.concurrent.CopyOnWriteArrayList<String>());
            observableCollection.add("item1");
            observableCollection.add("item2");

            assertDoesNotThrow(() -> {
                for (String item : observableCollection) {
                    observableCollection.add("newItem");
                }
            });
        }

        @Test
        @DisplayName("Test listener throws exception behavior")
        void testListenerExceptionHandling() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            AtomicInteger count = new AtomicInteger(0);

            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> {
                throw new RuntimeException("Test exception");
            });
            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> count.incrementAndGet());

            // Current implementation: exception in one listener stops notification of others
            assertThrows(RuntimeException.class, () -> observableCollection.add("item"));
            assertEquals(0, count.get());
        }

        @Test
        @DisplayName("Test collection backed by different collection types")
        void testDifferentBackingCollections() {
            var list = new ArrayList<String>();
            var observableList = ObservableCollections.fromCollection(list);
            observableList.add("item");
            assertTrue(list.contains("item"));

            var set = new HashSet<String>();
            var observableSet = ObservableCollections.fromCollection(set);
            observableSet.add("item");
            assertTrue(set.contains("item"));
        }
    }

    @Nested
    @DisplayName("ObservableList Specific Operations")
    class ObservableListSpecificOperations {

        @Test
        @DisplayName("Test get element at valid index")
        void testGetAtValidIndex() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0", "item1", "item2")));
            assertEquals("item0", observableList.get(0));
            assertEquals("item1", observableList.get(1));
            assertEquals("item2", observableList.get(2));
        }

        @Test
        @DisplayName("Test get element at invalid index throws exception")
        void testGetAtInvalidIndex() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0")));
            assertThrows(IndexOutOfBoundsException.class, () -> observableList.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> observableList.get(1));
        }

        @Test
        @DisplayName("Test set element at index triggers both add and remove listeners")
        void testSetAtIndex() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0")));
            AtomicInteger addedCount = new AtomicInteger(0);
            AtomicInteger removedCount = new AtomicInteger(0);

            observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.incrementAndGet();
                assertTrue(addedItems.contains("newItem"));
            });
            observableList.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.incrementAndGet();
                assertTrue(removedItems.contains("item0"));
            });

            var old = observableList.set(0, "newItem");
            assertEquals("item0", old);
            assertEquals(1, addedCount.get());
            assertEquals(1, removedCount.get());
            assertEquals("newItem", observableList.get(0));
        }

        @Test
        @DisplayName("Test add element at specific index")
        void testAddAtSpecificIndex() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0", "item1")));
            AtomicInteger addedCount = new AtomicInteger(0);
            observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.incrementAndGet();
                assertTrue(addedItems.contains("newItem"));
            });

            observableList.add(1, "newItem");
            assertEquals(1, addedCount.get());
            assertEquals(3, observableList.size());
            assertEquals("newItem", observableList.get(1));
            assertEquals("item1", observableList.get(2));
        }

        @Test
        @DisplayName("Test remove element at specific index")
        void testRemoveAtSpecificIndex() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0", "item1")));
            AtomicInteger removedCount = new AtomicInteger(0);
            observableList.addListener(ElementsRemovedEventListener.class, removedItems -> {
                removedCount.incrementAndGet();
                assertTrue(removedItems.contains("item0"));
            });

            var removed = observableList.remove(0);
            assertEquals("item0", removed);
            assertEquals(1, removedCount.get());
            assertEquals(1, observableList.size());
            assertEquals("item1", observableList.get(0));
        }

        @Test
        @DisplayName("Test indexOf and lastIndexOf operations")
        void testIndexOfOperations() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item", "duplicate", "duplicate")));
            assertEquals(0, observableList.indexOf("item"));
            assertEquals(1, observableList.indexOf("duplicate"));
            assertEquals(2, observableList.lastIndexOf("duplicate"));
            assertEquals(-1, observableList.indexOf("non-existing"));
        }

        @Test
        @DisplayName("Test addAll at specific index")
        void testAddAllAtIndex() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0", "item3")));
            var itemsToAdd = List.of("item1", "item2");
            AtomicInteger addedCount = new AtomicInteger(0);
            observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.incrementAndGet();
                assertEquals(2, addedItems.size());
                assertTrue(addedItems.containsAll(itemsToAdd));
            });

            var result = observableList.addAll(1, itemsToAdd);
            assertTrue(result);
            assertEquals(1, addedCount.get());
            assertEquals(4, observableList.size());
            assertEquals("item1", observableList.get(1));
            assertEquals("item2", observableList.get(2));
            assertEquals("item3", observableList.get(3));
        }
    }

    @Nested
    @DisplayName("ObservableList Bulk Operations")
    class ObservableListBulkOperations {

        @Test
        @DisplayName("Test removeAll preserves order of remaining elements")
        void testRemoveAllOrderPreservation() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item1", "item2", "item3", "item4")));
            observableList.removeAll(List.of("item1", "item3"));
            assertEquals(2, observableList.size());
            assertEquals("item2", observableList.get(0));
            assertEquals("item4", observableList.get(1));
        }

        @Test
        @DisplayName("Test retainAll preserves order of retained elements")
        void testRetainAllOrderPreservation() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item1", "item2", "item3", "item4")));
            observableList.retainAll(List.of("item2", "item4"));
            assertEquals(2, observableList.size());
            assertEquals("item2", observableList.get(0));
            assertEquals("item4", observableList.get(1));
        }

        @Test
        @DisplayName("Test subList operations")
        void testSubListOperations() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0", "item1", "item2")));
            var subList = observableList.subList(1, 3);
            assertEquals(2, subList.size());
            assertEquals("item1", subList.get(0));
            assertEquals("item2", subList.get(1));

            // Modifications to subList affect original backing list
            subList.set(0, "newItem1");
            assertEquals("newItem1", observableList.get(1));
        }

        @Test
        @DisplayName("Test listIterator operations")
        void testListIteratorOperations() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item0", "item1")));
            var iterator = observableList.listIterator();
            assertTrue(iterator.hasNext());
            assertEquals("item0", iterator.next());
            assertTrue(iterator.hasNext());
            assertEquals("item1", iterator.next());
            assertFalse(iterator.hasNext());
        }
    }

    @Nested
    @DisplayName("ObservableList Edge Cases")
    class ObservableListEdgeCases {

        @Test
        @DisplayName("Test operations with large lists")
        void testLargeListOperations() {
            var observableList = ObservableCollections.fromList(new ArrayList<Integer>());
            int count = 1000;
            AtomicInteger addedCount = new AtomicInteger(0);
            observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.addAndGet(addedItems.size());
            });

            for (int i = 0; i < count; i++) {
                observableList.add(i);
            }
            assertEquals(count, observableList.size());
            assertEquals(count, addedCount.get());
        }

        @Test
        @DisplayName("Test duplicate elements handling")
        void testDuplicateElements() {
            var observableList = ObservableCollections.fromList(new ArrayList<String>());
            observableList.add("item");
            observableList.add("item");
            assertEquals(2, observableList.size());
            assertEquals(0, observableList.indexOf("item"));
            assertEquals(1, observableList.lastIndexOf("item"));
        }

        @Test
        @DisplayName("Test null elements in list")
        void testNullElementsInList() {
            var observableList = ObservableCollections.fromList(new ArrayList<String>());
            AtomicInteger addedCount = new AtomicInteger(0);
            observableList.addListener(ElementsAddedEventListener.class, addedItems -> {
                addedCount.addAndGet(addedItems.size());
                assertTrue(addedItems.contains(null));
            });

            observableList.add(null);
            assertEquals(1, observableList.size());
            assertNull(observableList.get(0));
            assertEquals(1, addedCount.get());
        }

        @Test
        @DisplayName("Test list operations after clear")
        void testOperationsAfterClear() {
            var observableList = ObservableCollections.fromList(new ArrayList<>(List.of("item")));
            observableList.clear();
            assertTrue(observableList.isEmpty());

            AtomicInteger addedCount = new AtomicInteger(0);
            observableList.addListener(ElementsAddedEventListener.class, addedItems -> addedCount.addAndGet(addedItems.size()));

            observableList.add("newItem");
            assertEquals(1, observableList.size());
            assertEquals(1, addedCount.get());
        }
    }

    @Nested
    @DisplayName("Thread Safety Tests")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Test concurrent listener additions and removals")
        void testConcurrentListenerManagement() throws InterruptedException {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            int threadCount = 10;
            int operationsPerThread = 100;
            var startLatch = new java.util.concurrent.CountDownLatch(1);
            var endLatch = new java.util.concurrent.CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            ElementsAddedEventListener<String> listener = items -> {};
                            observableCollection.addListener(ElementsAddedEventListener.class, listener);
                            observableCollection.removeListener(ElementsAddedEventListener.class, listener);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(endLatch.await(5, java.util.concurrent.TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("Test concurrent collection modifications")
        void testConcurrentModifications() throws InterruptedException {
            // Using a thread-safe backing collection
            var observableCollection = ObservableCollections.fromCollection(new java.util.concurrent.CopyOnWriteArrayList<String>());
            int threadCount = 10;
            int operationsPerThread = 100;
            var startLatch = new java.util.concurrent.CountDownLatch(1);
            var endLatch = new java.util.concurrent.CountDownLatch(threadCount);

            AtomicInteger addedCount = new AtomicInteger(0);
            observableCollection.addListener(ElementsAddedEventListener.class, addedItems -> addedCount.addAndGet(addedItems.size()));

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            observableCollection.add("item");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            assertTrue(endLatch.await(5, java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(threadCount * operationsPerThread, observableCollection.size());
            assertEquals(threadCount * operationsPerThread, addedCount.get());
        }

        @Test
        @DisplayName("Test listener notification order")
        void testListenerNotificationOrder() {
            var observableCollection = ObservableCollections.fromCollection(new ArrayList<String>());
            List<Integer> order = new java.util.concurrent.CopyOnWriteArrayList<>();

            observableCollection.addListener(ElementsAddedEventListener.class, items -> order.add(1));
            observableCollection.addListener(ElementsAddedEventListener.class, items -> order.add(2));
            observableCollection.addListener(ElementsAddedEventListener.class, items -> order.add(3));

            observableCollection.add("item");
            assertEquals(List.of(1, 2, 3), order);
        }
    }
}
