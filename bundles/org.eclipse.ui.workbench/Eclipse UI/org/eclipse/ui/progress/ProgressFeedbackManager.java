/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.internal.progress.AwaitingFeedbackInfo;
import org.eclipse.ui.internal.progress.ProgressFeedbackDialog;
import org.eclipse.ui.internal.progress.ProgressMessages;

/**
 * The ProgressFeedbackManager is a class that blocks a Thread
 * until a result in the UI has occured.
 */
public class ProgressFeedbackManager {

	private static ProgressFeedbackManager singleton;
	private ProgressFeedbackDialog dialog;
	List pendingInfos = new ArrayList();

		private UIJob openProgressJob = new UIJob(ProgressMessages.getString("ProgressFeedbackManager.OpenFeedbackJob")) {//$NON-NLS-1$
	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			openProgressRequestDialog();
			return Status.OK_STATUS;
		}
	};
	
	/**
	 * Get the feedback manager.
	 * 
	 * Note: This is experimental API and subject to change
	 * at any time.
	 * @return ProgressFeedbackManager
	 * @since 3.0
	 */

	public static ProgressFeedbackManager getFeedbackManager() {
		if (singleton == null)
			singleton = new ProgressFeedbackManager();
		return singleton;
	}

	IStructuredContentProvider contentProvider;
	
	/**
	 * Create a new instance of the receiver.	 *
	 */
	ProgressFeedbackManager() {
		contentProvider = getContentProvider();
	}
	/**
	 * Block the current thread until UIJob is served. The message
	 * is used to announce to the user a pending UI Job.
	 * 
	 * Note: This is experimental API and subject to change
	 * at any time.
	 * 
	 * @param job
	 * @param message
	 * @return IStatus
	 * @since 3.0
	 */
	public IStatus requestInUI(UIJob job, String message) {

		final IStatus[] statuses = new IStatus[1];
		final boolean[] wait = { true };
		final AwaitingFeedbackInfo info = new AwaitingFeedbackInfo(message, job);
		pendingInfos.add(info);

		job.addJobChangeListener(new JobChangeAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				statuses[0] = event.getResult();
				wait[0] = false;
				pendingInfos.remove(info);
				refreshDialog();
			}
		});

		openProgressJob.schedule();

		while (wait[0]) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}
		}
		return statuses[0];

	}

	/**
	 * Bring the request dialog to the front. If it does not exist
	 * yet then create ir first
	 *
	 */
	void openProgressRequestDialog() {
		if (dialog == null) {
			dialog = new ProgressFeedbackDialog(contentProvider);
			dialog.create();
			dialog.getShell().addDisposeListener(new DisposeListener() {
				/* (non-Javadoc)
				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
				 */
				public void widgetDisposed(DisposeEvent arg0) {
					clearDialog();

				}
			});
			dialog.open();
		} else {
			refreshDialog();
			dialog.getShell().forceFocus();
		}

	}

	/**
	 * Clear the dialog instance variable.
	 */
	void clearDialog() {
		dialog = null;
	}

	/**
	 * Refresh the viewer in the dialog.
	 */
	void refreshDialog() {
		dialog.refreshViewer();
	}

	/**
	 * Get the content provider to use for the feedback
	 * dialog.
	 * @return IStructuredContentProvider
	 */
	private IStructuredContentProvider getContentProvider() {
		return new IStructuredContentProvider() {
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return pendingInfos.toArray();
			}
			/* (non-Javadoc)
			* @see org.eclipse.jface.viewers.IContentProvider#dispose()
			*/
			public void dispose() {
			}

			/* (non-Javadoc)
			* @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			*/
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}
		};
	}
}
