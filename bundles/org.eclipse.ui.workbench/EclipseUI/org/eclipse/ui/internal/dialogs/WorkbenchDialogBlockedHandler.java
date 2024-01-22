/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogBlockedHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.progress.BlockedJobsDialog;

/**
 * The WorkbenchWizardBlockedHandler is the class that implements the blocked
 * handler for the workbench.
 */
public class WorkbenchDialogBlockedHandler implements IDialogBlockedHandler {
	IProgressMonitor outerMonitor;

	int nestingDepth = 0;

	/**
	 * Create a new instance of the receiver.
	 */
	public WorkbenchDialogBlockedHandler() {
		// No default behavior
	}

	@Override
	public void clearBlocked() {
		if (nestingDepth == 0) {
			return;
		}

		nestingDepth--;

		if (nestingDepth <= 0) {
			BlockedJobsDialog.clear(outerMonitor);
			outerMonitor = null;
			nestingDepth = 0;
		}

	}

	@Override
	public void showBlocked(Shell parentShell, IProgressMonitor blockingMonitor, IStatus blockingStatus,
			String blockedName) {

		nestingDepth++;
		if (outerMonitor == null) {
			outerMonitor = blockingMonitor;
			BlockedJobsDialog.createBlockedDialog(parentShell, blockingMonitor, blockingStatus);
		}

	}

	@Override
	public void showBlocked(IProgressMonitor blocking, IStatus blockingStatus, String blockedName) {
		showBlocked(null, blocking, blockingStatus, blockedName);
	}
}
