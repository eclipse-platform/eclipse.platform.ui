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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagetLocationSelectionDialog;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Add a remote resource to the workspace. Current implementation:
 * -Works only for remote folders
 * -Does not prompt for project name; uses folder name instead
 */
public class CheckoutAsAction extends AddToWorkspaceAction {
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		if (folders.length == 1){
			// make a copy of the folder so that we will not effect the original folder when we refetch the members
			// todo: this is a strang thing to need to do. We shold fix this.
			final ICVSRemoteFolder folder = (ICVSRemoteFolder)folders[0].forTag(folders[0].getTag());
			checkoutSingleProject(folder);
		} else {
			checkoutMultipleProjects(folders);
		}
	}
	
	private void checkoutMultipleProjects(final ICVSRemoteFolder[] folders) throws InvocationTargetException, InterruptedException {
		
		// create the target project handles
		IProject[] targetProjects = new IProject[folders.length];
		for (int i = 0; i < folders.length; i++) {
			ICVSRemoteFolder remoteFolder = folders[i];
			targetProjects[i] = ResourcesPlugin.getWorkspace().getRoot().getProject(remoteFolder.getName());
		}
		
		// prompt for the parent location
		TagetLocationSelectionDialog dialog = new TagetLocationSelectionDialog(
			getShell(), 
			Policy.bind("CheckoutAsAction.enterLocationTitle", new Integer(targetProjects.length).toString()), //$NON-NLS-1$
			targetProjects);
		int result = dialog.open();
		if (result != Dialog.OK) return;
		String targetParentLocation = dialog.getTargetLocation();
			
		// if the location is null, just checkout the projects into the workspace
		if (targetParentLocation == null) {
			checkoutSelectionIntoWorkspaceDirectory();
			return;
		}
		
		// create the project descriptions for each project
		IProjectDescription[] descriptions = new IProjectDescription[targetProjects.length];
		for (int i = 0; i < targetProjects.length; i++) {
			String projectName = targetProjects[i].getName();
			descriptions[i] = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
			descriptions[i].setLocation(new Path(targetParentLocation).append(projectName));
		}
			
		// prompt if the projects or locations exist locally
		PromptingDialog prompt = new PromptingDialog(getShell(), targetProjects,
			getOverwriteLocalAndFileSystemPrompt(descriptions), Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
		IResource[] projectsToCheckout = prompt.promptForMultiple();
		if (projectsToCheckout.length== 0) return;
		
		// copy the selected projects to a new array
		final IProject[] projects = new IProject[projectsToCheckout.length];
		for (int i = 0; i < projects.length; i++) {
			projects[i] = projectsToCheckout[i].getProject();
		}
		
		// perform the checkout
		final IProjectDescription[] newDescriptions = descriptions;
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor.beginTask(null, 100);
					monitor.setTaskName(Policy.bind("CheckoutAsAction.multiCheckout", new Integer(projects.length).toString())); //$NON-NLS-1$
					// create the projects
					createAndOpenProjects(projects, newDescriptions, Policy.subMonitorFor(monitor, 5));
					checkoutProjects(folders, projects, Policy.subMonitorFor(monitor, 95));


				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}

	private void checkoutSingleProject(final ICVSRemoteFolder remoteFolder) throws InvocationTargetException, InterruptedException {
		// Fetch the members of the folder to see if they contain a .project file.
		final String remoteFolderName = remoteFolder.getName();
		final boolean[] hasProjectMetaFile = new boolean[] { false };
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					remoteFolder.members(monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
				// Check for the existance of the .project file
				try {
					remoteFolder.getFile(".project"); //$NON-NLS-1$
					hasProjectMetaFile[0] = true;
				} catch (TeamException e) {
					// We couldn't retrieve the meta file so assume it doesn't exist
					hasProjectMetaFile[0] = false;
				}
				// If the above failed, look for the old .vcm_meta file
				if (! hasProjectMetaFile[0]) {
					try {
						remoteFolder.getFile(".vcm_meta"); //$NON-NLS-1$
						hasProjectMetaFile[0] = true;
					} catch (TeamException e) {
						// We couldn't retrieve the meta file so assume it doesn't exist
						hasProjectMetaFile[0] = false;
					}
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
		
		// Prompt outside a workspace runnable so that the project creation delta can be heard
		IProject newProject = null;
		IProjectDescription newDesc = null;
		if (hasProjectMetaFile[0]) {
			
			// prompt for the project name and location
			newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(remoteFolderName);
			TagetLocationSelectionDialog dialog = new TagetLocationSelectionDialog(getShell(), Policy.bind("CheckoutAsAction.enterProjectTitle", remoteFolderName), newProject); //$NON-NLS-1$
			int result = dialog.open();
			if (result != Dialog.OK) return;
			// get the name and location from the dialog
			String targetLocation = dialog.getTargetLocation();
			String targetName = dialog.getNewProjectName();
			
			// create the project description for a custom location
			if (targetLocation != null) {
				newDesc = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
				newDesc.setLocation(new Path(targetLocation));
			}
			
			// prompt if the project or location exists locally
			newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(targetName);
			PromptingDialog prompt = new PromptingDialog(getShell(), new IResource[] { newProject },
				getOverwriteLocalAndFileSystemPrompt(
					newDesc == null ? new IProjectDescription[0] : new IProjectDescription[] {newDesc}), 
					Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
			if (prompt.promptForMultiple().length == 0) return;
			
		} else {
			newProject = getNewProject(remoteFolderName);
			if (newProject == null) return;
		}
		
		final IProject project = newProject;
		final IProjectDescription desc = newDesc;
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor.beginTask(null, 100);
					monitor.setTaskName(Policy.bind("CheckoutAsAction.taskname", remoteFolderName, project.getName())); //$NON-NLS-1$
					int used = 0;
					if (hasProjectMetaFile[0]) {
						used = 5;
						createAndOpenProject(project, desc, Policy.subMonitorFor(monitor, used));
					}
					checkoutProjects(new ICVSRemoteFolder[] { remoteFolder }, new IProject[] { project }, Policy.subMonitorFor(monitor, 100 - used));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}
	
	private void createAndOpenProjects(IProject[] projects, IProjectDescription[] descriptions, IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(null, projects.length* 100);
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			IProjectDescription desc = findDescription(descriptions, project);
			createAndOpenProject(project, desc, Policy.subMonitorFor(monitor, 100));
		}
		monitor.done();
	}

	private void createAndOpenProject(IProject project, IProjectDescription desc, IProgressMonitor monitor) throws CVSException {
		try {
			monitor.beginTask(null, 5);
			if (project.exists()) {
				if (desc != null) {
					project.move(desc, true, Policy.subMonitorFor(monitor, 3));
				}
			} else {
				if (desc == null) {
					// create in default location
					project.create(Policy.subMonitorFor(monitor, 3));
				} else {
					// create in some other location
					project.create(desc, Policy.subMonitorFor(monitor, 3));
				}
			}
			if (!project.isOpen()) {
				project.open(Policy.subMonitorFor(monitor, 2));
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}
	
	private void checkoutProjects(ICVSRemoteFolder[] folders, IProject[] projects, IProgressMonitor monitor) throws TeamException {
		CVSWorkspaceRoot.checkout(folders, projects, monitor);
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length > 0;
	}

	/**
	 * Get a new project.
	 * 
	 * The suggestedName is not currently used but is a desired capability.
	 */
	private IProject getNewProject(String suggestedName) {
		NewProjectListener listener = new NewProjectListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		(new NewProjectAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow())).run();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		// Ensure that the project only has a single member which is the .project file
		IProject project = listener.getNewProject();
		if (project == null) return null;
		try {
			IResource[] members = project.members();
			if ((members.length == 0) 
				||(members.length == 1 && members[0].getName().equals(".project"))) { //$NON-NLS-1$
				return project;
			} else {
				// prompt to overwrite
				PromptingDialog prompt = new PromptingDialog(getShell(), new IProject[] { project }, 
						getOverwriteLocalAndFileSystemPrompt(), 
						Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
				try {
					if (prompt.promptForMultiple().length == 1) return project;
				} catch (InterruptedException e) {
				}
			}
		} catch (CoreException e) {
			handle(e);
		}
		return null;
	}
	
	class NewProjectListener implements IResourceChangeListener {
		private IProject newProject = null;
		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren();
			for (int i = 0; i < projectDeltas.length; i++) {							
				IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				if (delta.getKind() == IResourceDelta.ADDED) {
					newProject = (IProject)resource;
				}
			}
		}
		/**
		 * Gets the newProject.
		 * @return Returns a IProject
		 */
		public IProject getNewProject() {
			return newProject;
		}
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CheckoutAsAction.checkoutFailed"); //$NON-NLS-1$
	}
	
	protected IPromptCondition getOverwriteLocalAndFileSystemPrompt(final IProjectDescription[] descriptions) {
		return new IPromptCondition() {
			// prompt if resource in workspace exists or exists in local file system
			public boolean needsPrompt(IResource resource) {
				
				// First, check the description location
				IProjectDescription desc = findDescription(descriptions, resource);
				if (desc != null) {
					File localLocation = desc.getLocation().toFile();
					return localLocation.exists();
				}
				
				// Next, check if the resource itself exists
				if (resource.exists()) return true;
				
				// Finally, check if the location in the workspace exists;
				File localLocation  = getFileLocation(resource);
				if (localLocation.exists()) return true;
				
				// The target doesn't exist
				return false;
			}
			public String promptMessage(IResource resource) {
				IProjectDescription desc = findDescription(descriptions, resource);
				if (desc != null) {
					return Policy.bind("AddToWorkspaceAction.thisExternalFileExists", desc.getLocation().toString());//$NON-NLS-1$
				} else if(resource.exists()) {
					return Policy.bind("AddToWorkspaceAction.thisResourceExists", resource.getName());//$NON-NLS-1$
				} else {
					File localLocation  = getFileLocation(resource);
					return Policy.bind("AddToWorkspaceAction.thisExternalFileExists", localLocation.toString());//$NON-NLS-1$
				}
			}
			private File getFileLocation(IResource resource) {
				return new File(resource.getParent().getLocation().toFile(), resource.getName());
			}
		};
	}
	
	private IProjectDescription findDescription(IProjectDescription[] descriptions, IResource resource) {
		IProject project = resource.getProject();
		for (int i = 0; i < descriptions.length; i++) {
			IProjectDescription description = descriptions[i];
			if (description.getName().equals(project.getName()))
				return description;
		}
		return null;
	}
}
