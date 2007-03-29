/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * Groups configurations by type.
 * 
 * @since 3.3
 * CONTEXTLAUNCHING
 */
public class LaunchConfigurationComparator extends WorkbenchViewerComparator {

	/**
	 * the map of categories of <code>ILaunchConfigurationType</code>s to <code>Integer</code>s entries
	 */
	private static Map fgCategories;
	
	/**
	 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
	 */
	public int category(Object element) {
		Map map = getCategories();
		if (element instanceof ILaunchConfiguration) {
			ILaunchConfiguration configuration = (ILaunchConfiguration) element;
			try {
				Integer i = (Integer) map.get(configuration.getType());
				if (i != null) {
					return i.intValue();
				}
			} catch (CoreException e) {
			}
		}
		return map.size();
	}
	
	/**
	 * Returns the map of categories
	 * @return the map of categories
	 */
	private Map getCategories() {
		if (fgCategories == null) {
			fgCategories = new HashMap();
			List types = Arrays.asList(DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes());
			Collections.sort(types, new Comparator() {
				public int compare(Object o1, Object o2) {
					ILaunchConfigurationType t1 = (ILaunchConfigurationType) o1;
					ILaunchConfigurationType t2 = (ILaunchConfigurationType) o2;
					return t1.getName().compareTo(t2.getName());
				}
			
			});
			Iterator iterator = types.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				fgCategories.put(iterator.next(), new Integer(i));
				i++;
			}
		}
		return fgCategories;
	}
}
