package org.eclipse.core.internal.utils;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.NoSuchElementException;
import java.util.Enumeration;

/**
 * An object which enumerates the elements of an array
 */

public class ArrayEnumeration implements Enumeration {
	int index;
	int lastElement;
	Object[] elements;
/**
 * Returns new array enumeration over the given object array
 */
public ArrayEnumeration(Object[] elements) {
	this(elements, 0, elements.length - 1);
}
/**
 * Returns new array enumeration over the given object array
 */
public ArrayEnumeration(Object[] elements, int firstElement, int lastElement) {
	super();
	this.elements = elements;
	index = firstElement;
	this.lastElement = lastElement;
}
/**
 * Returns true if this enumeration contains more elements.
 */
public boolean hasMoreElements() {
	return elements != null && index <= lastElement;
}
/**
 * Returns the next element of this enumeration.
 * @exception  NoSuchElementException  if no more elements exist.
 */
public Object nextElement() throws NoSuchElementException {
	if (!hasMoreElements())
		throw new NoSuchElementException();
	return elements[index++];
}
}
