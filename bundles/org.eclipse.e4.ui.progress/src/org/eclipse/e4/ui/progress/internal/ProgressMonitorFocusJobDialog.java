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
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.e4.ui.progress.UIJob;
import org.eclipse.e4.ui.progress.internal.legacy.PlatformUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * The ProgressMonitorFocusJobDialog is a dialog that shows progress for a
 * particular job in a modal dialog so as to give a user accustomed to a modal
 * UI a more familiar feel.
 */
class ProgressMonitorFocusJobDialog extends ProgressMonitorJobsDialog {
	Job job;
	private boolean showDialog;
	private ProgressManager progressManager;

	/**
	 * Create a new instance of the receiver with progress reported on the job.
	 *
	 * @param parentShell
	 *            The shell this is parented from.
	 */
	public ProgressMonitorFocusJobDialog(Shell parentShell,
	        IProgressService progressService, ProgressManager progressManager,
	        ContentProviderFactory contentProviderFactory, FinishedJobs finishedJobs) {
		super(parentShell == null ? ProgressManagerUtil.getNonModalShell()
		        : parentShell, progressService, progressManager,
		        contentProviderFactory, finishedJobs);
		this.progressManager = progressManager;
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
		shell.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE) {
					cancelPressed();
					e.detail = SWT.TRAVERSE_NONE;
					e.doit = true;
				}
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
		runInWorkspace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Rectangle shellPosition = getShell().getBounds();
				job.setProperty(IProgressConstants.PROPERTY_IN_DIALOG,
						Boolean.FALSE);
				finishedRun();
				//TODO E4
				//ProgressManagerUtil.animateDown(shellPosition);
			}
		});
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
				Job closeJob = new UIJob(
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

	/**
	 * Return the ProgressMonitorWithBlocking for the receiver.
	 *
	 * @return IProgressMonitorWithBlocking
	 */
	private IProgressMonitorWithBlocking getBlockingProgressMonitor() {
		return new IProgressMonitorWithBlocking() {
			@Override
			public void beginTask(String name, int totalWork) {
				final String finalName = name;
				final int finalWork = totalWork;
				runAsync(new Runnable() {
					@Override
					public void run() {
						getProgressMonitor().beginTask(finalName, finalWork);
					}
				});
			}

			@Override
			public void clearBlocked() {
				runAsync(new Runnable() {
					@Override
					public void run() {
						((IProgressMonitorWithBlocking) getProgressMonitor())
								.clearBlocked();
					}
				});
			}

			@Override
			public void done() {
				runAsync(new Runnable() {
					@Override
					public void run() {
						getProgressMonitor().done();
					}
				});
			}

			@Override
			public void internalWorked(double work) {
				final double finalWork = work;
				runAsync(new Runnable() {
					@Override
					public void run() {
						getProgressMonitor().internalWorked(finalWork);
					}
				});
			}

			@Override
			public boolean isCanceled() {
				return getProgressMonitor().isCanceled();
			}

			/**
			 * Run the runnable as an asyncExec if we are already open.
			 *
			 * @param runnable
			 */
			private void runAsync(final Runnable runnable) {

				if (alreadyClosed) {
					return;
				}
				Shell currentShell = getShell();

				Display display;
				if (currentShell == null) {
					display = Display.getDefault();
				} else {
					if (currentShell.isDisposed())// Don't bother if it has
						// been closed
						return;
					display = currentShell.getDisplay();
				}

				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (alreadyClosed) {
							return;// Check again as the async may come too
							// late
						}
						Shell shell = getShell();
						if (shell != null && shell.isDisposed())
							return;

						runnable.run();
					}
				});
			}

			@Override
			public void setBlocked(IStatus reason) {
				final IStatus finalReason = reason;
				runAsync(new Runnable() {
					@Override
					public void run() {
						((IProgressMonitorWithBlocking) getProgressMonitor())
								.setBlocked(finalReason);
					}
				});
			}

			@Override
			public void setCanceled(boolean value) {
				// Just a listener - doesn't matter.
			}

			@Override
			public void setTaskName(String name) {
				final String finalName = name;
				runAsync(new Runnable() {
					@Override
					public void run() {
						getProgressMonitor().setTaskName(finalName);
					}
				});
			}

			@Override
			public void subTask(String name) {
				final String finalName = name;
				runAsync(new Runnable() {
					@Override
					public void run() {
						getProgressMonitor().subTask(finalName);
					}
				});
			}

			@Override
			public void worked(int work) {
				internalWorked(work);
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

		progressManager.progressFor(job).addProgressListener(
				getBlockingProgressMonitor());

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
		BusyIndicator.showWhile(getDisplay(),
				new Runnable() {
					@Override
					public void run() {
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
					}
				});
		job.removeJobChangeListener(jobListener);

		Job openJob = new UIJob(
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
		progressManager.checkForStaleness(job);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		// Give the job info as the initial details
		getProgressMonitor().setTaskName(
				progressManager.getJobInfo(this.job)
						.getDisplayString());
		return area;
	}

	@Override
	protected void createExtendedDialogArea(Composite parent) {
		showDialog = (Preferences.getBoolean(IProgressConstants.RUN_IN_BACKGROUND));
		final Button showUserDialogButton = new Button(parent, SWT.CHECK);
		showUserDialogButton
				.setText(ProgressMessages.WorkbenchPreference_RunInBackgroundButton);
		showUserDialogButton
				.setToolTipText(ProgressMessages.WorkbenchPreference_RunInBackgroundToolTip);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = GridData.FILL;
		showUserDialogButton.setLayoutData(gd);

		showUserDialogButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showDialog = showUserDialogButton.getSelection();
			}
		});

		super.createExtendedDialogArea(parent);
	}

	@Override
	public boolean close() {
		if (getReturnCode() != CANCEL) {
			Preferences.set(IProgressConstants.RUN_IN_BACKGROUND, showDialog);
		}

		return super.close();
	}

	protected Display getDisplay() {
	    return Services.getInstance().getDisplay();
    }
}
