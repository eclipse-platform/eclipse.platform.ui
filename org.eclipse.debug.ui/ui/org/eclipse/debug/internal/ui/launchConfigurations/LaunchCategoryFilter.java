/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.activities.WorkbenchActivityHelper;

/**
 * Filters (hides) launch configurations and launch configuration
 * types from a specific launch category.
 */
public class LaunchCategoryFilter extends ViewerFilter {
	
	private String fCategory;
	
	/**
	 * Constructs a filter that hides configurations from a specific
	 * category.
	 * 
	 * @param groupExtension
	 */
	public LaunchCategoryFilter(String category) {
		fCategory = category;
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
			ILaunchConfigurationType type = null;
			ILaunchConfiguration config = null;
			if (parentElement instanceof ILaunchConfigurationType) {
				type = (ILaunchConfigurationType)parentElement;
			}
			if (element instanceof ILaunchConfigurationType) {
				type = (ILaunchConfigurationType)element;
			}
			if (element instanceof ILaunchConfiguration) {
				config = (ILaunchConfiguration)element;
				try {
					type = config.getType();
				} 
				catch (CoreException e) {}
			}
			boolean priv = false;
			if (config != null) {
				try {
					priv = config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
				} catch (CoreException e) {
				}
			} else if (type != null) {
				priv = !type.isPublic();
			}
			if (type != null) {
				return !priv && !equalCategories(type.getCategory(), fCategory) && !WorkbenchActivityHelper.filterItem(new LaunchConfigurationTypeContribution(type));
			}
			return true;
	}
	
	/**
	 * Returns whether the given categories are equal.
	 * 
	 * @param c1 category identifier or <code>null</code>
	 * @param c2 category identifier or <code>null</code>
	 * @return boolean
	 */
	private boolean equalCategories(String c1, String c2) {
		if (c1 == null || c2 == null) {
			return c1 == c2;
		}
		return c1.equals(c2);
	} 

}
