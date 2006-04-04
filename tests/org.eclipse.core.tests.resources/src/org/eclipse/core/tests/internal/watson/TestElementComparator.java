/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
	public int compare(Object oldInfo, Object newInfo) {
		if (oldInfo == null) {
			if (newInfo == null)
				return CHANGED;
			return ADDED;
		}
		if (newInfo == null)
			return REMOVED;
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
		if (oldInfo == null && newInfo == null)
			return true;
		if (oldInfo == null || newInfo == null)
			return false;

		return oldInfo.equals(newInfo);
	}
}
