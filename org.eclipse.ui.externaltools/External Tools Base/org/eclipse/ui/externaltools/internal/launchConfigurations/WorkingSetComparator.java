/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
public class WorkingSetComparator implements Comparator<String> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String o1, String o2) {
		if (o1 == null || o2 == null) {
			if (o1 == o2) {
				return 0;
			}
			return -1;
		}
		if (o1.startsWith("${working_set:") && o2.startsWith("${working_set:")) { //$NON-NLS-1$//$NON-NLS-2$
			IWorkingSet workingSet1 = RefreshTab.getWorkingSet(o1);
			IWorkingSet workingSet2 = RefreshTab.getWorkingSet(o2);
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
		return o1.compareTo(o2);
	}
}
