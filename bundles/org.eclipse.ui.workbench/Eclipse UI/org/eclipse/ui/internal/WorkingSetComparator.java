/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Comparator;

import org.eclipse.ui.IWorkingSet;

import com.ibm.icu.text.Collator;

/**
 * Compares two working sets by name.
 */
public class WorkingSetComparator implements Comparator {
	
	private static ThreadLocal INSTANCES = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return new WorkingSetComparator();
		}
	};
	
	public static WorkingSetComparator getInstance() {
		return (WorkingSetComparator) INSTANCES.get();
	}

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
