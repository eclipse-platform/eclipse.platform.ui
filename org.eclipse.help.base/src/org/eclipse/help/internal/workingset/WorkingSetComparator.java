/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import com.ibm.icu.text.Collator;
import java.util.Comparator;

/**
 * Compares two working sets by name.
 */
public class WorkingSetComparator implements Comparator<WorkingSet> {
	private Collator fCollator = Collator.getInstance();

	/**
	 * Implements Comparator.
	 * 
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(WorkingSet o1,WorkingSet o2) {
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
