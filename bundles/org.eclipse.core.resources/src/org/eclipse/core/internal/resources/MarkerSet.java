package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
public class MarkerSet {
	protected static final int MINIMUM_SIZE = 10;
	protected int elementCount = 0;
	protected IMarkerSetElement[] elements;
public MarkerSet() {
	this(MINIMUM_SIZE);
}
public MarkerSet(int capacity) {
	super();
	this.elements = new IMarkerSetElement[Math.max(MINIMUM_SIZE, capacity * 2)];
}
public void add(IMarkerSetElement element) {
	if (element == null)
		return;
	int hash = hashFor(element.getId()) % elements.length;

	// search for an empty slot at the end of the array
	for (int i = hash; i < elements.length; i++) {
		if (elements[i] == null) {
			elements[i] = element;
			elementCount++;
			// grow if necessary
			if (shouldGrow())
				expand();
			return;
		}
	}

	// search for an empty slot at the beginning of the array
	for (int i = 0; i < hash - 1; i++) {
		if (elements[i] == null) {
			elements[i] = element;
			elementCount++;
			// grow if necessary
			if (shouldGrow())
				expand();
			return;
		}
	}

	// if we didn't find a free slot, then try again with the expanded set
	expand();
	add(element);
}
public void addAll(IMarkerSetElement[] elements) {
	for (int i = 0; i < elements.length; i++)
		add(elements[i]);
}
public boolean contains(long id) {
	return get(id) != null;
}
public IMarkerSetElement[] elements() {
	IMarkerSetElement[] result = new IMarkerSetElement[elementCount];
	int j = 0;
	for (int i = 0; i < elements.length; i++) {
		IMarkerSetElement element = elements[i];
		if (element != null) {
			result[j] = element;
			j++;
		}
	}
	return result;
}
/**
 * The array isn't large enough so double its size and rehash
 * all its current values.
 */
protected void expand() {
	IMarkerSetElement[] array = new IMarkerSetElement[elements.length * 2];
	int maxArrayIndex = array.length - 1;
	for (int i = 0; i < elements.length; i++) {
		IMarkerSetElement element = elements[i];
		if (element != null) {
			int hash = hashFor(element.getId()) % array.length;
			while (array[hash] != null) {
				hash++;
				if (hash > maxArrayIndex)
					hash = 0;
			}
			array[hash] = element;
		}
	}
	elements = array;
}
public IMarkerSetElement get(long id) {
	int hash = hashFor(id) % elements.length;

	// search the last half of the array
	for (int i = hash; i < elements.length; i++) {
		IMarkerSetElement element = elements[i];
		if (element != null && element.getId() == id)
			return element;
	}

	// search the beginning of the array
	for (int i = 0; i < hash - 1; i++) {
		IMarkerSetElement element = elements[i];
		if (element != null && element.getId() == id)
			return element;
	}

	// marker info not found so return null
	return null;
}
private int hashFor(long id) {
	return Math.abs((int) id);
}
public boolean isEmpty() {
	return elementCount == 0;
}
/**
 * The element at the given index has been removed so move
 * elements to keep the set properly hashed.
 */
protected void rehashTo(int anIndex) {

	int target = anIndex;
	int index = anIndex + 1;
	if (index >= elements.length)
		index = 0;
	IMarkerSetElement element = elements[index];
	while (element != null) {
		int hash = hashFor(element.getId()) % elements.length + 1;
		if (index < target) {
			if (hash > target || hash <= index) {
				elements[target] = element;
				target = index;
			}
		} else {
			if (hash > target && hash <= index) {
				elements[target] = element;
				target = index;
			}
		}
		index++;
		if (index >= elements.length)
			index = 0;
		element = elements[index];
	}
	elements[target] = null;
}
public void remove(long id) {
	int hash = hashFor(id) % elements.length;

	for (int i = hash; i < elements.length; i++) {
		IMarkerSetElement element = elements[i];
		if (element == null)
			return;
		if (element.getId() == id) {
			rehashTo(i);
			elementCount--;
		}
	}

	for (int i = 0; i < hash - 1; i++) {
		IMarkerSetElement element = elements[i];
		if (element == null)
			return;
		if (element.getId() == id) {
			rehashTo(i);
			elementCount--;
		}
	}
}
public void remove(IMarkerSetElement element) {
	remove(element.getId());
}
public void removeAll(IMarkerSetElement[] elements) {
	for (int i = 0; i < elements.length; i++)
		remove(elements[i]);
}
private boolean shouldGrow() {
	return elementCount > elements.length * 0.75;
}
public int size() {
	return elementCount;
}
}
