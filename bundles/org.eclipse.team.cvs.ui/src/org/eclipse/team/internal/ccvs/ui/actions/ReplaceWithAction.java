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
	 * Copied from CVSDecorationRunnable
	 */
	protected boolean isDirty(IResource resource) {
		final CoreException DECORATOR_EXCEPTION = new CoreException(new Status(IStatus.OK, "id", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {

					// a project can't be dirty, continue with its children
					if (resource.getType() == IResource.PROJECT) {
						return true;
					}
					
					// if the resource does not exist in the workbench or on the file system, stop searching.
					if(!resource.exists()) {
						return false;
					}

					ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
					try {
						if (!cvsResource.isManaged()) {
							if (cvsResource.isIgnored()) {
								return false;
							} else {
								// new resource, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
						if (!cvsResource.isFolder()) {
							if (((ICVSFile) cvsResource).isModified()) {
								// file has changed, show as dirty
								throw DECORATOR_EXCEPTION;
							}
						}
					} catch (CVSException e) {
						return true;
					}
					// no change -- keep looking in children
					return true;
				}
			}, IResource.DEPTH_INFINITE, true);
		} catch (CoreException e) {
			//if our exception was caught, we know there's a dirty child
			return e == DECORATOR_EXCEPTION;
		}
		return false;
	}	
	
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
}
