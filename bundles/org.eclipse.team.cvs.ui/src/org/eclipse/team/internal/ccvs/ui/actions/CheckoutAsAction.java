package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Add a remote resource to the workspace. Current implementation:
 * -Works only for remote folders
 * -Does not prompt for project name; uses folder name instead
 */
public class CheckoutAsAction extends TeamAction {
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
					final Shell shell = getShell();
					ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
					if (folders.length != 1) return;
					String name = folders[0].getName();
					// Prompt for name
					final int[] result = new int[] { InputDialog.OK };
					final InputDialog dialog = new InputDialog(shell, Policy.bind("CheckoutAsAction.enterProjectTitle"), Policy.bind("CheckoutAsAction.enterProject"), name, null);
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							result[0] = dialog.open();
						}
					});
					if (result[0] != InputDialog.OK) {
						return;
					}

					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(dialog.getValue());
					if (project.exists()) {
						// Make sure the user understands they will overwrite the project.
						final boolean[] confirm = new boolean[] { false };
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								confirm[0] = MessageDialog.openConfirm(shell, Policy.bind("confirmOverwriteTitle"), Policy.bind("confirmOverwrite"));
							}
						});
						if (!confirm[0]) return;
					}
					monitor.beginTask(null, 100);
					monitor.setTaskName(Policy.bind("CheckoutAsAction.taskname", name, project.getName()));
					CVSProviderPlugin.getProvider().checkout(folders, new IProject[] { project }, Policy.subMonitorFor(monitor, 100));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CheckoutAsAction.checkoutFailed"), this.PROGRESS_DIALOG);
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSelectedRemoteFolders().length == 1;
	}
}