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

package org.eclipse.jface.internal.databinding.api.observable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.internal.databinding.api.observable.list.IListDiffEntry;
import org.eclipse.jface.internal.databinding.api.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.api.observable.list.ListDiffEntry;

/**
 * @since 1.0
 * 
 */
public class Diffs {
	
	/**
	 * @param oldList
	 * @param newList
	 * @return the differences between oldList and newList
	 */
	public static ListDiff computeDiff(List oldList, List newList) {
		List diffEntries = new ArrayList();
		for (Iterator it = oldList.iterator(); it.hasNext();) {
			Object oldElement = it.next();
			diffEntries
					.add(new ListDiffEntry(0, false, oldElement));
		}
		int i = 0;
		for (Iterator it = newList.iterator(); it.hasNext();) {
			Object newElement = it.next();
			diffEntries
					.add(new ListDiffEntry(i++, true, newElement));
		}
		ListDiff listDiff = new ListDiff((IListDiffEntry[]) diffEntries
				.toArray(new IListDiffEntry[diffEntries.size()]));
		return listDiff;
	}
	
	/**
	 * Checks whether the two objects are <code>null</code> -- allowing for
	 * <code>null</code>.
	 * 
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return <code>true</code> if the two objects are equivalent;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean equals(final Object left, final Object right) {
		return left == null ? right == null : ((right != null) && left
				.equals(right));
	}

}
