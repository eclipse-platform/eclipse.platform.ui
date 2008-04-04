/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import java.util.Comparator;

import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;

/**
 * @since 3.2
 * 
 */
public class ExtensionPriorityComparator implements Comparator {

	/**
	 * The initialized singleton instance.
	 */
	public static final ExtensionPriorityComparator INSTANCE = new ExtensionPriorityComparator(true);

	/**
	 * The initialized singleton instance.
	 */
	public static final ExtensionPriorityComparator DESCENDING = new ExtensionPriorityComparator(false);
	
	private final int sortAscending;
	
	/**
	 * Creates an instance that sorts according to the given boolean flag.
	 * 
	 * @param toSortAscending
	 *            <code>true</code> for ascending sort order or
	 *            <code>false</code> for descending sort order.
	 */
	public ExtensionPriorityComparator(boolean toSortAscending) {
		sortAscending = toSortAscending ? 1 : -1; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {

		INavigatorContentDescriptor lvalue = null;
		INavigatorContentDescriptor rvalue = null;

		if (o1 instanceof INavigatorContentDescriptor) {
			lvalue = (INavigatorContentDescriptor) o1;
		} else if (o1 instanceof INavigatorContentExtension) {
			lvalue = ((INavigatorContentExtension) o1).getDescriptor();
		}

		if (o2 instanceof INavigatorContentDescriptor) {
			rvalue = (INavigatorContentDescriptor) o2;
		} else if (o2 instanceof INavigatorContentExtension) {
			rvalue = ((INavigatorContentExtension) o2).getDescriptor();
		}

		if (lvalue == null || rvalue == null) {
			return  -1 * sortAscending;
		}

		int c = lvalue.getPriority() - rvalue.getPriority();
		if (c != 0) {
			return c * sortAscending;
		}
		return lvalue.getId().compareTo(rvalue.getId()) * sortAscending;

	}

}
