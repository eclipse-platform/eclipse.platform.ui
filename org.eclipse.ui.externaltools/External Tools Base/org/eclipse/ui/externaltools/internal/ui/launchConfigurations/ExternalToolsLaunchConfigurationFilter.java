package org.eclipse.ui.externaltools.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

/**
 * Filters all but the "external tools" launch configurations
 */
public class ExternalToolsLaunchConfigurationFilter extends ViewerFilter {

	/**
	 * Constructor for ExternalToolsLaunchConfigurationFilter.
	 */
	public ExternalToolsLaunchConfigurationFilter() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(
		Viewer viewer,
		Object parentElement,
		Object element) {
			ILaunchConfigurationType type = null;
			if (parentElement instanceof ILaunchConfigurationType) {
				type = (ILaunchConfigurationType)parentElement;
			}
			if (element instanceof ILaunchConfigurationType) {
				type = (ILaunchConfigurationType)element;
			}
			if (element instanceof ILaunchConfiguration) {
				try {
					type = ((ILaunchConfiguration)element).getType();
				} catch (CoreException e) {
				}
			}
			if (type != null) {
				return ExternalToolsPlugin.getDefault().getDescriptor().getUniqueIdentifier().equals(type.getCategory());
			}
			return false;
	}


}
