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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.PromptingDialog;

/**
 * The purpose of the CheckoutAction is to allow the checkout of module aliases into the
 */
public class CheckoutAction extends CVSAction {
	
	/**
	 * @see CVSAction#execute(IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		// Fetch the module expansions for the folder
		final ICVSRemoteFolder[] remoteFolders = getSelectedRemoteFolders();
		final String[][] expansions = new String[1][0];
		expansions[0] = null;
		CVSUIPlugin.runWithProgressDialog(getShell(), true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					expansions[0] = CVSProviderPlugin.getProvider().getExpansions(remoteFolders, monitor);
				} catch (CVSException e) {
					throw new InvocationTargetException(e);
				}
			}
		});
		if (expansions[0] == null) return;
		
		// If the folder exists, inform the user that it will be overridden
		Set targetProjects = new HashSet();
		for (int i = 0; i < expansions[0].length; i++) {
			String string = expansions[0][i];
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(string).segment(0));
			targetProjects.add(project);
		}
		IResource[] projects = (IResource[]) targetProjects.toArray(new IResource[targetProjects.size()]);
		PromptingDialog prompt = new PromptingDialog(getShell(), projects, 
													  getOverwriteLocalAndFileSystemPrompt(), 
													  Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
		if (prompt.promptForMultiple().length != projects.length) return;
		
		CVSUIPlugin.runWithProgressDialog(getShell(), true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					monitor.beginTask(getTaskName(remoteFolders), 100);
					monitor.setTaskName(getTaskName(remoteFolders));						
					CVSProviderPlugin.getProvider().checkout(remoteFolders, null, Policy.subMonitorFor(monitor, 100));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		});
	}

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length == 1;
	}

	protected static String getTaskName(ICVSRemoteFolder[] remoteFolders) {
		if (remoteFolders.length == 1) {
			ICVSRemoteFolder folder = remoteFolders[0];
			return Policy.bind("AddToWorkspace.taskName1", folder.getRepositoryRelativePath());  //$NON-NLS-1$
		}
		else {
			return Policy.bind("AddToWorkspace.taskNameN", new Integer(remoteFolders.length).toString());  //$NON-NLS-1$
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
					return Policy.bind("AddToWorkspaceAction.thisResourceExists", resource.getName());//$NON-NLS-1$
				} else {
					return Policy.bind("AddToWorkspaceAction.thisExternalFileExists", resource.getName());//$NON-NLS-1$
				}
			}
			private File getFileLocation(IResource resource) {
				return new File(resource.getParent().getLocation().toFile(), resource.getName());
			}
		};
	}
}
