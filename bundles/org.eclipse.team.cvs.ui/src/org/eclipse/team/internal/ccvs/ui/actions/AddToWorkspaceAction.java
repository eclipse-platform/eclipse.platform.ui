package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.PromptingDialog;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Add some remote resources to the workspace. Current implementation:
 * -Works only for remote folders
 * -Does not prompt for project name; uses folder name instead
 */
public class AddToWorkspaceAction extends TeamAction {
	/**
	 * Returns the selected remote folders
	 */
	protected ICVSRemoteFolder[] getSelectedRemoteFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRemoteFolder) {
					if (!Checkout.ALIAS.isElementOf(((ICVSRemoteFolder)next).getLocalOptions())) {
						resources.add(next);
					}
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSRemoteFolder.class);
					if (adapter instanceof ICVSRemoteFolder) {
						if (!Checkout.ALIAS.isElementOf(((ICVSRemoteFolder)adapter).getLocalOptions())) {
							resources.add(adapter);
						}
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			return (ICVSRemoteFolder[])resources.toArray(new ICVSRemoteFolder[resources.size()]);
		}
		return new ICVSRemoteFolder[0];
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
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
																  Policy.bind("ReplaceWithAction.confirmOverwrite"));
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
						CVSProviderPlugin.getProvider().checkout(remoteFolders, localFolders, Policy.subMonitorFor(monitor, 100));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("AddToWorkspaceAction.checkoutFailed"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}
	
	private static String getTaskName(ICVSRemoteFolder[] remoteFolders) {
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
					return Policy.bind("AddToWorkspaceAction.thisResourceExists", resource.getName());
				} else {
					return Policy.bind("AddToWorkspaceAction.thisExternalFileExists", resource.getName());
				}
			}
			private File getFileLocation(IResource resource) {
				return new File(resource.getParent().getLocation().toFile(), resource.getName());
			}
		};
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
}