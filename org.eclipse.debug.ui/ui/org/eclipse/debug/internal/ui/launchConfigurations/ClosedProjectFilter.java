/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * 
 * This implementation is used to filter closed projects from the launch configuration dialog.
 * It is (de)activated via the <code>IInternalDebugUIConstants.PREF_FILTER_LAUNCH_CLOSED</code> preference, and is 
 * provided to fix bug 19521.
 * 
 * @since 3.2
 *
 */
public class ClosedProjectFilter extends ViewerFilter {

	/**
	 * Constructor
	 */
	public ClosedProjectFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		//always let through types, we only care about configs
		if(element instanceof ILaunchConfigurationType) {
			return true;
		}
		if(element instanceof ILaunchConfiguration) {
			try {
				ILaunchConfiguration config = (ILaunchConfiguration)element;
				IResource[] resources = config.getMappedResources();
				//if it has no mapping, it might not have migration delegate, so let it pass
				if(resources == null) {
					return true;
				}
				for(int i = 0; i < resources.length; i++) {
					IProject project= resources[i].getProject();
					//we don't want overlap with the deleted projects filter, so we need to allow projects that don't exist through
					if(project != null && (project.isOpen() || !project.exists())) {
						return true;
					}
				}
			} 
			catch (CoreException e) {}
		}
		return false;
	}
}
