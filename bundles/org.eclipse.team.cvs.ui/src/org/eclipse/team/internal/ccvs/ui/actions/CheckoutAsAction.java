package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.PromptingDialog;
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
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					final Shell shell = getShell();
					ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
					if (folders.length != 1) return;
					String name = folders[0].getName();
					// Prompt for name
					final int[] result = new int[] { InputDialog.OK };
					final InputDialog dialog = new InputDialog(shell, Policy.bind("CheckoutAsAction.enterProjectTitle"), Policy.bind("CheckoutAsAction.enterProject"), name,  //$NON-NLS-1$ //$NON-NLS-2$
						new IInputValidator() {
							public String isValid(String newText) {
								IStatus status = ResourcesPlugin.getWorkspace().validateName(newText, IResource.PROJECT);
								if (status.isOK()) {
									return ""; //$NON-NLS-1$
								}
								return status.getMessage();
							}
	
						});
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							result[0] = dialog.open();
						}
					});
					if (result[0] != InputDialog.OK) {
						return;
					}

					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(dialog.getValue());
					
					PromptingDialog prompt = new PromptingDialog(getShell(), new IResource[] {project}, 
																  getOverwriteLocalAndFileSystemPrompt(), 
																  Policy.bind("ReplaceWithAction.confirmOverwrite"));
					IResource[] resources = prompt.promptForMultiple();
					if(resources.length != 0) {				
						monitor.beginTask(null, 100);
						monitor.setTaskName(Policy.bind("CheckoutAsAction.taskname", name, project.getName())); //$NON-NLS-1$
						CVSProviderPlugin.getProvider().checkout(folders, new IProject[] { project }, Policy.subMonitorFor(monitor, 100));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
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