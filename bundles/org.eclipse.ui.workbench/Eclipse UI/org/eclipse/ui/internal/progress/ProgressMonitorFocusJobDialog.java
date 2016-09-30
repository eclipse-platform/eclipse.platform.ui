/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 * Patrik Suzzi <psuzzi@gmail.com> - Bug 460683
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.ProgressMonitorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManager.JobMonitor;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The ProgressMonitorFocusJobDialog is a dialog that shows progress for a
 * particular job in a modal dialog so as to give a user accustomed to a modal
 * UI a more familiar feel.
 */
public class ProgressMonitorFocusJobDialog extends ProgressMonitorJobsDialog {
	Job job;
	private boolean showDialog;

	/**
	 * Create a new instance of the receiver with progress reported on the job.
	 *
	 * @param parentShell
	 *            The shell this is parented from.
	 */
	public ProgressMonitorFocusJobDialog(Shell parentShell) {
		super(parentShell == null ? ProgressManagerUtil.getNonModalShell()
				: parentShell);
		setShellStyle(getDefaultOrientation() | SWT.BORDER | SWT.TITLE
				| SWT.RESIZE | SWT.MAX | SWT.MODELESS);
		setCancelable(true);
		enableDetailsButton = true;
	}

	@Override
	protected void cancelPressed() {
		job.cancel();
		super.cancelPressed();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(job.getName());
		shell.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_ESCAPE) {
				cancelPressed();
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button runInWorkspace = createButton(
				parent,
				IDialogConstants.CLOSE_ID,
				ProgressMessages.ProgressMonitorFocusJobDialog_RunInBackgroundButton,
				true);
		runInWorkspace.addSelectionListener(widgetSelectedAdapter(e -> {
			Rectangle shellPosition = getShell().getBounds();
			job.setProperty(IProgressConstants.PROPERTY_IN_DIALOG, Boolean.FALSE);
			finishedRun();
			ProgressManagerUtil.animateDown(shellPosition);
		}));
		runInWorkspace.setCursor(arrowCursor);

		cancel = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		cancel.setCursor(arrowCursor);

		createDetailsButton(parent);
	}

	/**
	 * Returns a listener that will close the dialog when the job completes.
	 *
	 * @return IJobChangeListener
	 */
	private IJobChangeListener createCloseListener() {
		return new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				// first of all, make sure this listener is removed
				event.getJob().removeJobChangeListener(this);
				if (!PlatformUI.isWorkbenchRunning()) {
					return;
				}
				// nothing to do if the dialog is already closed
				if (getShell() == null) {
					return;
				}
				WorkbenchJob closeJob = new WorkbenchJob(
						ProgressMessages.ProgressMonitorFocusJobDialog_CLoseDialogJob) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						Shell currentShell = getShell();
						if (currentShell == null || currentShell.isDisposed()) {
							return Status.CANCEL_STATUS;
						}
						finishedRun();
						return Status.OK_STATUS;
					}
				};
				closeJob.setSystem(true);
				closeJob.schedule();
			}
		};
	}

	@Override
	public int open() {
		int result = super.open();

		// add a listener that will close the dialog when the job completes.
		IJobChangeListener listener = createCloseListener();
		job.addJobChangeListener(listener);
		if (job.getState() == Job.NONE) {
			// if the job completed before we had a chance to add
			// the listener, just remove the listener and return
			job.removeJobChangeListener(listener);
			finishedRun();
			cleanUpFinishedJob();
		}

		return result;
	}

	/**
	 * Opens this dialog for the duration that the given job is running.
	 *
	 * @param jobToWatch
	 * @param originatingShell
	 *            The shell this request was created from. Do not block on this
	 *            shell.
	 */
	public void show(Job jobToWatch, final Shell originatingShell) {
		job = jobToWatch;
		// after the dialog is opened we can get access to its monitor
		job.setProperty(IProgressConstants.PROPERTY_IN_DIALOG, Boolean.TRUE);

		setOpenOnRun(false);
		aboutToRun();

		final Object jobIsDone = new Object();
		final JobChangeAdapter jobListener = new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				synchronized (jobIsDone) {
					jobIsDone.notify();
				}
			}
		};
		job.addJobChangeListener(jobListener);

		// start with a quick busy indicator. Lock the UI as we
		// want to preserve modality
		BusyIndicator.showWhile(PlatformUI.getWorkbench().getDisplay(),
				() -> {
					try {
						synchronized (jobIsDone) {
							if (job.getState() != Job.NONE) {
								jobIsDone.wait(ProgressManagerUtil.SHORT_OPERATION_TIME);
							}
						}
					} catch (InterruptedException e) {
						// Do not log as this is a common operation from the
						// lock listener
					}
				});
		job.removeJobChangeListener(jobListener);

		WorkbenchJob openJob = new WorkbenchJob(
				ProgressMessages.ProgressMonitorFocusJobDialog_UserDialogJob) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				// if the job is done at this point, we don't need the dialog
				if (job.getState() == Job.NONE) {
					finishedRun();
					cleanUpFinishedJob();
					return Status.CANCEL_STATUS;
				}

				// now open the progress dialog if nothing else is
				if (!ProgressManagerUtil.safeToOpen(
						ProgressMonitorFocusJobDialog.this, originatingShell)) {
					return Status.CANCEL_STATUS;
				}

				// Do not bother if the parent is disposed
				if (getParentShell() != null && getParentShell().isDisposed()) {
					return Status.CANCEL_STATUS;
				}

				JobMonitor jobMonitor = ProgressManager.getInstance().progressFor(job);
				Display d = Display.getCurrent();
				IProgressMonitorWithBlocking wrapper = ProgressMonitorUtil
						.createAccumulatingProgressMonitor(getProgressMonitor(), d);
				jobMonitor.addProgressListener(wrapper);
				open();

				return Status.OK_STATUS;
			}
		};
		openJob.setSystem(true);
		openJob.schedule();

	}

	/**
	 * The job finished before we did anything so clean up the finished
	 * reference.
	 */
	private void cleanUpFinishedJob() {
		ProgressManager.getInstance().checkForStaleness(job);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		// Give the job info as the initial details
		getProgressMonitor().setTaskName(
				ProgressManager.getInstance().progressFor(this.job).getJobInfo()
						.getDisplayString());
		return area;
	}

	@Override
	protected void createExtendedDialogArea(Composite parent) {

		showDialog = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.RUN_IN_BACKGROUND);
		final Button showUserDialogButton = new Button(parent, SWT.CHECK);
		showUserDialogButton
				.setText(WorkbenchMessages.WorkbenchPreference_RunInBackgroundButton);
		showUserDialogButton
				.setToolTipText(WorkbenchMessages.WorkbenchPreference_RunInBackgroundToolTip);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		showUserDialogButton.setLayoutData(gd);

		showUserDialogButton.addSelectionListener(widgetSelectedAdapter(e -> showDialog = showUserDialogButton.getSelection()));

		super.createExtendedDialogArea(parent);
	}

	@Override
	public boolean close() {
		if (getReturnCode() != CANCEL)
			WorkbenchPlugin.getDefault().getPreferenceStore().setValue(
					IPreferenceConstants.RUN_IN_BACKGROUND, showDialog);

		return super.close();
	}
}
