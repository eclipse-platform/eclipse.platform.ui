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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.dialogs.IPromptCondition;
import org.eclipse.team.internal.ui.dialogs.PromptingDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Add some remote resources to the workspace. Current implementation:
 * -Works only for remote folders
 * -Does not prompt for project name; uses folder name instead
 */
public class AddToWorkspaceAction extends CVSAction {
	/**
	 * Returns the selected remote folders. 
	 * Remove any module aliases as they may cause problems on checkout this way
	 */
	protected ICVSRemoteFolder[] getSelectedRemoteFolders() {
		ICVSRemoteFolder[] allFolders = super.getSelectedRemoteFolders();
		if (allFolders.length == 0) return allFolders;
		ArrayList resources = new ArrayList();
		for (int i = 0; i < allFolders.length; i++) {
			ICVSRemoteFolder folder = allFolders[i];
			if (!Checkout.ALIAS.isElementOf(folder.getLocalOptions())) {
				resources.add(folder);
			}
		}
		return (ICVSRemoteFolder[])resources.toArray(new ICVSRemoteFolder[resources.size()]);
	}

	/*
	 * @see CVSAction#execute()
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		checkoutSelectionIntoWorkspaceDirectory();
	}
	
	protected void checkoutSelectionIntoWorkspaceDirectory() throws InvocationTargetException, InterruptedException {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
							
					List targetProjects = new ArrayList();
					Map targetFolders = new HashMap();
					for (int i = 0; i < folders.length; i++) {
						String name = folders[i].getName();
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						targetFolders.put(name, folders[i]);
						targetProjects.add(project);
					}
					
					IResource[] projects = (IResource[]) targetProjects.toArray(new IResource[targetProjects.size()]);
					
					PromptingDialog prompt = new PromptingDialog(getShell(), projects, 
																  getOverwriteLocalAndFileSystemPrompt(), 
																  Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
					projects = prompt.promptForMultiple();
															
					monitor.beginTask(null, 100);
					if (projects.length != 0) {
						IProject[] localFolders = new IProject[projects.length];
						ICVSRemoteFolder[] remoteFolders = new ICVSRemoteFolder[projects.length];
						for (int i = 0; i < projects.length; i++) {
							localFolders[i] = (IProject)projects[i];
							remoteFolders[i] = (ICVSRemoteFolder)targetFolders.get(projects[i].getName());
						}
						
						monitor.setTaskName(getTaskName(remoteFolders));						
						CVSWorkspaceRoot.checkout(remoteFolders, localFolders, Policy.subMonitorFor(monitor, 100));
					}
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
		ICVSRemoteFolder[] resources = getSelectedRemoteFolders();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof ICVSRepositoryLocation) return false;
		}
		return true;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("AddToWorkspaceAction.checkoutFailed"); //$NON-NLS-1$
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
}
