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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.core.target.UrlUtil;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * Action to transfer a remote folder and it's contents into the workspace. The
 * resulting project is named with the remote folder name and the kind of project
 * is determined by the remote .project file. If the remote folder doesn't not
 * contain a .project, then the project default simple type is used.
 * 
 * @see GetAsAction
 */
public class GetAsProjectAction extends TargetAction {
	
	public void run(IAction action) {
		
		final IRemoteTargetResource[] remoteFolders = getSelectedRemoteFolders();
		
		Set targetProjects = new HashSet();
		for (int i = 0; i < remoteFolders.length; i++) {
			String projectName = remoteFolders[i].getName();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			targetProjects.add(project);
		}
		final IResource[] projects = (IResource[]) targetProjects.toArray(new IResource[targetProjects.size()]);
		PromptingDialog prompt = new PromptingDialog(getShell(), projects, 
													  getOverwriteLocalAndFileSystemPrompt(), 
													  Policy.bind("GetAsProject.confirmOverwrite"));//$NON-NLS-1$
		
		try {
			if (prompt.promptForMultiple().length != projects.length) return;
		} catch (InterruptedException e) {
			return;
		}
		
		try {
			TeamUIPlugin.runWithProgressDialog(getShell(), true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(getTaskName(remoteFolders), IProgressMonitor.UNKNOWN);
						monitor.setTaskName(getTaskName(remoteFolders));						
						for (int i = 0; i < remoteFolders.length; i++) {
							IProject project = (IProject)projects[i];
							IRemoteTargetResource remote = remoteFolders[i];
							IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 100);
							get(project, remote, subMonitor);
						}
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			handle(e, Policy.bind("Error"), Policy.bind("GetAsProject.errorGettingResources")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length > 0;
	}
	
	protected void get(IProject project, IRemoteTargetResource remote, IProgressMonitor monitor) throws TeamException {
		try {
			monitor.beginTask(null, 100);
			try {
				if(!project.exists()) {
					project.create(Policy.subMonitorFor(monitor, 5));
				}
				if(!project.isOpen()) {
					project.open(Policy.subMonitorFor(monitor, 5));
				}
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), Policy.bind("Error"), Policy.bind("GetAsProject.errorCreatingProject"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			Site site = remote.getSite();
			TargetProvider provider = TargetManager.getProvider(project);
			if(provider != null) {
				TargetManager.unmap(project);
			}
			TargetManager.map(project, site, UrlUtil.getTrailingPath(remote.getURL(), site.getURL()));
			provider = TargetManager.getProvider(project);
			provider.get(project, remote, Policy.subMonitorFor(monitor, 90));
		} finally {
			monitor.done();
		}
	}

	protected static String getTaskName(IRemoteTargetResource[] remoteFolders) {
		if (remoteFolders.length == 1) {
			IRemoteTargetResource folder = remoteFolders[0];
			return Policy.bind("GetAsProject.taskName1", folder.getURL().toExternalForm());  //$NON-NLS-1$
		}
		else {
			return Policy.bind("GetAsProject.taskNameN", new Integer(remoteFolders.length).toString());  //$NON-NLS-1$
		}
	}
	
	protected IPromptCondition getOverwriteLocalAndFileSystemPrompt() {
		return new IPromptCondition() {
			// prompt if resource in workspace exists or exists in local file system
			public boolean needsPrompt(IResource resource) {
				File localLocation  = getFileLocation(resource);
				if(resource.exists() || localLocation.exists()) {
					return true;
				}
				return false;
			}
			public String promptMessage(IResource resource) {
				File localLocation  = getFileLocation(resource);
				if(resource.exists()) {
					return Policy.bind("GetAsProject.thisResourceExists", resource.getName());//$NON-NLS-1$
				} else {
					return Policy.bind("GetAsProject.thisExternalFileExists", resource.getName());//$NON-NLS-1$
				}//$NON-NLS-1$
			}
			private File getFileLocation(IResource resource) {
				return new File(resource.getParent().getLocation().toFile(), resource.getName());
			}
		};
	}
}
