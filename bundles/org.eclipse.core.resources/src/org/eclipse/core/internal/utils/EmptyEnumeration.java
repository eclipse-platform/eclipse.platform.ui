package org.eclipse.core.internal.utils;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Enumeration;
/**
 * An enumeration that never has any elements.
 */
public class EmptyEnumeration implements Enumeration {
	/**
	 * Singleton instance
	 */
	protected static Enumeration instance = new EmptyEnumeration();
/**
 * EmptyEnumeration constructor comment.
 */
public EmptyEnumeration() {
	super();
}
/**
 * Returns the singleton instance
 */
public static Enumeration getEnumeration() {
	return instance;
}
/**
 * Returns true if this enumeration has more elements.
 */
public boolean hasMoreElements() {
	return false;
}
/**
 * @see Enumeration#nextElement
 */
public Object nextElement() {
	throw new java.util.NoSuchElementException(Policy.bind("utils.noElements"));
}
}
