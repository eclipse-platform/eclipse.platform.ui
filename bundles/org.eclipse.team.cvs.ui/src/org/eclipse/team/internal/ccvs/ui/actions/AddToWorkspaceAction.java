package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.ui.Policy;
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
		
					monitor.beginTask(null, 100);
					monitor.setTaskName(getTaskName(folders));

					
					boolean yesToAll = false;
					boolean yesToAllExternal = false;
					int action;
					List targetProjects = new ArrayList();
					List targetFolders = new ArrayList();
					for (int i = 0; i < folders.length; i++) {
						String name = folders[i].getName();
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						if (project.exists()) {
							action = confirmOverwrite(project, yesToAll);
							yesToAll = action == 2;
						} else {
							File location = new File(project.getParent().getLocation().toFile(), project.getName());
							action = confirmOverwriteExternalFile(location, yesToAllExternal);
							yesToAllExternal = action == 2;
						}
						switch (action) {
							// no
							case 1:
								break;
							// yes to all
							case 2:
							// yes
							case 0:
								targetFolders.add(folders[i]);
								targetProjects.add(project);
								break;
							// cancel
							case 3:
							default:
								return;
						}
					}
					if (targetFolders.size() > 0) {
						CVSProviderPlugin.getProvider().checkout((ICVSRemoteFolder[])targetFolders.toArray(new ICVSRemoteFolder[targetFolders.size()]),
																 (IProject[])targetProjects.toArray(new IProject[targetProjects.size()]), 
																 Policy.subMonitorFor(monitor, 100));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("AddToWorkspaceAction.checkoutFailed"), this.PROGRESS_DIALOG);
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
	
	private int confirmOverwrite(IProject project, boolean yesToAll) {
		if (yesToAll) return 2;
		if ( ! project.exists()) return 0;
		final MessageDialog dialog = 
			new MessageDialog(shell, Policy.bind("AddToWorkspaceAction.confirmOverwrite"), null, Policy.bind("AddToWorkspaceAction.thisResourceExists", project.getName()), MessageDialog.QUESTION, 
				new String[] {
					IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL,
					IDialogConstants.YES_TO_ALL_LABEL, 
					IDialogConstants.CANCEL_LABEL}, 
				0);
		final int[] result = new int[1];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = dialog.open();
			}
		});
		return result[0];
	}
	
	private int confirmOverwriteExternalFile(File location, boolean yesToAll) {
		if (yesToAll) return 2;
		if ( ! location.exists()) return 0;
		final MessageDialog dialog = 
			new MessageDialog(shell, Policy.bind("AddToWorkspaceAction.confirmOverwrite"), null, Policy.bind("AddToWorkspaceAction.thisExternalFileExists", location.getName()), MessageDialog.QUESTION, 
				new String[] {
					IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL,
					IDialogConstants.YES_TO_ALL_LABEL, 
					IDialogConstants.CANCEL_LABEL}, 
				0);
		final int[] result = new int[1];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = dialog.open();
			}
		});
		return result[0];
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