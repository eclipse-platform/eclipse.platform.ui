/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.text.Collator;
import java.util.Comparator;

import org.eclipse.ui.IWorkingSet;

class WorkingSetComparator implements Comparator {

	private Collator fCollator = Collator.getInstance();

	/*
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		String name1 = null;
		String name2 = null;

		if (o1 instanceof IWorkingSet)
			name1 = ((IWorkingSet) o1).getName();

		if (o2 instanceof IWorkingSet)
			name2 = ((IWorkingSet) o2).getName();

		return fCollator.compare(name1, name2);
	}
}