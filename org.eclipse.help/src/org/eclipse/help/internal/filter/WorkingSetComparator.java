package org.eclipse.help.internal.filter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.text.Collator;
import java.util.Comparator;

/**
 * Compares two working sets by name.
 */
public class WorkingSetComparator implements Comparator {
	private Collator fCollator = Collator.getInstance();

	/**
	 * Implements Comparator.
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		String name1 = null;
		String name2 = null;

		if (o1 instanceof WorkingSet)
			name1 = ((WorkingSet) o1).getName();

		if (o2 instanceof WorkingSet)
			name2 = ((WorkingSet) o2).getName();

		if (name1 == null || name2 == null)
			return -1;
			
		return fCollator.compare(name1, name2);
	}
}