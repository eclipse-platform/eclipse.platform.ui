/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.utils;

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
	throw new java.util.NoSuchElementException(Policy.bind("utils.noElements")); //$NON-NLS-1$
}
}
