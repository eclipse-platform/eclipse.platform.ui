/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.statushandlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.WorkbenchJob;

import com.ibm.icu.text.DateFormat;

/**
 * The StatusNotificationManager is the class that manages the display of status
 * information.
 */
public class StatusNotificationManager {

	private static final String ERROR_JOB = "errorstate.gif"; //$NON-NLS-1$

	static final String ERROR_JOB_KEY = "ERROR_JOB"; //$NON-NLS-1$

	private Collection errors = Collections.synchronizedSet(new HashSet());

	private StatusDialog dialog;

	private static StatusNotificationManager sharedInstance;

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static StatusNotificationManager getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new StatusNotificationManager();
		}
		return sharedInstance;
	}

	/**
	 * Create a new instance of the receiver.
	 */
	public StatusNotificationManager() {

	}

	/**
	 * Set up any images the error management needs.
	 * 
	 * @param iconsRoot
	 * @throws MalformedURLException
	 */
	void setUpImages(URL iconsRoot) throws MalformedURLException {
		// TODO see ErrorNotificationManager - this method isn't currently used
		// In the ErrorNotificationManager it is invoked by ProgressManager
		JFaceResources.getImageRegistry().put(ERROR_JOB_KEY,
				ImageDescriptor.createFromURL(new URL(iconsRoot, ERROR_JOB)));
	}

	/**
	 * Add a new error to the list for the supplied job.
	 * 
	 * @param status
	 */
	public void addError(IStatus status, Object extension) {
		StatusInfo errorInfo = new StatusInfo(status, extension);
		showError(errorInfo);
	}

	/**
	 * Show the error in the error dialog. This is done from the UI thread to
	 * ensure that no errors are dropped.
	 * 
	 * @param statusInfo
	 *            the error to be displayed
	 */
	private void showError(final StatusInfo statusInfo) {

		if (!PlatformUI.isWorkbenchRunning()) {
			// we are shuttting down, so just log
			WorkbenchPlugin.log(statusInfo.getStatus());
			return;
		}

		// We must open or update the error dialog in the UI thread to ensure
		// that errors are not dropped
		WorkbenchJob job = new WorkbenchJob(
				ProgressMessages.ErrorNotificationManager_OpenErrorDialogJob) {
			public IStatus runInUIThread(IProgressMonitor monitor) {

				// Add the error in the UI thread to ensure thread safety in the
				// dialog
				errors.add(statusInfo);
				if (dialog != null) {
					dialog.refresh();
				} else if (Platform.isRunning()) {
					// Delay prompting if the job property is set
					Object noPromptProperty = null;
					Object extension = statusInfo.getExtension();

					if (extension != null && extension instanceof Job) {
						noPromptProperty = ((Job) extension)
								.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY);
					}

					boolean prompt = true;
					if (noPromptProperty instanceof Boolean) {
						prompt = !((Boolean) noPromptProperty).booleanValue();
					}

					if (prompt) {
						return openErrorDialog(null /* use default title */,
								null /* use default message */, statusInfo);
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
	 * 
	 * @return Collection of ErrorInfo
	 */
	Collection getErrors() {
		return errors;
	}

	/**
	 * The job caleed jobName has just failed with status status. Open the error
	 * dialog if possible - otherwise log the error.
	 * 
	 * @param title
	 *            the title of the dialog or <code>null</code>
	 * @param msg
	 *            the message for the dialog oe <code>null</code>
	 * @param statusInfo
	 *            The info the dialog is being opened for.
	 * @return IStatus
	 */
	private IStatus openErrorDialog(String title, String msg,
			final StatusInfo statusInfo) {
		IWorkbench workbench = PlatformUI.getWorkbench();

		// Abort on shutdown
		if (workbench instanceof Workbench
				&& ((Workbench) workbench).isClosing()) {
			return Status.CANCEL_STATUS;
		}
		dialog = new StatusDialog(ProgressManagerUtil.getDefaultParent(),
				title, msg, statusInfo, IStatus.OK | IStatus.INFO
						| IStatus.WARNING | IStatus.ERROR);

		dialog.open();
		return Status.OK_STATUS;
	}

	/**
	 * TODO unused !!!
	 * 
	 * Remove all of the errors supplied from the list of errors.
	 * 
	 * @param errorsToRemove
	 *            Collection of ErrorInfo
	 */
	void removeErrors(Collection errorsToRemove) {
		errors.removeAll(errorsToRemove);
		removeFromFinishedJobs(errorsToRemove);
	}

	/**
	 * Remove all of the errors from the finished jobs
	 * 
	 * @param errorsToRemove
	 *            The ErrorInfos that will be deleted.
	 */
	private void removeFromFinishedJobs(Collection errorsToRemove) {
		Iterator errorIterator = errorsToRemove.iterator();
		Set errorStatuses = new HashSet();
		while (errorIterator.hasNext()) {
			StatusInfo next = (StatusInfo) errorIterator.next();
			errorStatuses.add(next.getStatus());
		}

		// TODO those classes have default access modifier :/
		// finished jobs are removed when the dialog is shown
		// also - job removal wasn't used in the old workencherrorhandler

		// JobTreeElement[] infos = FinishedJobs.getInstance().getJobInfos();
		// for (int i = 0; i < infos.length; i++) {
		// if (infos[i].isJobInfo()) {
		// JobInfo info = (JobInfo) infos[i];
		// if (errorStatuses.contains(info.getJob().getResult())) {
		// FinishedJobs.getInstance().remove(info);
		// }
		// }
		// }

	}

	/**
	 * Clear all of the errors held onto by the receiver.
	 */
	private void clearAllErrors() {
		removeFromFinishedJobs(errors);
		errors.clear();
	}

	/**
	 * Display the error for the given job and any other errors that have been
	 * accumulated. This method must be invoked from the UI thread.
	 * 
	 * @param job
	 *            the job whose error should be displayed
	 * @param title
	 *            The title for the dialog
	 * @param msg
	 *            The message for the dialog.
	 * @return <code>true</code> if the info for the job was found and the
	 *         error displayed and <code>false</code> otherwise.
	 */
	public boolean showErrorFor(Job job, String title, String msg) {
		if (dialog != null) {
			// The dialog is already open so the error is being displayed
			return true;
		}
		StatusInfo info = null;
		if (job == null) {
			info = getMostRecentJobError();
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
	private StatusInfo getMostRecentJobError() {
		StatusInfo mostRecentInfo = null;
		for (Iterator iter = errors.iterator(); iter.hasNext();) {
			StatusInfo info = (StatusInfo) iter.next();
			if ((mostRecentInfo == null || info.getTimestamp() > mostRecentInfo
					.getTimestamp())
					&& info.getExtension() != null
					&& info.getExtension() instanceof Job) {
				mostRecentInfo = info;
			}
		}
		return mostRecentInfo;
	}

	/*
	 * Return the error info for the given job
	 */
	private StatusInfo getErrorInfo(Job job) {
		for (Iterator iter = errors.iterator(); iter.hasNext();) {
			StatusInfo info = (StatusInfo) iter.next();
			if (info.getExtension() == job) {
				return info;
			}
		}
		return null;
	}

	/**
	 * Return whether the manager has errors to report.
	 * 
	 * @return whether the manager has errors to report
	 */
	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	/**
	 * The error dialog has been closed. Clear the list of errors and the stored
	 * dialog.
	 */
	public void dialogClosed() {
		dialog = null;
		clearAllErrors();
	}

	/**
	 * A wrapper class for statuses displayed in the dialog.
	 * 
	 */
	protected static class StatusInfo implements Comparable {

		private final IStatus status;

		private final long timestamp;

		private final Object extension;

		/**
		 * Constructs a simple <code>StatusInfo</code>, without any
		 * extensions.
		 * 
		 * @param status
		 *            the root status for this status info
		 */
		public StatusInfo(IStatus status) {
			this(status, null);
		}

		/**
		 * Constructs a <code>StatusInfo</code> with a extension (used to
		 * retrieve extra properties).
		 * 
		 * @param status
		 *            the root status for this status info
		 * @param extension
		 *            the extension
		 */
		public StatusInfo(IStatus status, Object extension) {
			this.status = status;
			timestamp = System.currentTimeMillis();
			this.extension = extension;
		}

		String getDisplayString() {
			String text = status.getMessage();
			if (this.extension != null && this.extension instanceof Job) {
				text = ((Job) extension).getName();
			}

			return NLS.bind(ProgressMessages.JobInfo_Error, (new Object[] {
					text,
					DateFormat.getDateTimeInstance(DateFormat.LONG,
							DateFormat.LONG).format(new Date(timestamp)) }));
		}

		/**
		 * Time when this status info was created.
		 * 
		 * @return the time
		 */
		public long getTimestamp() {
			return timestamp;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(T)
		 */
		public int compareTo(Object arg0) {
			if (arg0 instanceof StatusInfo) {
				// Order ErrorInfo by time received
				long otherTimestamp = ((StatusInfo) arg0).timestamp;
				if (timestamp < otherTimestamp) {
					return -1;
				} else if (timestamp > otherTimestamp) {
					return 1;
				} else {
					return getDisplayString().compareTo(
							((StatusInfo) arg0).getDisplayString());
				}
			}
			return 0;
		}

		/**
		 * @return Returns the status.
		 */
		public IStatus getStatus() {
			return status;
		}

		/**
		 * @return Returns the extension.
		 */
		public Object getExtension() {
			return extension;
		}

	}
}
