/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.jface.dialogs.IDialogBlockedHandler;
import org.eclipse.swt.widgets.Shell;

/**
 * The WorkbenchWizardBlockedHandler is the class that implements the blocked
 * handler for the workbench.
 */
@Creatable
@Singleton
public class WorkbenchDialogBlockedHandler implements IDialogBlockedHandler {
    IProgressMonitor outerMonitor;

    @Inject
    @Optional
    IProgressService progressService;

    @Inject
    @Optional
    ProgressManager progressManager;

    @Inject
    @Optional
    FinishedJobs finishedJobs;

    @Inject
    @Optional
    ProgressViewUpdater progressViewUpdater;




    int nestingDepth = 0;

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
			        blockingStatus, blockedName, progressService, finishedJobs,
			        progressViewUpdater, progressManager);
        }

    }

    @Override
	public void showBlocked(IProgressMonitor blocking, IStatus blockingStatus,
            String blockedName) {
        showBlocked(null, blocking, blockingStatus, blockedName);
    }
}
