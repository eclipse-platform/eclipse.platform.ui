/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;

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
		if (folders.length != 1) return;
		final String remoteFolderName = folders[0].getName();
		// make a copy of the folder so that we will not effect the original folder when we refetch the members
		final ICVSRemoteFolder folder = (ICVSRemoteFolder)folders[0].forTag(folders[0].getTag());
		
		// Fetch the members of the folder to see if they contain a .project file.
		final boolean[] hasProjectMetaFile = new boolean[] { false };
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					folder.members(monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
				// Check for the existance of the .project file
				try {
					folder.getFile(".project"); //$NON-NLS-1$
					hasProjectMetaFile[0] = true;
				} catch (TeamException e) {
					// We couldn't retrieve the meta file so assume it doesn't exist
					hasProjectMetaFile[0] = false;
				}
				// If the above failed, look for the old .vcm_meta file
				if (! hasProjectMetaFile[0]) {
					try {
						folder.getFile(".vcm_meta"); //$NON-NLS-1$
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
			ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(getShell(), newProject);
			dialog.setTitle(Policy.bind("CheckoutAsAction.enterProjectTitle", remoteFolderName)); //$NON-NLS-1$
			int result = dialog.open();
			if (result != Dialog.OK) return;
			Object[] destinationPaths = dialog.getResult();
			if (destinationPaths == null) return;
			String newName = (String) destinationPaths[0];
			IPath newLocation = new Path((String) destinationPaths[1]);
			
			// create the project description for a custom location
			boolean useDefaultLocation = newLocation.equals(Platform.getLocation());
			if (!useDefaultLocation) {
				newDesc = ResourcesPlugin.getWorkspace().newProjectDescription(newProject.getName());
				newDesc.setLocation(newLocation);
			}
			
			// prompt if the project or location exists locally
			newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(newName);
			PromptingDialog prompt = new PromptingDialog(getShell(), new IResource[] { newProject },
				getOverwriteLocalAndFileSystemPrompt(newDesc), Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
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
					if (hasProjectMetaFile[0]) {
	
						monitor.beginTask(null, 100);
						monitor.setTaskName(Policy.bind("CheckoutAsAction.taskname", remoteFolderName, project.getName())); //$NON-NLS-1$
	
						// create the project
						try {
							if (desc == null) {
								// create in default location
								project.create(Policy.subMonitorFor(monitor, 3));
							} else {
								// create in some other location
								project.create(desc, Policy.subMonitorFor(monitor, 3));
							}
							project.open(Policy.subMonitorFor(monitor, 2));
						} catch (CoreException e) {
							throw CVSException.wrapException(e);
						}
					} else {
						monitor.beginTask(null, 95);
						monitor.setTaskName(Policy.bind("CheckoutAsAction.taskname", remoteFolderName, project.getName())); //$NON-NLS-1$
					}

					CVSWorkspaceRoot.checkout(folders, new IProject[] { project }, Policy.subMonitorFor(monitor, 95));

				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length == 1;
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
	
	protected IPromptCondition getOverwriteLocalAndFileSystemPrompt(final IProjectDescription desc) {
		return new IPromptCondition() {
			// prompt if resource in workspace exists or exists in local file system
			public boolean needsPrompt(IResource resource) {
				
				// First, check the description location
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
}