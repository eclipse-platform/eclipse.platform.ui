/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        //No default behavior
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogBlockedHandler#clearBlocked()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogBlockedHandler#showBlocked(org.eclipse.swt.widgets.Shell,
     *      org.eclipse.core.runtime.IProgressMonitor,
     *      org.eclipse.core.runtime.IStatus, java.lang.String)
     */
    public void showBlocked(Shell parentShell,
            IProgressMonitor blockingMonitor, IStatus blockingStatus,
            String blockedName) {

        nestingDepth++;
        if (outerMonitor == null) {
            outerMonitor = blockingMonitor;
            //Try to get a name as best as possible
            if (blockedName == null && parentShell != null) {
				blockedName = parentShell.getText();
			}
            BlockedJobsDialog.createBlockedDialog(parentShell, blockingMonitor,
                    blockingStatus, blockedName);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogBlockedHandler#showBlocked(org.eclipse.core.runtime.IProgressMonitor,
     *      org.eclipse.core.runtime.IStatus, java.lang.String)
     */
    public void showBlocked(IProgressMonitor blocking, IStatus blockingStatus,
            String blockedName) {
        showBlocked(null, blocking, blockingStatus, blockedName);
    }
}
