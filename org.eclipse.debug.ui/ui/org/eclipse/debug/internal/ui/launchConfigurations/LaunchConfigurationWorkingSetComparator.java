package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.text.Collator;
import java.util.Comparator;

import org.eclipse.ui.IWorkingSet;

public class LaunchConfigurationWorkingSetComparator implements Comparator {

	private Collator fCollator= Collator.getInstance();
	
	/**
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	public int compare(Object o1, Object o2) {
		String name1= null;
		String name2= null;
		
		if (o1 instanceof IWorkingSet) {
			name1= ((IWorkingSet)o1).getName();
		}

		if (o2 instanceof IWorkingSet) {
			name2= ((IWorkingSet)o2).getName();
		}

		return fCollator.compare(name1, name2);
	}
}
