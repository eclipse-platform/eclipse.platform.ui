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
package org.eclipse.ui.internal.progress;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
/**
 * The ProgressFeedbackManager is a class that blocks a Thread until a result
 * in the UI has occured. <b>NOTE: This is an experimental API subject to
 * change at any time.
 */
public class ProgressFeedbackManager {
	private ProgressFeedbackDialog dialog;
	List pendingInfos = new ArrayList();
	private UIJob openProgressJob = new UIJob(ProgressMessages
			.getString("ProgressFeedbackManager.OpenFeedbackJob")) {//$NON-NLS-1$
		/*
		 * (non-Javadoc) @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			openProgressRequestDialog();
			return Status.OK_STATUS;
		}
	};
	IStructuredContentProvider contentProvider;
	/**
	 * Create a new instance of the receiver. *
	 */
	ProgressFeedbackManager() {
		contentProvider = getContentProvider();
	}
	/**
	 * Block the current thread until UIJob is served. The message is used to
	 * announce to the user a pending UI Job.
	 * 
	 * Note: This is experimental API and subject to change at any time.
	 * @param job
	 * @param message
	 * @return IStatus
	 * @throws IllegalThreadStateException if this is called from the UIThread
	 * as we do not want to block the UIThread to make a request in the UIThread.
	 * @since 3.0
	 */
	IStatus requestInUI(UIJob job, String message) {
		//We are in the UI Thread so we want to avoid blocking
		if (Thread.currentThread() == Display.getDefault().getThread()) {
			throw new IllegalThreadStateException(ProgressMessages
					.getString("ProgressFeedbackManager.invalidThreadMessage")); //$NON-NLS-1$
		}
		final IStatus[] statuses = new IStatus[1];
		final boolean[] waiting = new boolean[1];
		waiting[0] = true;
		final AwaitingFeedbackInfo info = new AwaitingFeedbackInfo(message, job);
		pendingInfos.add(info);
		job.addJobChangeListener(new JobChangeAdapter() {
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				if (!PlatformUI.isWorkbenchRunning())
					return;
				statuses[0] = event.getResult();
				waiting[0] = false;
				pendingInfos.remove(info);
				refreshDialog();
			}
		});
		openProgressJob.schedule();
		try {
			while(waiting[0])
				job.join();
		} catch (InterruptedException exception) {
			return ProgressManagerUtil.exceptionStatus(exception);
		}
		return statuses[0];
	}
	/**
	 * Bring the request dialog to the front. If it does not exist yet then
	 * create ir first
	 *  
	 */
	void openProgressRequestDialog() {
		if (dialog == null) {
			dialog = new ProgressFeedbackDialog(contentProvider);
			dialog.create();
			dialog.getShell().addDisposeListener(new DisposeListener() {
				/*
				 * (non-Javadoc) @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
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
	 * Get the content provider to use for the feedback dialog.
	 * 
	 * 
	 * @return IStructuredContentProvider
	 */
	private IStructuredContentProvider getContentProvider() {
		return new IStructuredContentProvider() {
			/*
			 * (non-Javadoc) @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return pendingInfos.toArray();
			}
			/*
			 * (non-Javadoc) @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				//No dispose behavior
			}
			/*
			 * (non-Javadoc) @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 * java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}
		};
	}
}