package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

public abstract class ReplaceWithAction extends TeamAction {
	private boolean confirmOverwrite = true;
			
	/**
	 * The user is attempting to load a project that already exists in
	 * the workspace.  Prompt the user to confirm overwrite and return
	 * their choice.
	 */
	protected boolean confirmOverwrite(String msg) throws InterruptedException {
		if (!confirmOverwrite) {
			return true;
		}
		String title = Policy.bind("ReplaceWithAction.Confirm_Overwrite_3"); //$NON-NLS-1$
		final MessageDialog dialog = 
			new MessageDialog(shell, title, null, msg, MessageDialog.QUESTION, 
				new String[] {
					IDialogConstants.YES_LABEL, 
					IDialogConstants.NO_LABEL, 
					IDialogConstants.YES_TO_ALL_LABEL, 
					IDialogConstants.CANCEL_LABEL}, 
				0);
		// run in syncExec because callback is from an operation,
		// which is probably not running in the UI thread.
		shell.getDisplay().syncExec(
			new Runnable() {
				public void run() {
					dialog.open();
				}
			});
		switch (dialog.getReturnCode()) {
			case 0://Yes
				return true;
			case 1://No
				return false;
			case 2://Yes to all
				confirmOverwrite = false; 
				return true;
			case 3://Cancel
			default:
				throw new InterruptedException();
		}
	}
	
	protected boolean getConfirmOverwrite() {
		return confirmOverwrite;
	}
	
	/**
	 * It's important to note that actions have state and subclasses should 
	 * reset the confirmation setting before calling getConfirmOverwrite().
	 */
	protected void setConfirmOverwrite(boolean shouldConfirm) {
		this.confirmOverwrite = shouldConfirm;
	}
}
