package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.PromptingDialog;
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
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					final Shell shell = getShell();
					ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
					if (folders.length != 1) return;
					String name = folders[0].getName();
					// Prompt for name
					final int[] result = new int[] { Dialog.OK };
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
					final ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(shell, project);
					dialog.setTitle(Policy.bind("CheckoutAsAction.enterProjectTitle", name));	

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
						getOverwriteLocalAndFileSystemPrompt(), Policy.bind("ReplaceWithAction.confirmOverwrite"));
					if (prompt.promptForMultiple().length == 0) return;

					monitor.beginTask(null, 100);
					monitor.setTaskName(Policy.bind("CheckoutAsAction.taskname", name, newName)); //$NON-NLS-1$

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
						throw CVSException.wrapException(e);
					}

					CVSProviderPlugin.getProvider().checkout(folders, new IProject[] { project }, Policy.subMonitorFor(monitor, 95));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("CheckoutAsAction.checkoutFailed"), this.PROGRESS_DIALOG); //$NON-NLS-1$
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length == 1;
	}
}