/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import java.util.Comparator;

import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorContentExtension;

/**
 * @since 3.2
 */
public class ExtensionSequenceNumberComparator implements Comparator {

	/**
	 * The initialized singleton instance.
	 */
	public static final ExtensionSequenceNumberComparator INSTANCE = new ExtensionSequenceNumberComparator(true);

	/**
	 * The initialized singleton instance.
	 */
	public static final ExtensionSequenceNumberComparator DESCENDING = new ExtensionSequenceNumberComparator(false);

	private final int sortAscending;

	/**
	 * Creates an instance that sorts according to the given boolean flag.
	 *
	 * @param toSortAscending
	 *            <code>true</code> for ascending sort order or
	 *            <code>false</code> for descending sort order.
	 */
	public ExtensionSequenceNumberComparator(boolean toSortAscending) {
		sortAscending = toSortAscending ? 1 : -1;
	}

	@Override
	public int compare(Object o1, Object o2) {

		INavigatorContentDescriptor lvalue = null;
		INavigatorContentDescriptor rvalue = null;

		if (o1 instanceof INavigatorContentDescriptor) {
			lvalue = (INavigatorContentDescriptor) o1;
		} else if (o1 instanceof NavigatorContentExtension) {
			lvalue = ((NavigatorContentExtension) o1).getDescriptor();
		}

		if (o2 instanceof INavigatorContentDescriptor) {
			rvalue = (INavigatorContentDescriptor) o2;
		} else if (o2 instanceof INavigatorContentExtension) {
			rvalue = ((NavigatorContentExtension) o2).getDescriptor();
		}

		if (lvalue == null || rvalue == null) {
			return  -1 * sortAscending;
		}

		int c = lvalue.getSequenceNumber() - rvalue.getSequenceNumber();
		if (c != 0) {
			return c * sortAscending;
		}
		return 0;

	}

}
