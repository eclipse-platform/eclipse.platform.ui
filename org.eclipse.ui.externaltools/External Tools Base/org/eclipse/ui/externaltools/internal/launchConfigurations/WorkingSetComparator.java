/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.launchConfigurations;

import java.util.Comparator;

import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.ui.IWorkingSet;

/**
 * Comparator for refresh scope launch configuration attribute
 * <code>ATTR_REFRESH_SCOPE</code>.
 */
public class WorkingSetComparator implements Comparator {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		String one= (String)o1;
		String two= (String)o2;
		if (one == null || two == null) {
			if (one == two) {
				return 0;
			} 
			return -1;
		}
		if (one.startsWith("${working_set:") && two.startsWith("${working_set:")) {		  //$NON-NLS-1$//$NON-NLS-2$
			IWorkingSet workingSet1 = RefreshTab.getWorkingSet(one);
			IWorkingSet workingSet2 = RefreshTab.getWorkingSet(two);
			if (workingSet1 == null || workingSet2 == null) {
				if (workingSet1 == workingSet2) {
					return 0;
				} 
				return -1;
			}
			if (workingSet1.equals(workingSet2)) {
				return 0;
			}
			return -1;
		}
		return one.compareTo(two);
	}
}
