/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Philipp Bumann <bumannp@gmail.com> - Bug 477602
 ******************************************************************************/

package org.eclipse.e4.ui.progress.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.e4.ui.progress.UIJob;
import org.eclipse.e4.ui.progress.internal.legacy.EventLoopProgressMonitor;
import org.eclipse.e4.ui.progress.internal.legacy.PlatformUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ProgressServiceImpl implements IProgressService {

	private static final String IMAGE_KEY = "org.eclipse.ui.progress.images"; //$NON-NLS-1$

	private Hashtable<Object, String> imageKeyTable = new Hashtable<>();

	@Inject
	@Optional
	ProgressManager progressManager;

	@Inject
	@Optional
	FinishedJobs finishedJobs;

	@Inject
	@Optional
	ContentProviderFactory contentProviderFactory;

	@Inject
	@Optional
	UISynchronize uiSynchronize;

	@Override
	public int getLongOperationTime() {
		return 800;
	}

	@Override
	public void registerIconForFamily(ImageDescriptor icon, Object family) {
		String key = IMAGE_KEY + String.valueOf(imageKeyTable.size());
		imageKeyTable.put(family, key);
		ImageRegistry registry = JFaceResources.getImageRegistry();

		// Avoid registering twice
		if (registry.getDescriptor(key) == null) {
			registry.put(key, icon);
		}
	}

	@Override
	public void runInUI(IRunnableContext context,
	        IRunnableWithProgress runnable, ISchedulingRule rule)
	        throws InvocationTargetException, InterruptedException {
		final RunnableWithStatus runnableWithStatus = new RunnableWithStatus(
				context,
				runnable, rule);
		uiSynchronize.syncExec(new Runnable() {
			@Override
			public void run() {
				BusyIndicator.showWhile(getDisplay(), runnableWithStatus);
			}

		});

		IStatus status = runnableWithStatus.getStatus();
		if (!status.isOK()) {
			Throwable exception = status.getException();
			if (exception instanceof InvocationTargetException)
				throw (InvocationTargetException) exception;
			else if (exception instanceof InterruptedException)
				throw (InterruptedException) exception;
			else // should be OperationCanceledException
				throw new InterruptedException(exception.getMessage());
		}
	}

	@Override
	public Image getIconFor(Job job) {
		Enumeration<Object> families = imageKeyTable.keys();
		while (families.hasMoreElements()) {
			Object next = families.nextElement();
			if (job.belongsTo(next)) {
				return JFaceResources.getImageRegistry().get(imageKeyTable.get(next));
			}
		}
		return null;
	}

	@Override
	public void busyCursorWhile(final IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(
		        ProgressManagerUtil.getDefaultParent(), this, progressManager,
		        contentProviderFactory, finishedJobs);
		dialog.setOpenOnRun(false);
		final InvocationTargetException[] invokes = new InvocationTargetException[1];
		final InterruptedException[] interrupt = new InterruptedException[1];
		// show a busy cursor until the dialog opens
		Runnable dialogWaitRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					dialog.setOpenOnRun(false);
					setUserInterfaceActive(false);
					dialog.run(true, true, runnable);
				} catch (InvocationTargetException e) {
					invokes[0] = e;
				} catch (InterruptedException e) {
					interrupt[0] = e;
				} finally {
					setUserInterfaceActive(true);
				}
			}
		};
		busyCursorWhile(dialogWaitRunnable, dialog);
		if (invokes[0] != null) {
			throw invokes[0];
		}
		if (interrupt[0] != null) {
			throw interrupt[0];
		}
	}

	@Override
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		if (fork == false || cancelable == false) {
			// backward compatible code
			final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(
			        null, this, progressManager, contentProviderFactory,
			        finishedJobs);
			dialog.run(fork, cancelable, runnable);
			return;
		}

		busyCursorWhile(runnable);
	}


	@Override
	public void showInDialog(Shell shell, Job job) {
		if (shouldRunInBackground()) {
			return;
		}

		final ProgressMonitorFocusJobDialog dialog = new ProgressMonitorFocusJobDialog(
		        shell, this, progressManager, contentProviderFactory,
		        finishedJobs);
		dialog.show(job, shell);
	}

	/**
	 * Return whether or not dialogs should be run in the background
	 *
	 * @return <code>true</code> if the dialog should not be shown.
	 */
	protected boolean shouldRunInBackground() {
		return Preferences.getBoolean(IProgressConstants.RUN_IN_BACKGROUND);
	}

	private class RunnableWithStatus implements Runnable {

		IStatus status = Status.OK_STATUS;
		private final IRunnableContext context;
		private final IRunnableWithProgress runnable;
		private final ISchedulingRule rule;

		public RunnableWithStatus(IRunnableContext context,
				IRunnableWithProgress runnable, ISchedulingRule rule) {
			this.context = context;
			this.runnable = runnable;
			this.rule = rule;
		}

		@Override
		public void run() {
			IJobManager manager = Job.getJobManager();
			try {
				manager.beginRule(rule, getEventLoopMonitor());
				context.run(false, false, runnable);
			} catch (InvocationTargetException e) {
				status = new Status(IStatus.ERROR, IProgressConstants.PLUGIN_ID, e
						.getMessage(), e);
			} catch (InterruptedException e) {
				status = new Status(IStatus.ERROR, IProgressConstants.PLUGIN_ID, e
						.getMessage(), e);
			} catch (OperationCanceledException e) {
				status = new Status(IStatus.ERROR, IProgressConstants.PLUGIN_ID, e
						.getMessage(), e);
			} finally {
				manager.endRule(rule);
			}
		}

		/**
		 * Get a progress monitor that forwards to an event loop monitor.
		 * Override #setBlocked() so that we always open the blocked dialog.
		 *
		 * @return the monitor on the event loop
		 */
		private IProgressMonitor getEventLoopMonitor() {

			if (PlatformUI.isWorkbenchStarting())
				return new NullProgressMonitor();

			return new EventLoopProgressMonitor(new NullProgressMonitor()) {

				@Override
				public void setBlocked(IStatus reason) {

					// Set a shell to open with as we want to create
					// this
					// even if there is a modal shell.
					Dialog.getBlockedHandler().showBlocked(
							ProgressManagerUtil.getDefaultParent(), this,
							reason, getTaskName());
				}
			};
		}

		public IStatus getStatus() {
			return status;
		}

	}

	/**
	 * Show the busy cursor while the runnable is running. Schedule a job to
	 * replace it with a progress dialog.
	 *
	 * @param dialogWaitRunnable
	 * @param dialog
	 */
	private void busyCursorWhile(Runnable dialogWaitRunnable,
			ProgressMonitorJobsDialog dialog) {
		// create the job that will open the dialog after a delay
		scheduleProgressMonitorJob(dialog);
		final Display display = getDisplay();
		if (display == null) {
			return;
		}
		// show a busy cursor until the dialog opens
		BusyIndicator.showWhile(display, dialogWaitRunnable);
	}

	/**
	 * Schedule the job that will open the progress monitor dialog
	 *
	 * @param dialog
	 *            the dialog to open
	 */
	private void scheduleProgressMonitorJob(
			final ProgressMonitorJobsDialog dialog) {

		final Job updateJob = new UIJob(
				ProgressMessages.ProgressManager_openJobName) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				setUserInterfaceActive(true);
				if (ProgressManagerUtil.safeToOpen(dialog, null)) {
					dialog.open();
				}
				return Status.OK_STATUS;
			}
		};
		updateJob.setSystem(true);
		updateJob.schedule(getLongOperationTime());

	}

	/**
	 * Iterate through all of the windows and set them to be disabled or enabled
	 * as appropriate.'
	 *
	 * @param active
	 *            The set the windows will be set to.
	 */
	private void setUserInterfaceActive(boolean active) {
		Shell[] shells = getDisplay().getShells();
		if (active) {
			for (int i = 0; i < shells.length; i++) {
				if (!shells[i].isDisposed()) {
					shells[i].setEnabled(active);
				}
			}
		} else {
			// Deactive shells in reverse order
			for (int i = shells.length - 1; i >= 0; i--) {
				if (!shells[i].isDisposed()) {
					shells[i].setEnabled(active);
				}
			}
		}
	}

	protected Display getDisplay() {
		return Services.getInstance().getDisplay();
	}

}
