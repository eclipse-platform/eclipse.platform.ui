package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class CVSModelElement implements IWorkbenchAdapter {
	/**
	 * Handles exceptions that occur in CVS model elements.
	 */
	protected void handle(Throwable t) {
		IStatus error = null;
		if (t instanceof CoreException) {
			error = ((CoreException)t).getStatus();
		} else if (t instanceof TeamException) {
			error = ((TeamException)t).getStatus();
		} else {
			error = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, Policy.bind("simpleInternal"), t); //$NON-NLS-1$
		}
	
		Shell shell = new Shell(Display.getDefault());
	
		if (error.getSeverity() == IStatus.INFO) {
			MessageDialog.openInformation(shell, Policy.bind("information"), error.getMessage()); //$NON-NLS-1$
		} else {
			ErrorDialog.openError(shell, Policy.bind("exception"), null, error); //$NON-NLS-1$
		}
		shell.dispose();
		// Let's log non-team exceptions
		if (!(t instanceof TeamException)) {
			CVSUIPlugin.log(error);
		}
	}
}

