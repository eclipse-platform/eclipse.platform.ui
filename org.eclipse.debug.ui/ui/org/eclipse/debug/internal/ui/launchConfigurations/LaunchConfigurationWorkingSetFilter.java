package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;

/**
 * Working set filter for launch configuration viewers.
 */
public class LaunchConfigurationWorkingSetFilter extends ViewerFilter {
	
	/**
	 * The working set currently being used as a filter.
	 */
	private IWorkingSet fWorkingSet = null;
	
	private HashSet fWholeTypesSet = new HashSet();	
	private HashSet fPartialTypesAndConfigsSet = new HashSet();

	/**
	 * Returns the working set which is used by this filter.
	 * 
	 * @return the working set
	 */
	public IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}
		
	/**
	 * Sets this filter's working set.
	 * 
	 * @param workingSet the working set
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		fWorkingSet= workingSet;
		buildLookupSet();
	}
	
	/**
	 * Construct sets that speed up calls to 'select()'.  There is one set for
	 * ENTIRE config types, and another for individual configs and their corresponding
	 * config types.  The first contains only config types for which ALL configs should
	 * be visible.  The second contains individually selected configs and config types
	 * for individually selected configs.
	 */
	private void buildLookupSet() {
		fWholeTypesSet.clear();
		fPartialTypesAndConfigsSet.clear();
		
		IWorkingSet workingSet = getWorkingSet();
		if (workingSet != null) {
			IAdaptable[] entries = workingSet.getElements();
			for (int i = 0; i < entries.length; i++) {
				IAdaptable entry = entries[i];
				if (entry instanceof ILaunchConfigurationType) {
					fWholeTypesSet.add(entry);
				} else if (entry instanceof ILaunchConfiguration) {
					ILaunchConfiguration config = (ILaunchConfiguration) entry;
					fPartialTypesAndConfigsSet.add(config);
					try {
						fPartialTypesAndConfigsSet.add(config.getType());
					} catch (CoreException ce) {
					}
				}				
			}
		}
	}
	
	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(Viewer, Object, Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (fWorkingSet == null) {
			return true;
		}

		if (element instanceof ILaunchConfigurationType) {
			return fWholeTypesSet.contains(element) || fPartialTypesAndConfigsSet.contains(element);			
		} else if (element instanceof ILaunchConfiguration) {
			if (fPartialTypesAndConfigsSet.contains(element)) {
				return true;
			}
			try {
				ILaunchConfigurationType configType = ((ILaunchConfiguration)element).getType();
				return fWholeTypesSet.contains(configType);
			} catch (CoreException ce) {
			}
		}

		return false;
	}

}