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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusAdapter;

import com.ibm.icu.text.DateFormat;

/**
 * The StatusNotificationManager is the class that manages the display of status
 * information.
 */
public class StatusNotificationManager {

	private Collection errors = Collections.synchronizedSet(new HashSet());

	private StatusDialog dialog;
	
	private boolean dialogOpened = false;

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
	 * Add a new error to the list for the supplied job.
	 * 
	 * @param status
	 */
	public void addError(StatusAdapter statusAdapter) {
		StatusInfo errorInfo = new StatusInfo(statusAdapter);
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
			WorkbenchPlugin.log(statusInfo.getStatus().getStatus());
			return;
		}

		// IWorkbench workbench = PlatformUI.getWorkbench();
		//
		// // Abort on shutdown
		// if (workbench instanceof Workbench
		// && ((Workbench) workbench).isClosing()) {
		// return Status.CANCEL_STATUS;
		// }

		// Add the error in the UI thread to ensure thread safety in the
		// dialog
		 if (dialogOpened == true) {
			statusInfo.getStatus().setProperty(
					IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
					Boolean.FALSE);
			WorkbenchJob job = new WorkbenchJob(
					ProgressMessages.ErrorNotificationManager_OpenErrorDialogJob) {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					errors.add(statusInfo);
					dialog.refresh();
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		} else if (Platform.isRunning()) {
			errors.add(statusInfo);
			// Delay prompting if the status adapter property is set
			Object noPromptProperty = null;
			if (statusInfo.getStatus().getProperty(
					IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) == Boolean.TRUE) {
				noPromptProperty = Boolean.TRUE;
			} else {
				noPromptProperty = Boolean.FALSE;
			}

			boolean prompt = true;
			if (noPromptProperty instanceof Boolean) {
				prompt = !((Boolean) noPromptProperty).booleanValue();
			}

			if (prompt) {
				dialogOpened = true;
				WorkbenchJob job = new WorkbenchJob(
						ProgressMessages.ErrorNotificationManager_OpenErrorDialogJob) {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						dialog = new StatusDialog(ProgressManagerUtil
								.getDefaultParent(), statusInfo, IStatus.OK
								| IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
						dialog.open();
						dialog.refresh();
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
		}
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
	 * Clear all of the errors held onto by the receiver.
	 */
	private void clearAllErrors() {
		errors.clear();
	}

	/**
	 * The error dialog has been closed. Clear the list of errors and the stored
	 * dialog.
	 */
	public void dialogClosed() {
		dialog = null;
		dialogOpened = false;
		clearAllErrors();
	}

	/**
	 * A wrapper class for statuses displayed in the dialog.
	 * 
	 */
	protected static class StatusInfo implements Comparable {

		private final StatusAdapter statusAdapter;

		/**
		 * Constructs a simple <code>StatusInfo</code>, without any
		 * extensions.
		 * 
		 * @param status
		 *            the root status for this status info
		 */
		public StatusInfo(StatusAdapter statusAdapter) {
			this.statusAdapter = statusAdapter;

			Object timestampProperty = statusAdapter
					.getProperty(StatusAdapter.TIMESTAMP_PROPERTY);

			if (timestampProperty == null
					|| !(timestampProperty instanceof Long)) {
				statusAdapter.setProperty(StatusAdapter.TIMESTAMP_PROPERTY,
						new Long(System.currentTimeMillis()));
			}
		}

		String getDisplayString() {
			String text = statusAdapter.getStatus().getMessage();

			Job job = (Job) (statusAdapter.getAdapter(Job.class));
			if (job != null) {
				text = job.getName();
			}

			return NLS.bind(ProgressMessages.JobInfo_Error, (new Object[] {
					text,
					DateFormat.getDateTimeInstance(DateFormat.LONG,
							DateFormat.LONG).format(new Date(getTimestamp())) }));
		}

		/**
		 * Time when this status info was created.
		 * 
		 * @return the time
		 */
		public long getTimestamp() {
			return ((Long) statusAdapter
					.getProperty(StatusAdapter.TIMESTAMP_PROPERTY)).longValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(T)
		 */
		public int compareTo(Object arg0) {
			if (arg0 instanceof StatusInfo) {
				// Order ErrorInfo by time received
				long otherTimestamp = ((StatusInfo) arg0).getTimestamp();
				if (getTimestamp() < otherTimestamp) {
					return -1;
				} else if (getTimestamp() > otherTimestamp) {
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
		public StatusAdapter getStatus() {
			return statusAdapter;
		}
	}
}
