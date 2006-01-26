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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;

/**
 * creates a filter for the current working sets in use on the workbench to be applied in the launch configuration
 * dialog and the launch history/last launched
 * 
 * @since 3.2
 */
public class WorkingSetsFilter extends ViewerFilter {

	/**
	 * The id for a breakpoint working set
	 */
	private static String bpid = "org.eclipse.debug.ui.breakpointWorkingSet"; //$NON-NLS-1$
	
	/**
	 * Constructor
	 */
	public WorkingSetsFilter() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if(element instanceof ILaunchConfigurationType) {
			return true;
		}
		if(element instanceof ILaunchConfiguration) {
			ILaunchConfiguration config = (ILaunchConfiguration)element;
			if(config.exists()) {
				try {
					IResource[] resources = config.getMappedResources();
					if(resources == null) {
						return true;
					}
					IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
					if(window == null) {
						return true;
					}
					IWorkbenchPage page = window.getActivePage();
					if(page == null) {
						return true;
					}
					IWorkingSet[] wsets = page.getWorkingSets();
					if(wsets.length < 1) {
						return true;
					}
					IResource res = null;
					for(int i = 0; i < wsets.length; i++) {
						if(!wsets[i].getId().equals(bpid)) {
							IAdaptable[] elements = wsets[i].getElements();
							for(int j = 0; j < elements.length; j++) {
								res = (IResource)elements[j].getAdapter(IResource.class);
								if(res != null) {
									for(int k = 0; k < resources.length; k++) {
										if(resources[k].equals(res)) {
											return true;
										}
									}
								}
							}
						}
					}
				} 
				catch (CoreException e) {DebugUIPlugin.log(e);}
			}
		}
		return false;
	}

}
