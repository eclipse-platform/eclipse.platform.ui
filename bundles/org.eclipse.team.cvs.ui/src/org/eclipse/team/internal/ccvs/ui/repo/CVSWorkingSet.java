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
package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * This class adds ICVSRemoteResource filtering to a working set
 */
public class CVSWorkingSet {

	IWorkingSet workingSet;
	IProject[] includedProjects;
	IPropertyChangeListener listener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			IWorkingSet changedSet = getChangedSet(event);
			if (changedSet == null || !changedSet.equals(workingSet)) return;
			String property = event.getProperty();
			if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
				includedProjects = null;
			} else if (property.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
				workingSet = null;
				includedProjects = null;
			}
		}
		private IWorkingSet getChangedSet(PropertyChangeEvent event) {
			Object old = event.getOldValue();
			if (old instanceof IWorkingSet) {
				return (IWorkingSet)old;
				
			}
			Object newSet = event.getNewValue();
			if (newSet instanceof IWorkingSet) {
				return (IWorkingSet)newSet;
				
			}
			return null;
		}
	};
	
	/**
	 * 
	 */
	public CVSWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
		PlatformUI.getWorkbench().getWorkingSetManager().addPropertyChangeListener(listener);
	}

	public void dispose() {
		PlatformUI.getWorkbench().getWorkingSetManager().removePropertyChangeListener(listener);
	}
	
	private void initializeProjects() {
		if (workingSet == null) {
			includedProjects = null;
			return;
		}
		// get the projects associated with the working set
		IAdaptable[] adaptables = workingSet.getElements();
		Set projects = new HashSet();
		for (int i = 0; i < adaptables.length; i++) {
			IAdaptable adaptable = adaptables[i];
			Object adapted = adaptable.getAdapter(IResource.class);
			if (adapted != null) {
				// Can this code be generalized?
				IProject project = ((IResource)adapted).getProject();
				projects.add(project);
			}
		}
		this.includedProjects = (IProject[]) projects.toArray(new IProject[projects.size()]);
	}
	
	public boolean select(ICVSRemoteResource remoteResource) {
		if (workingSet == null) return true;
		if (includedProjects == null) initializeProjects();
		for (int i = 0; i < includedProjects.length; i++) {
			IProject project = includedProjects[i];
			if (project.getName().equals(remoteResource.getName())) {
				return true;
			}
		}
		return false;
	}
}
