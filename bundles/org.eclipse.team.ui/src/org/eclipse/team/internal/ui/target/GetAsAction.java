/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;

/**
 * Action to transfer a remote folder and it's contents into the workspace. If
 * the remote folder doesn't have a .project then the project creation wizard
 * is shown to configure a new project, otherwise the a prompt is shown to choose
 * the project name and location.
 * 
 * @see GetAsProjectAction
 */
public class GetAsAction extends GetAsProjectAction {
	public void run(IAction action) {
		
		final IRemoteTargetResource[] folders = getSelectedRemoteFolders();
		if (folders.length != 1) return;
		final IRemoteTargetResource remoteFolder = folders[0];
		final String remoteName = remoteFolder.getName();
		
		// Fetch the members of the folder to see if they contain a .project file.
		final boolean[] hasProjectMetaFile = new boolean[] { false };
		final boolean[] errorOccured = new boolean[] { false };
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					hasProjectMetaFile[0] = hasProjectMetaFile(remoteFolder, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("GetAs.checkoutFailed"), this.PROGRESS_DIALOG); //$NON-NLS-1$
		if (errorOccured[0]) return;
		
		// Prompt outside a workspace runnable so that the project creation delta can be heard
		IProject newProject = null;
		if ( ! hasProjectMetaFile[0]) {
			newProject = getNewProject(remoteFolder.getName());
			if (newProject == null) return;
		}
		
		final IProject createdProject = newProject;
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					IProject project;
					if (hasProjectMetaFile[0]) {
						// Prompt for name
						final Shell shell = getShell();
						final int[] result = new int[] { Dialog.OK };
						project = ResourcesPlugin.getWorkspace().getRoot().getProject(remoteName);
						final ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(shell, project);
						dialog.setTitle(Policy.bind("GetAs.enterProjectTitle", remoteName)); //$NON-NLS-1$
	
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								result[0] = dialog.open();
							}
						});
						if (result[0] != Dialog.OK) return;
	
						Object[] destinationPaths = dialog.getResult();
						if (destinationPaths == null) return;
						String newName = (String) destinationPaths[0];
						IPath newLocation = new Path((String) destinationPaths[1]);
	
						// prompt if the project exists locally
						project = ResourcesPlugin.getWorkspace().getRoot().getProject(newName);
						PromptingDialog prompt = new PromptingDialog(getShell(), new IResource[] { project },
							getOverwriteLocalAndFileSystemPrompt(), Policy.bind("GetAsProject.confirmOverwrite"));//$NON-NLS-1$
						if (prompt.promptForMultiple().length == 0) return;
	
						monitor.beginTask(null, 100);
						monitor.setTaskName(Policy.bind("GetAs.taskname", remoteFolder.getName(), newName)); //$NON-NLS-1$
	
						// create the project
						try {
							if (newLocation.equals(Platform.getLocation())) {
								// create in default location
								project.create(Policy.subMonitorFor(monitor, 3));
							} else {
								// create in some other location
								IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(project.getName());
								desc.setLocation(newLocation);
								project.create(desc, Policy.subMonitorFor(monitor, 3));
							}
							project.open(Policy.subMonitorFor(monitor, 2));
						} catch (CoreException e) {
							throw new TeamException(e.getStatus());
						}
					} else {
						project = createdProject;
						monitor.beginTask(null, 95);
						monitor.setTaskName(Policy.bind("GetAs.taskname", remoteFolder.getName(), createdProject.getName())); //$NON-NLS-1$
					}

					get(project, remoteFolder, Policy.subInfiniteMonitorFor(monitor, 95));

				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("GetAs.checkoutFailed"), this.PROGRESS_DIALOG); //$NON-NLS-1$
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
		return listener.getNewProject();
	}
	
	protected boolean hasProjectMetaFile(IRemoteTargetResource remote, IProgressMonitor monitor) throws TeamException {
		IRemoteResource[] children = remote.members(monitor);
		for (int i = 0; i < children.length; i++) {
			if(children[i].getName().equals(".project")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
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
}
