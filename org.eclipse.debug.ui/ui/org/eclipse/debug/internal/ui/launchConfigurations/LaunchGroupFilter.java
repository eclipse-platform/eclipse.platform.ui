/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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

/**
 * Displays launch configurations for a specific launch group
 */
public class LaunchGroupFilter extends ViewerFilter {
	
	private LaunchGroupExtension fGroup;

	/**
	 * Constructor for ExternalToolsLaunchConfigurationFilter.
	 */
	public LaunchGroupFilter(LaunchGroupExtension groupExtension) {
		super();
		fGroup = groupExtension;
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(
		Viewer viewer,
		Object parentElement,
		Object element) {
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
				} catch (CoreException e) {
				}
			}
			boolean priv = false;
			if (config != null) {
				try {
					priv = config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
				} catch (CoreException e) {
				}
			}
			if (type != null) {
				return !priv && type.supportsMode(fGroup.getMode()) && equalCategories(type.getCategory(), fGroup.getCategory());
			}
			return false;
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
