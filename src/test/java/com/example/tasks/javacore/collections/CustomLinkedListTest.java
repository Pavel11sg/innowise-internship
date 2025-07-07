package com.example.tasks.javacore.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomLinkedListTest {

	private CustomLinkedList<String> list;

	@BeforeEach
	void setUp() {
		list = new CustomLinkedList<>();
	}

	@Test
	void addFirst_shouldAddElementToFront() {
		list.addFirst("A");
		assertEquals(1, list.size());
		assertEquals("A", list.getFirstValue());
	}

	@Test
	void addLast_shouldAddElementToEnd() {
		list.addFirst("A");
		list.addLast("B");
		assertEquals(2, list.size());
		assertEquals("B", list.getLastValue());
	}

	@Test
	void addAtIndex_shouldInsertElementInMiddle() {
		list.addLast("A");
		list.addLast("C");
		list.add(1, "B"); // A - B - C
		assertEquals(3, list.size());
		assertEquals("B", list.getValue(1));
	}

	@Test
	void get_shouldReturnCorrectElementByIndex() {
		list.addLast("A");
		list.addLast("B");
		list.addLast("C");
		assertEquals("B", list.getValue(1));
	}

	@Test
	void get_shouldThrowIfIndexOutOfBounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> list.getValue(0));
	}

	@Test
	void removeFirst_shouldRemoveHead() {
		list.addLast("A");
		list.addLast("B");
		list.removeFirst();
		assertEquals(1, list.size());
		assertEquals("B", list.getFirstValue());
	}

	@Test
	void removeLast_shouldRemoveTail() {
		list.addLast("A");
		list.addLast("B");
		list.removeLast();
		assertEquals(1, list.size());
		assertEquals("A", list.getLastValue());
	}

	@Test
	void removeByIndex_shouldRemoveCorrectElement() {
		list.addLast("A");
		list.addLast("B");
		list.addLast("C");
		list.remove(1); // Remove B
		assertEquals(2, list.size());
		assertEquals("C", list.getValue(1));
	}

	@Test
	void removeByIndex_shouldThrowIfOutOfBounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
	}

	@Test
	void removeFirst_shouldThrowIfListIsEmpty() {
		assertThrows(IllegalStateException.class, () -> list.removeFirst());
	}

	@Test
	void removeLast_shouldThrowIfListIsEmpty() {
		assertThrows(IllegalStateException.class, () -> list.removeLast());
	}

	@Test
	void size_shouldReturnCorrectCount() {
		list.addLast("A");
		list.addLast("B");
		list.addLast("C");
		assertEquals(3, list.size());
	}
}
