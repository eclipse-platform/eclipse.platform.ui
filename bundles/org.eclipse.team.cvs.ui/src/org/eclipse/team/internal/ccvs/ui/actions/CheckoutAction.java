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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.PromptingDialog;

/**
 * Checkout a remote module into the workspace ensuring that the user is prompted for
 * any overwrites that may occur.
 */
public class CheckoutAction extends CVSAction {
	
	/**
	 * @see CVSAction#execute(IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					ICVSRemoteFolder[] remoteFolders = getSelectedRemoteFolders();
					String taskName = getTaskName(remoteFolders);
					monitor.beginTask(taskName, 100);
					monitor.setTaskName(taskName);
					String[] expansions = getExpansions(remoteFolders, Policy.subMonitorFor(monitor, 10));
					if (!checkValidExpansions(expansions)) return;
					if (!promptForOverwrite(expansions)) return;
					checkoutModules(remoteFolders, Policy.subMonitorFor(monitor, 90));
				} catch (CVSException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}

	/*
	 * Get the module expansions for the selected remote modules.
	 */
	private String[] getExpansions(ICVSRemoteFolder[] remoteFolders, IProgressMonitor monitor) throws CVSException {
		return CVSWorkspaceRoot.getExpansions(remoteFolders, monitor);
	}
	
	/*
	 * Ensure that the expansions are valid and non-overlapping
	 */
	private boolean checkValidExpansions(String[] expansions) throws CVSException {
		if (expansions == null) return false;
		// Ensure that the expansions are unique
		Set unique = new HashSet();
		for (int i = 0; i < expansions.length; i++) {
			String expansion = expansions[i];
			if (unique.contains(expansion)) {
				throw new CVSException(new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("CheckoutAction.overlappingModuleExpansions", expansion), null)); //$NON-NLS-1$
			}
			unique.add(expansion);
		}
		return true;
	}
	
	/*
	 * Prompt the user to overwrite any projects that overlap with the module expansions.
	 * 
	 * This is an all or nothing prompt. If the user says no to one project overwrite
	 * then the whole operation must be aborted. This is because there is no easy way to
	 * map the module expansions back to their remote modules.
	 */
	private boolean promptForOverwrite(String[] expansions) throws InterruptedException {

		// If the target project exists, prompt the user for overwrite
		Set targetProjects = new HashSet();
		for (int i = 0; i < expansions.length; i++) {
			String string = expansions[i];
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(string).segment(0));
			targetProjects.add(project);
		}
		IResource[] projects = (IResource[]) targetProjects.toArray(new IResource[targetProjects.size()]);
		PromptingDialog prompt = new PromptingDialog(getShell(), projects, 
													  getOverwriteLocalAndFileSystemPrompt(), 
													  Policy.bind("ReplaceWithAction.confirmOverwrite"),
													  true /* all or nothing*/);//$NON-NLS-1$
		return (prompt.promptForMultiple().length == projects.length);
	}
	
	/*
	 * Peform the checkout of the remote modules
	 */
	protected void checkoutModules(ICVSRemoteFolder[] remoteFolders, IProgressMonitor monitor) throws InvocationTargetException {
		try {					
			CVSWorkspaceRoot.checkout(remoteFolders, null, monitor);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		if (folders.length == 0) return false;
		// only enabled when all folders are in the same repository
		ICVSRepositoryLocation location = folders[0].getRepository();
		for (int i = 1; i < folders.length; i++) {
			ICVSRemoteFolder folder = folders[i];
			if (!folder.getRepository().equals(location)) {
				return false;
			}
		}
		return true;
	}

	protected static String getTaskName(ICVSRemoteFolder[] remoteFolders) {
		if (remoteFolders.length == 1) {
			ICVSRemoteFolder folder = remoteFolders[0];
			String label = folder.getRepositoryRelativePath();
			if (label.equals(FolderSyncInfo.VIRTUAL_DIRECTORY)) {
				label = folder.getName();
			}
			return Policy.bind("AddToWorkspace.taskName1", label);  //$NON-NLS-1$
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
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("AddToWorkspaceAction.checkoutFailed"); //$NON-NLS-1$
	}
}
