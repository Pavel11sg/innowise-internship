package com.example.tasks.javacore.collections;

public class CustomLinkedList<T> {

	private class Node {
		T value;
		Node previous;
		Node next;

		Node(T value) {
			this.value = value;
		}
	}

	private Node head;
	private Node tail;
	private int size;

	public CustomLinkedList() {
		this.size = 0;
	}

	public int size() {
		return this.size;
	}

	public void addFirst(T element) {
		Node newNode = new Node(element);
		if (size == 0) {
			head = tail = newNode;
		} else {
			Node currentHead = getFirst();
			currentHead.previous = newNode;
			newNode.next = currentHead;
			head = newNode;
		}
		size++;
	}

	public void addLast(T element) {
		Node newNode = new Node(element);
		if (size == 0) {
			head = tail = newNode;
		} else {
			Node currentTail = getLast();
			currentTail.next = newNode;
			newNode.previous = currentTail;
			tail = newNode;
		}
		size++;
	}

	public void add(int index, T element) {
		Node current = get(index);
		Node newNode = new Node(element);
		Node previousNode = current.previous;
		previousNode.next = newNode;
		newNode.previous = previousNode;
		newNode.next = current;
		current.previous = newNode;
		size++;
	}

	public Node getFirst() {
		return this.head;
	}

	public Node getLast() {
		return this.tail;
	}

	public Node get(int index) {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Индекс вне диапазона!");
		}
		Node current;
		if (index < size() / 2) {
			current = head;
			for (int i = 0; i < index; i++) {
				current = current.next;
			}
		} else {
			current = tail;
			for (int i = size() - 1; i > index; i--) {
				current = current.previous;
			}
		}
		return current;
	}

	public void removeFirst() {
		if (head == null) {
			throw new IllegalStateException("Список пуст");
		}
		if (head == tail) {
			head = tail = null;
		} else {
			head = head.next;
			head.previous = null;
		}
		size--;
	}

	public void removeLast() {
		if (head == null) {
			throw new IllegalStateException("Список пуст");
		}
		if (head == tail) {
			head = tail = null;
		} else {
			tail = tail.previous;
			tail.next = null;
		}
		size--;
	}

	public void remove(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException("Индекс вне допустимого диапазона");
		}
		if (index == 0) {
			removeFirst();
			return;
		}
		if (index == size - 1) {
			removeLast();
			return;
		}
		Node nodeToRemove = get(index);
		Node previousNode = nodeToRemove.previous;
		Node nextNode = nodeToRemove.next;
		previousNode.next = nextNode;
		nextNode.previous = previousNode;
		size--;
	}

	public T getFirstValue() {
		return head != null ? head.value : null;
	}

	public T getLastValue() {
		return tail != null ? tail.value : null;
	}

	public T getValue(int index) {
		return get(index).value;
	}
	public void print() {
		Node element = getFirst();
		while (element != null) {
			System.out.print("[" + element.value + "]" + (element.next != null ? " - " : ".\n"));
			element = element.next;
		}
	}
}
