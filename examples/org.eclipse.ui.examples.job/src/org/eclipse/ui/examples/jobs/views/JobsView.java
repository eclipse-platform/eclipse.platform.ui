package org.eclipse.ui.examples.jobs.views;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.examples.jobs.TestJob;
import org.eclipse.ui.examples.jobs.UITestJob;
import org.eclipse.ui.part.ViewPart;

/**
 * A view that allows a user to create jobs of various types, and interact with
 * and test other job-related APIs.
 */
public class JobsView extends ViewPart {
	private Combo durationField;
	private Button lockField, failureField, threadField, systemField, userField, groupField, unknownField;
	private Text quantityField, delayField;

	protected void busyCursorWhile() {
		try {
			final long duration = getDuration();
			final boolean shouldLock = lockField.getSelection();
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException {
					if (shouldLock)
						doRunInWorkspace(duration, monitor);
					else
						doRun(duration, monitor);
				}

			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void createJobs() {
		int jobCount = Integer.parseInt(quantityField.getText());
		boolean ui = threadField.getSelection();
		long duration = getDuration();
		boolean lock = lockField.getSelection();
		boolean failure = failureField.getSelection();
		boolean system = systemField.getSelection();
		boolean useGroup = groupField.getSelection();
		boolean unknown = unknownField.getSelection();
		boolean user = userField.getSelection();

		int groupIncrement = IProgressMonitor.UNKNOWN;
		IProgressMonitor group = new NullProgressMonitor();
		int total = IProgressMonitor.UNKNOWN;

		if (jobCount > 1) {
			total = 100;
			groupIncrement = 100 / jobCount;
		}

		if (useGroup) {

			group = Platform.getJobManager().createProgressGroup();
			group.beginTask("Group", total); //$NON-NLS-1$
		}

		long delay = Integer.parseInt(delayField.getText());
		for (int i = 0; i < jobCount; i++) {
			Job result;
			if (ui)
				result = new UITestJob(duration, lock, failure, unknown);
			else
				result = new TestJob(duration, lock, failure, unknown);

			result.setProgressGroup(group, groupIncrement);
			result.setSystem(system);
			result.setUser(user);
			result.schedule(delay);
		}

	}

	/**
	 * @see ViewPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		Composite body = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		body.setLayout(layout);

		//duration
		Label label = new Label(body, SWT.NONE);
		label.setText("Duration:"); //$NON-NLS-1$
		durationField = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		durationField.setLayoutData(data);
		durationField.add("1 second"); //$NON-NLS-1$
		durationField.add("10 seconds"); //$NON-NLS-1$
		durationField.add("1 minute"); //$NON-NLS-1$
		durationField.add("10 minutes"); //$NON-NLS-1$
		durationField.select(2);

		//delay
		label = new Label(body, SWT.NONE);
		label.setText("Start delay (ms):"); //$NON-NLS-1$
		delayField = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		delayField.setLayoutData(data);
		delayField.setText("0"); //$NON-NLS-1$

		//quantity
		label = new Label(body, SWT.NONE);
		label.setText("Quantity:"); //$NON-NLS-1$
		quantityField = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		quantityField.setLayoutData(data);
		quantityField.setText("1"); //$NON-NLS-1$

		//lock
		lockField = new Button(body, SWT.CHECK);
		lockField.setText("Lock the workspace"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		lockField.setLayoutData(data);

		//system
		systemField = new Button(body, SWT.CHECK);
		systemField.setText("System job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		systemField.setLayoutData(data);

		//thread
		threadField = new Button(body, SWT.CHECK);
		threadField.setText("Run in UI thread"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		threadField.setLayoutData(data);

		//failure
		failureField = new Button(body, SWT.CHECK);
		failureField.setText("Fail"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		failureField.setLayoutData(data);

		//groups
		groupField = new Button(body, SWT.CHECK);
		groupField.setText("Run in Group"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		groupField.setLayoutData(data);

		//IProgressMonitor.UNKNOWN
		unknownField = new Button(body, SWT.CHECK);
		unknownField.setText("Indeterminate Progress"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		unknownField.setLayoutData(data);

		//whether the job is a user job
		userField = new Button(body, SWT.CHECK);
		userField.setText("User job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		userField.setLayoutData(data);

		//create jobs
		Button create = new Button(body, SWT.PUSH);
		create.setText("Create jobs"); //$NON-NLS-1$
		create.setToolTipText("Creates and schedules jobs according to above parameters"); //$NON-NLS-1$
		create.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		create.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createJobs();
			}
		});

		//touch workspace
		Button touch = new Button(body, SWT.PUSH);
		touch.setText("Touch workspace"); //$NON-NLS-1$
		touch.setToolTipText("Modifies the workspace in the UI thread"); //$NON-NLS-1$
		touch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		touch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				touchWorkspace();
			}
		});
		//busy cursor while
		Button busyWhile = new Button(body, SWT.PUSH);
		busyWhile.setText("Progress Service"); //$NON-NLS-1$
		busyWhile.setToolTipText("Uses IProgressService.busyCursorWhile"); //$NON-NLS-1$
		busyWhile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		busyWhile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				busyCursorWhile();
			}
		});
		//progress monitor dialog with fork=false
		Button noFork = new Button(body, SWT.PUSH);
		noFork.setText("Dialog (fork==false"); //$NON-NLS-1$
		noFork.setToolTipText("ProgressMonitorDialog.run(fork==false)"); //$NON-NLS-1$
		noFork.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		noFork.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				progressNoFork();
			}
		});

		//progress monitor dialog with fork=false
		Button exception = new Button(body, SWT.PUSH);
		exception.setText("Runtime Exception"); //$NON-NLS-1$
		exception.setToolTipText("NullPointerException when running"); //$NON-NLS-1$
		exception.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exception.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				jobWithRuntimeException();
			}
		});

		//join the running test jobs
		Button join = new Button(body, SWT.PUSH);
		join.setText("Join Test Jobs"); //$NON-NLS-1$
		join.setToolTipText("IJobManager.join() on test jobs"); //$NON-NLS-1$
		join.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		join.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				joinTestJobs();
			}
		});

	}

	protected void doRun(long duration, IProgressMonitor monitor) {
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		monitor.beginTask("Spinning inside IProgressService.busyCursorWhile", ticks); //$NON-NLS-1$
		monitor.setTaskName("Spinning inside IProgressService.busyCursorWhile"); //$NON-NLS-1$
		for (int i = 0; i < ticks; i++) {
			monitor.subTask("Processing tick #" + i); //$NON-NLS-1$
			if (monitor.isCanceled())
				return;
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				//ignore
			}
			monitor.worked(1);
		}
	}

	protected void doRunInWorkspace(final long duration, IProgressMonitor monitor) {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doRun(duration, monitor);
				}
			}, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected long getDuration() {
		switch (durationField.getSelectionIndex()) {
			case 0 :
				return 1000;
			case 1 :
				return 10000;
			case 2 :
				return 60000;
			case 3 :
			default :
				return 600000;
		}
	}

	protected void jobWithRuntimeException() {
		Job runtimeExceptionJob = new Job("Job with Runtime exception") { //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				throw new NullPointerException();
			}
		};
		runtimeExceptionJob.schedule();
	}

	/**
	 * Example usage of the IJobManager.join method.
	 */
	protected void joinTestJobs() {
		try {
			//note that when a null progress monitor is used when in the UI
			//thread, the workbench will create a default progress monitor
			//that reports progress in a modal dialog with details area
			Platform.getJobManager().join(TestJob.FAMILY_TEST_JOB, null);
		} catch (OperationCanceledException e) {
			//thrown if the user interrupts the join by canceling the progress monitor
			//A UI component should swallow the exception and finish the action
			//or operation. A lower level component should just propagate the
			//exception
			e.printStackTrace();
		} catch (InterruptedException e) {
			// Thrown if Thread.interrupt is called on this thread
			// This can either be ignored (repeat the join attempt until success), or propagated
			e.printStackTrace();
		}
	}

	protected void progressNoFork() {
		try {
			final long duration = getDuration();
			final boolean shouldLock = lockField.getSelection();
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(getViewSite().getShell());
			dialog.run(false, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException {
					if (shouldLock)
						doRunInWorkspace(duration, monitor);
					else
						doRun(duration, monitor);
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see ViewPart#setFocus()
	 */
	public void setFocus() {
		if (durationField != null && !durationField.isDisposed())
			durationField.setFocus();
	}

	protected void touchWorkspace() {
		//create an asyncExec to touch the workspace the specific number of times
		int jobCount = Integer.parseInt(quantityField.getText());
		for (int i = 0; i < jobCount; i++) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) {
								//no-op
							}
						}, null);
					} catch (OperationCanceledException e) {
						//ignore
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}