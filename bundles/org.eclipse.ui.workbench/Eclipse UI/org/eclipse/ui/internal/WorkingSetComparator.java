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
package org.eclipse.ui.internal;

import com.ibm.icu.text.Collator;
import java.util.Comparator;

import org.eclipse.ui.IWorkingSet;

/**
 * Compares two working sets by name.
 */
public class WorkingSetComparator implements Comparator {
	
	/**
	 * Static instance of this class.
	 * @since 3.2
	 */
	public static WorkingSetComparator INSTANCE = new WorkingSetComparator();
	
    private Collator fCollator = Collator.getInstance();

    /**
     * Implements Comparator.
     * 
     * @see Comparator#compare(Object, Object)
     */
    public int compare(Object o1, Object o2) {
		String name1 = null;
		String name2 = null;

		if (o1 instanceof IWorkingSet) {
			name1 = ((IWorkingSet) o1).getLabel();
		}

		if (o2 instanceof IWorkingSet) {
			name2 = ((IWorkingSet) o2).getLabel();
		}

		int result = fCollator.compare(name1, name2);
		if (result == 0) { // okay, same name - now try the unique id

			if (o1 instanceof IWorkingSet) {
				name1 = ((IWorkingSet) o1).getName();
			}

			if (o2 instanceof IWorkingSet) {
				name2 = ((IWorkingSet) o2).getName();
			}
			result = name1.compareTo(name2);
		}
		return result;
	}
}
