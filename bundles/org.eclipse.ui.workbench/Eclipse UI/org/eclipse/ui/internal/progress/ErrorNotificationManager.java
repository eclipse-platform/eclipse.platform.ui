/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ExceptionHandler;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The ErrorNotificationManager is the class that manages the display of
 * error information.
 */
public class ErrorNotificationManager {

    private static final String ERROR_JOB = "errorstate.gif"; //$NON-NLS-1$

    static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

    private Collection errors = Collections.synchronizedSet(new HashSet());

    private ErrorNotificationDialog dialog;

    private boolean dialogActive = false;

    /**
     * Create a new instance of the receiver.
     */
    public ErrorNotificationManager() {
        //No special initialization
    }

    /**
     * Set up any images the error management needs.
     * @param iconsRoot
     * @throws MalformedURLException
     */
    void setUpImages(URL iconsRoot) throws MalformedURLException {
        JFaceResources.getImageRegistry().put(ERROR_JOB_KEY,
                ImageDescriptor.createFromURL(new URL(iconsRoot, ERROR_JOB)));
    }

    /**
     * Add a new error to the list.
     * @param status
     * @param jobName
     */
    void addError(IStatus status, String jobName) {

        //Handle out of memory errors via the workbench
        final Throwable exception = status.getException();
        if (exception != null && exception instanceof OutOfMemoryError) {
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    ExceptionHandler.getInstance().handleException(exception);
                }
            });

            return;
        }
        errors.add(new ErrorInfo(status, jobName));
        if (dialogActive) {
            if (dialog != null)
                refreshDialog();
        } else
            openErrorDialog(jobName, status);
    }

    /**
     * 
     */
    private void refreshDialog() {

        UIJob refreshJob = new UIJob(ProgressMessages
                .getString("ErrorNotificationManager.RefreshErrorDialogJob")) { //$NON-NLS-1$
            /* (non-Javadoc)
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            public IStatus runInUIThread(IProgressMonitor monitor) {
                dialog.refresh();
                return Status.OK_STATUS;
            }
        };

        refreshJob.setSystem(true);
        refreshJob.schedule();

    }

    /**
     * Get the currently registered errors in the receiver.
     * @return Collection of ErrorInfo
     */
    Collection getErrors() {
        return errors;
    }

    /**
     * The job caleed jobName has just failed with status status.
     * Open the error dialog if possible - otherwise log the error.
     * @param jobName String. The name of the Job
     * @param status IStatus The status of the failure.
     */
    private void openErrorDialog(String jobName, IStatus status) {

        if (!PlatformUI.isWorkbenchRunning()) {
            //We are shutdown so just log
            WorkbenchPlugin.log(jobName, status);
            return;
        }

        dialogActive = true;
        WorkbenchJob job = new WorkbenchJob(ProgressMessages
                .getString("ErrorNotificationManager.OpenErrorDialogJob")) { //$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            public IStatus runInUIThread(IProgressMonitor monitor) {
                IWorkbench workbench = PlatformUI.getWorkbench();

                //Abort on shutdown
                if (workbench instanceof Workbench
                        && ((Workbench) workbench).isClosing())
                    return Status.CANCEL_STATUS;
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

                if (window == null)
                    return Status.CANCEL_STATUS;
                dialog = new ErrorNotificationDialog(window.getShell());
                dialog.open();
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
    }

    /**
     * Remove all of the errors supplied from the list of errors.
     * @param errorsToRemove Collection of ErrorInfo
     */
    void removeErrors(Collection errorsToRemove) {
        errors.removeAll(errorsToRemove);
    }

    /**
     * Clear all of the errors held onto by the receiver.
     */
    void clearAllErrors() {
        errors.clear();
    }

    /**
     * Remove the reference to the errors dialog.
     */
    void clearDialog() {
        dialog = null;
        dialogActive = false;
    }
}