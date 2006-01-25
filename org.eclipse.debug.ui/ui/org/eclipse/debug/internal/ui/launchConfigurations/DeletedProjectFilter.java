/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 * Provides implementaiton to filter unavailable projects form the launch configuration dialog.
 * It is (de) activated via the <code>IInternalDebugUIConstants.PREF_FILTER_LAUNCH_DELETED</code> preference.
 * 
 * @since 3.2
 *
 */
public class DeletedProjectFilter extends ViewerFilter {

	/**
	 * Constructor
	 */
	public DeletedProjectFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		//always let through types, we only care about configs
		if (element instanceof ILaunchConfigurationType) {
			return true;
		}
		if(element instanceof ILaunchConfiguration) {
			try {
				ILaunchConfiguration config = (ILaunchConfiguration)element;
				if(config.exists()) {
					IResource[] resources = config.getMappedResources();
					if(resources == null) {
						return true;
					}
					for(int i = 0; i < resources.length; i++) {
						if(resources[i] instanceof IProject) {
							IProject project = (IProject)resources[i];
							if(project.exists()) {
								return true;
							}
						}
					}
				}
			}
			catch(CoreException e) {e.printStackTrace();}
		}
		return false;
	}

}
