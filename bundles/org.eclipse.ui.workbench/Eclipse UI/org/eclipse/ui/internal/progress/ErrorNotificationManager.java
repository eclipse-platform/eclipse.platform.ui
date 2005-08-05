/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ExceptionHandler;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The ErrorNotificationManager is the class that manages the display of
 * error information.
 */
public class ErrorNotificationManager {

    private static final String ERROR_JOB = "errorstate.gif"; //$NON-NLS-1$

    static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

    private Collection errors = Collections.synchronizedSet(new HashSet());

    private JobErrorDialog dialog;

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
     * Add a new error to the list for the supplied job.
     * @param status
     * @param job
     */
    void addError(IStatus status, Job job) {

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
        ErrorInfo errorInfo = new ErrorInfo(status, job);
        showError(errorInfo);
    }

    /**
     * Show the error in the error dialog. This is done from the UI thread to
     * ensure that no errors are dropped.
     * @param errorInfo the error to be displayed
     */
    private void showError(final ErrorInfo errorInfo) {
        
        if (!PlatformUI.isWorkbenchRunning()) {
            //We are shutdown so just log
            WorkbenchPlugin.log(errorInfo.getJob().getName(), errorInfo.getErrorStatus());
            return;
        }

        // We must open or update the error dialog in the UI thread to ensure that
        // errors are not dropped
        WorkbenchJob job = new WorkbenchJob(ProgressMessages.ErrorNotificationManager_OpenErrorDialogJob) { 
            public IStatus runInUIThread(IProgressMonitor monitor) {
            	    	 
                // Add the error in the UI thread to ensure thread safety in the dialog
                errors.add(errorInfo);
                if (dialog != null) {
                    dialog.refresh();
                } else if (Platform.isRunning()) {
                    // Delay prompting if the job property is set
                    Object noPromptProperty = errorInfo.getJob().getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY);
                    boolean prompt= true;
                    if (noPromptProperty instanceof Boolean) 
                        prompt = !((Boolean)noPromptProperty).booleanValue();
                    
                    if (prompt) {
                        return openErrorDialog(null /* use default title */, null /* use default message */, errorInfo);
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.schedule();
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
     * @param title the title of the dialog or <code>null</code>
     * @param msg the message for the dialog oe <code>null</code>
     * @param errorInfo The info the dialog is being opened for.
     * @return IStatus
     */
    private IStatus openErrorDialog(String title, String msg, final ErrorInfo errorInfo) {
        IWorkbench workbench = PlatformUI.getWorkbench();

        //Abort on shutdown
        if (workbench instanceof Workbench
                && ((Workbench) workbench).isClosing())
            return Status.CANCEL_STATUS;
        dialog = new JobErrorDialog(ProgressManagerUtil.getDefaultParent(), title, msg, errorInfo, IStatus.OK
                | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
        
        dialog.open();
        return Status.OK_STATUS;
    }

    /**
     * Remove all of the errors supplied from the list of errors.
     * @param errorsToRemove Collection of ErrorInfo
     */
    void removeErrors(Collection errorsToRemove) {
        errors.removeAll(errorsToRemove);
        removeFromFinishedJobs(errorsToRemove);
    }

    /**
     * Remove all of the errors from the finished jobs
	 * @param errorsToRemove The ErrorInfos that will be deleted.
	 */
	private void removeFromFinishedJobs(Collection errorsToRemove) {
		Iterator errorIterator = errorsToRemove.iterator();
		Set errorStatuses = new HashSet();
		while(errorIterator.hasNext()){
			ErrorInfo next = (ErrorInfo) errorIterator.next();
			errorStatuses.add(next.getErrorStatus());			
		}
		
		JobTreeElement[] infos = FinishedJobs.getInstance().getJobInfos();
		for (int i = 0; i < infos.length; i++) {
			if(infos[i].isJobInfo()){
				JobInfo info = (JobInfo) infos[i];
				if(errorStatuses.contains(info.getJob().getResult()))
					FinishedJobs.getInstance().remove(info);
			}
		}
		
	}

	/**
     * Clear all of the errors held onto by the receiver.
     */
    private void clearAllErrors() {
    	removeFromFinishedJobs(errors);
        errors.clear();
    }

    /**
     * Display the error for the given job and any other errors
     * that have been accumulated. This method must be invoked
     * from the UI thread.
     * @param job the job whose error should be displayed
     * @param title The title for the dialog
     * @param msg The message for the dialog.
     * @return <code>true</code> if the info for the job was found and the error
     * displayed and <code>false</code> otherwise.
     */
    public boolean showErrorFor(Job job, String title, String msg) {
        if (dialog != null) {
            // The dialog is already open so the error is being displayed
            return true;
        }
        ErrorInfo info = getErrorInfo(job);
        if (job == null) {
            info = getMostRecentError();
        } else {
            info = getErrorInfo(job);
        }
        if (info != null) {
	        openErrorDialog(title, msg, info);
	        return true;
        }
        return false;
    }

    /*
     * Return the most recent error.
     */
    private ErrorInfo getMostRecentError() {
        ErrorInfo mostRecentInfo = null;
        for (Iterator iter = errors.iterator(); iter.hasNext();) {
            ErrorInfo info = (ErrorInfo) iter.next();
            if (mostRecentInfo == null || info.getTimestamp() > mostRecentInfo.getTimestamp()) {
                mostRecentInfo = info;
            }
        }
        return mostRecentInfo;
    }

    /*
     * Return the error info for the given job
     */
    private ErrorInfo getErrorInfo(Job job) {
        for (Iterator iter = errors.iterator(); iter.hasNext();) {
            ErrorInfo info = (ErrorInfo) iter.next();
            if (info.getJob() == job) {
                return info;
            }
        }
        return null;
    }

    /**
     * Return whether the manager has errors to report.
     * @return whether the manager has errors to report
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * The error dialog has been closed. Clear the list of errors and
     * the stored dialog.
     */
	public void dialogClosed() {
        dialog = null;
        clearAllErrors();
	}
}
