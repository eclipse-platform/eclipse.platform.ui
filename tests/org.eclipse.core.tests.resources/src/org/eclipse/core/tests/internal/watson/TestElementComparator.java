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
package org.eclipse.core.tests.internal.watson;

import org.eclipse.core.internal.dtree.NodeComparison;
import org.eclipse.core.internal.watson.IElementComparator;

/**
 * This is what you would expect for an element tree comparator.
 * Clients of the element tree that want specific comparison behaviour
 * must define their own element comparator (without subclassing or
 * otherwise extending this comparator).  Internal element tree operations 
 * rely on the behaviour of this type, and the ElementTree maintainer 
 * reserves the right to change its behaviour as necessary.
 */
public class TestElementComparator implements IElementComparator {
	private static IElementComparator fSingleton;

	static final int ADDED = NodeComparison.K_ADDED;
	static final int REMOVED = NodeComparison.K_REMOVED;
	static final int CHANGED = NodeComparison.K_CHANGED;
/**
 * Force clients to use the singleton
 */
protected TestElementComparator() {
	super();
}
/**
 * Returns the type of change.
 */
public int compare (Object oldInfo, Object newInfo) {
	if (oldInfo == null) {
		if (newInfo == null) {
			return CHANGED;
		} else {
			return ADDED;
		}
	}
	if (newInfo == null) {
		if (oldInfo == null) {
			return CHANGED;
		} else {
			return REMOVED;
		}
	}
	return testEquality(oldInfo, newInfo) ? K_NO_CHANGE : CHANGED;
}
/**
 * Returns the singleton instance
 */
public static IElementComparator getComparator() {
	if (fSingleton == null) {
		fSingleton = new TestElementComparator();
	}
	return fSingleton;
}
/**
 * Makes a comparison based on equality
 */
protected boolean testEquality(Object oldInfo, Object newInfo) {
	if (oldInfo == null && newInfo == null) return true;
	if (oldInfo == null || newInfo == null) return false;
	
	return oldInfo.equals(newInfo);
}
}
