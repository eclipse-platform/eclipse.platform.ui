/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;

/**
 * Default implementation of a launch configuration delegate.
 * <p>
 * Clients implementing launch configration delegates should subclass
 * this class.
 * </p>
 * @since 3.0
 */
public abstract class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate2 {
	
	/**
	 * Status code for which a UI prompter is registered.
	 */
	protected static final IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	/**
	 * Status code for which a prompter is registered to ask the user if they
	 * want to launch in debug mode when breakpoints are present.
	 */
	protected static final IStatus switchToDebugPromptStatus = new Status(IStatus.INFO, "org.eclipse.debug.core", 201, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#buildForLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#finalLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}
	/* (non-Javadoc)
	 * 
	 * If launching in run mode, and the configuration supports debug mode, check
	 * if there are any breakpoints in the workspace, and ask the user if they'd
	 * rather launch in debug mode.
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		if (mode.equals(ILaunchManager.RUN_MODE)  && configuration.supportsMode(ILaunchManager.DEBUG_MODE)) {
			IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
			if (!breakpointManager.isEnabled()) {
				// no need to check breakpoints individually.
				return true; 
			}
			IBreakpoint[] breakpoints = breakpointManager.getBreakpoints();
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i].isEnabled()) {
					IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
					if (prompter != null) {
						boolean lauchInDebugModeInstead = ((Boolean)prompter.handleStatus(switchToDebugPromptStatus, configuration)).booleanValue();
						if (lauchInDebugModeInstead) { 
							return false; //kill this launch
						} 
					}
					// if no user prompt, or user says to continue (no need to check other breakpoints)
					return true;
				}
			}
		}	
		// no enabled breakpoints... continue launch
		return true;
	}

	/**
	 * Convenience method that returns an array of referenced projects in their suggested build order.
	 * Subclasses may override this method to provide a different implementation. 
	 * @param project The project containing the resource being launched
	 * @return referenced projects ordered by their suggested build order
	 * @throws CoreException if an error occurs while getting referenced projects from the current project
	 */
	protected IProject[] getBuildOrder(IProject[] projects) throws CoreException {
		HashSet unorderedProjects = new HashSet();
		for(int i = 0; i< projects.length; i++) {
			unorderedProjects.add(projects[i]);
			fillReferencedProjectSet(projects[i], unorderedProjects);
		}
		
		IProject[] projectSet = (IProject[]) unorderedProjects.toArray(new IProject[unorderedProjects.size()]);
		return orderProjectSet(projectSet);
	}


	/**
	 * Recursively creates a set of projects referenced by the current project
	 * @param project The current project
	 * @param referencedProjSet A set of referenced projects
	 * @throws CoreException if an error occurs while getting referenced projects from the current project
	 */
	protected void fillReferencedProjectSet(IProject project, HashSet referencedProjSet) throws CoreException{
		IProject[] projects = project.getReferencedProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject refProject= projects[i];
			if (refProject.exists() && !referencedProjSet.contains(refProject)) {
				referencedProjSet.add(refProject);
				fillReferencedProjectSet(refProject, referencedProjSet);
			}
		}		
	}
	
	/**  
	 * creates a list of project ordered by their build order from an unordered list of projects.
	 * @param resourceCollection The list of projects to sort.
	 * @return A new list of projects, ordered by build order.
	 */
	protected IProject[] orderProjectSet(IProject[] projectSet) { 
		String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
		if (orderedNames != null) {
			List orderedProjects = new ArrayList(projectSet.length);
			//Projects may not be in the build order but should be built if selected
			List unorderedProjects = Arrays.asList(projectSet);
		
			for (int i = 0; i < orderedNames.length; i++) {
				String projectName = orderedNames[i];
				for (Iterator iterator = unorderedProjects.iterator(); iterator.hasNext(); ) {
					IProject project = (IProject)iterator.next();
					if (project.getName().equals(projectName)) {
						orderedProjects.add(project);
						iterator.remove();
						break;
					}
				}
			}
			//Add anything not specified before we return
			orderedProjects.addAll(unorderedProjects);
			return (IProject[]) orderedProjects.toArray(new IProject[orderedProjects.size()]);
		}

		// Computing build order returned null, try the project prerequisite order
		IWorkspace.ProjectOrder po = ResourcesPlugin.getWorkspace().computeProjectOrder(projectSet);
		return po.projects;
	}	
	
	/**
	 * Searches the project for problem markers of the specified severity
	 * @param proj The project to search
	 * @param severity The severity of the error to search for
	 * @return true if markers of the specified severity or higher severity exist.
	 * @throws CoreException if an error occurs while searching for problem markers
	 */
	protected boolean existsProblems(IProject proj, int severity) throws CoreException {
		IMarker[] markers = proj.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		
		if (markers.length > 0) {
			for (int i = 0; i < markers.length; i++) {
				if (((Integer)markers[i].getAttribute(IMarker.SEVERITY)).intValue() >= severity) {
					return true;
				}
			}
		}
		return false;
	}
}
