package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

public abstract class CVSWizard extends Wizard {
	/**
	 * Convenience method for running an operation with progress and
	 * error feedback.
	 * 
	 * @param runnable  the runnable to run
	 * @param problemMessage an optional message to display in case of errors
	 */
	protected void run(IRunnableWithProgress runnable, String problemMessage) {
		try {
			new ProgressMonitorDialog(getShell()).run(true, true, runnable);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			IStatus status = null;
			if (t instanceof CoreException) {
				status = ((CoreException)t).getStatus();
				CVSUIPlugin.log(status);
			} else if (t instanceof TeamException) {
				status = ((TeamException)t).getStatus();
			} else {
				status = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, problemMessage, t);
				problemMessage = Policy.bind("simpleInternal");
				CVSUIPlugin.log(status);
			}
			ErrorDialog.openError(getShell(), problemMessage, null, status);
		} catch (InterruptedException e) {
		}
	}
}

