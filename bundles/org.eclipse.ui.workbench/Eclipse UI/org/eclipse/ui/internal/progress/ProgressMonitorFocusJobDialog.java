/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.RectangleAnimation;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.WorkbenchJob;
/**
 * The ProgressMonitorFocusJobDialog is a dialog that shows progress for a
 * particular job in a modal dialog so as to give a user accustomed to a modal
 * UI a more famiiar feel.
 */
class ProgressMonitorFocusJobDialog extends ProgressMonitorJobsDialog {
	Job job;
	/**
	 * Create a new instance of the receiver with progress reported on the job.
	 * 
	 * @param parent
	 *            The shell this is parented from.
	 * @param jobToWatch
	 *            The job whose progress will be watched.
	 */
	public ProgressMonitorFocusJobDialog(Shell parent) {
		super(parent);
		setCancelable(true);
	}
	
	/**
	 * Set the job in the receiver and open the dialog. If the job
	 * has not started wait until it has. If not open immediately.
	 * @param jobToWatch
	 * @param running If true the dialog is opened right away
	 */
	public void setJobAndOpen(Job jobToWatch, boolean running) {
		job = jobToWatch;
		addListenerToClose();
		if (running)
			setProgressAndOpen();
		else
			addListenerToOpen();
	}
	/**
	 * Schedule the opening of the dialog.
	 */
	private void addListenerToOpen() {
		job.addJobChangeListener(new JobChangeAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void aboutToRun(IJobChangeEvent event) {
				WorkbenchJob openJob = new WorkbenchJob(
						ProgressMessages
								.getString("ProgressMonitorFocusJobDialog.OpenDialogJob")) { //$NON-NLS-1$
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
					 */
					public IStatus runInUIThread(IProgressMonitor monitor) {
						Display currentDisplay = getDisplay();
						if (currentDisplay == null
								|| currentDisplay.isDisposed())
							return Status.CANCEL_STATUS;
						//If there is a modal shell open then wait
						Shell[] shells = currentDisplay.getShells();
						int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL
								| SWT.PRIMARY_MODAL;
						for (int i = 0; i < shells.length; i++) {
							//Do not stop for shells that will not block the
							// user.
							if (shells[i].isVisible()) {
								int style = shells[i].getStyle();
								if ((style & modal) != 0) {
									//try again in a few seconds
									schedule(1000);
									return Status.CANCEL_STATUS;
								}
							}
						}
						create();
						setProgressAndOpen();
						return Status.OK_STATUS;
					}
				};
				openJob.setSystem(true);
				openJob.schedule(100);
			}
		});
	}
	/**
	 * Schedule the closing of the dialog.
	 */
	private void addListenerToClose() {
		job.addJobChangeListener(new JobChangeAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				if(!PlatformUI.isWorkbenchRunning())
					return;
				
				WorkbenchJob closeJob = new WorkbenchJob(
						ProgressMessages
								.getString("ProgressMonitorFocusJobDialog.CLoseDialogJob")) { //$NON-NLS-1$
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
					 */
					public IStatus runInUIThread(IProgressMonitor monitor) {
						Shell currentShell = getShell();
						if (currentShell == null || currentShell.isDisposed())
							return Status.CANCEL_STATUS;
						close();
						return Status.OK_STATUS;
					}
				};
				closeJob.setSystem(true);
				closeJob.schedule();
			}
		});
	}
	/**
	 * Return the ProgressMonitorWithBlocking for the receiver.
	 * 
	 * @return
	 */
	private IProgressMonitorWithBlocking getBlockingProgressMonitor() {
		return new IProgressMonitorWithBlocking() {
			/**
			 * Run the runnable as an asyncExec.
			 * 
			 * @param runnable
			 */
			private void runAsync(Runnable runnable) {
				Shell currentShell = getShell();
				if (currentShell == null || currentShell.isDisposed())
					return;
				currentShell.getDisplay().asyncExec(runnable);
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
			 *      int)
			 */
			public void beginTask(String name, int totalWork) {
				final String finalName = name;
				final int finalWork = totalWork;
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						getProgressMonitor().beginTask(finalName, finalWork);
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
			 */
			public void clearBlocked() {
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						((IProgressMonitorWithBlocking) getProgressMonitor())
								.clearBlocked();
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#done()
			 */
			public void done() {
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						getProgressMonitor().done();
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
			 */
			public void internalWorked(double work) {
				final double finalWork = work;
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						getProgressMonitor().internalWorked(finalWork);
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
			 */
			public boolean isCanceled() {
				return getProgressMonitor().isCanceled();
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
			 */
			public void setBlocked(IStatus reason) {
				final IStatus finalReason = reason;
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						((IProgressMonitorWithBlocking) getProgressMonitor())
								.setBlocked(finalReason);
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
			 */
			public void setCanceled(boolean value) {
				// Just a listener - doesn't matter.
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
			 */
			public void setTaskName(String name) {
				final String finalName = name;
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						getProgressMonitor().setTaskName(finalName);
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
			 */
			public void subTask(String name) {
				final String finalName = name;
				runAsync(new Runnable() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.lang.Runnable#run()
					 */
					public void run() {
						getProgressMonitor().subTask(finalName);
					}
				});
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
			 */
			public void worked(int work) {
				internalWorked(work);
			}
		};
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#cancelPressed()
	 */
	protected void cancelPressed() {
		job.cancel();
		super.cancelPressed();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		Button runInWorkspace = createButton(
				parent,
				IDialogConstants.CLOSE_ID,
				ProgressMessages
						.getString("ProgressMonitorFocusJobDialog.RunInBackgroundButton"), //$NON-NLS-1$
				true);
		runInWorkspace.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				Rectangle shellPosition = getShell().getBounds();
				close();
				animateClose(shellPosition);
			}
		});
		runInWorkspace.setCursor(arrowCursor);
		createCancelButton(parent);
		createDetailsButton(parent);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.ProgressMonitorDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(job.getName());
	}
	/**
	 * Animate the closing of the dialog.
	 * 
	 * @param startPosition
	 */
	private void animateClose(Rectangle startPosition) {
		IWorkbenchWindow currentWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (currentWindow == null)
			return;
		WorkbenchWindow internalWindow = (WorkbenchWindow) currentWindow;
		Rectangle end = internalWindow.getProgressRegion().getControl()
				.getBounds();
		Point start = internalWindow.getShell().getLocation();
		end.x += start.x;
		end.y += start.y;
		RectangleAnimation animation = new RectangleAnimation(internalWindow
				.getShell(), startPosition, end, 250);
		animation.schedule();
	}
	/**
	 * Set the progress up for the job by adding it as a listener
	 */
	private void setProgressAndOpen() {
		ProgressManager.getInstance().progressFor(job).addProgressListener(
				getBlockingProgressMonitor());
		open();
	}
}