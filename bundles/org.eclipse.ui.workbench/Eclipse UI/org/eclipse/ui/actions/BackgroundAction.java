/*
 * The BackgroundAction is an action that is Thread safe and 
 * can be run outside of the UI Thread.
 */
package org.eclipse.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public abstract class BackgroundAction extends Action {

	Shell shell;

	protected void runAsyncInUI(Runnable runnable) {
		shell.getDisplay().asyncExec(runnable);
	}

	protected void runSyncInUI(Runnable runnable) {
		shell.getDisplay().syncExec(runnable);
	}

	public boolean isThreadSafe() {
		return true;
	}

	/**
	 * Set the shell.
	 * @param shell
	 */
	public void setShell(Shell shell) {
		this.shell = shell;
	}

	protected void reportCoreException(
		final CoreException e,
		final String title) {
		WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e); //$NON-NLS-2$//$NON-NLS-1$

		runAsyncInUI(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				ErrorDialog.openError(
					shell,
					title,
					WorkbenchMessages.format(
						"WorkbenchAction.internalErrorWithMessage",
						new Object[] { e.getMessage()}),
					e.getStatus());
			}
		});
	}

	protected void reportException(final Exception e, final String title) {

		runAsyncInUI(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + e); //$NON-NLS-2$//$NON-NLS-1$
				MessageDialog.openError(
					shell,
					title,
					WorkbenchMessages.format(
						"WorkbenchAction.internalErrorWithMessage",
						new Object[] { e.getMessage()}));
			}
		});
	}

}
