package org.eclipse.ui.examples.jobs.views;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.examples.jobs.TestJob;
import org.eclipse.ui.examples.jobs.UITestJob;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

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
		layout.makeColumnsEqualWidth = false;
		body.setLayout(layout);

		createEntryFieldGroup(body);
		createPushButtonGroup(body);
		createCheckboxGroup(body);
	}

	/**
	 * Create all push button parts for the jobs view.
	 * @param parent
	 */
	private void createPushButtonGroup(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		//create jobs
		Button create = new Button(group, SWT.PUSH);
		create.setText("Create jobs"); //$NON-NLS-1$
		create.setToolTipText("Creates and schedules jobs according to above parameters"); //$NON-NLS-1$
		create.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		create.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createJobs();
			}
		});

		//touch workspace
		Button touch = new Button(group, SWT.PUSH);
		touch.setText("Touch workspace"); //$NON-NLS-1$
		touch.setToolTipText("Modifies the workspace in the UI thread"); //$NON-NLS-1$
		touch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		touch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				touchWorkspace();
			}
		});
		//busy cursor while
		Button busyWhile = new Button(group, SWT.PUSH);
		busyWhile.setText("busyCursorWhile"); //$NON-NLS-1$
		busyWhile.setToolTipText("Uses IProgressService.busyCursorWhile"); //$NON-NLS-1$
		busyWhile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		busyWhile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				busyCursorWhile();
			}
		});
		//progress monitor dialog with fork=false
		Button noFork = new Button(group, SWT.PUSH);
		noFork.setText("runInUI"); //$NON-NLS-1$
		noFork.setToolTipText("Uses IProgressService.runInUI"); //$NON-NLS-1$
		noFork.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		noFork.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				progressNoFork();
			}
		});

		//progress monitor dialog with fork=false
		Button exception = new Button(group, SWT.PUSH);
		exception.setText("Runtime Exception"); //$NON-NLS-1$
		exception.setToolTipText("NullPointerException when running"); //$NON-NLS-1$
		exception.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exception.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				jobWithRuntimeException();
			}
		});

		//join the running test jobs
		Button join = new Button(group, SWT.PUSH);
		join.setText("Join Test Jobs"); //$NON-NLS-1$
		join.setToolTipText("IJobManager.join() on test jobs"); //$NON-NLS-1$
		join.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		join.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				joinTestJobs();
			}
		});

		//join the running test jobs
		Button window = new Button(group, SWT.PUSH);
		window.setText("Runnable in Window"); //$NON-NLS-1$
		window.setToolTipText("Using a runnable context in the workbench window"); //$NON-NLS-1$
		window.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		window.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runnableInWindow();
			}
		});

		//join the running test jobs
		Button sleep = new Button(group, SWT.PUSH);
		sleep.setText("Sleep"); //$NON-NLS-1$
		sleep.setToolTipText("Calls sleep() on all TestJobs"); //$NON-NLS-1$
		sleep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sleep.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSleep();
			}
		});

		//join the running test jobs
		Button wake = new Button(group, SWT.PUSH);
		wake.setText("WakeUp"); //$NON-NLS-1$
		wake.setToolTipText("Using a runnable context in the workbench window"); //$NON-NLS-1$
		wake.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		wake.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doWakeUp();
			}
		});
	}
	/**
	 * Wakes up all sleeping test jobs.
	 */
	protected void doWakeUp() {
		Platform.getJobManager().wakeUp(TestJob.FAMILY_TEST_JOB);
	}
	/**
	 * Puts to sleep all waiting test jobs.
	 */
	protected void doSleep() {
		Platform.getJobManager().sleep(TestJob.FAMILY_TEST_JOB);
	}

	/**
	 * @param body
	 */
	private void createEntryFieldGroup(Composite body) {
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

	}

	/**
	 * Creates all of the checkbox buttons.
	 */
	private void createCheckboxGroup(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		//lock
		lockField = new Button(group, SWT.CHECK);
		lockField.setText("Lock the workspace"); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		lockField.setLayoutData(data);

		//system
		systemField = new Button(group, SWT.CHECK);
		systemField.setText("System job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		systemField.setLayoutData(data);

		//thread
		threadField = new Button(group, SWT.CHECK);
		threadField.setText("Run in UI thread"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		threadField.setLayoutData(data);

		//failure
		failureField = new Button(group, SWT.CHECK);
		failureField.setText("Fail"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		failureField.setLayoutData(data);

		//groups
		groupField = new Button(group, SWT.CHECK);
		groupField.setText("Run in Group"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		groupField.setLayoutData(data);

		//IProgressMonitor.UNKNOWN
		unknownField = new Button(group, SWT.CHECK);
		unknownField.setText("Indeterminate Progress"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		unknownField.setLayoutData(data);

		//whether the job is a user job
		userField = new Button(group, SWT.CHECK);
		userField.setText("User job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		userField.setLayoutData(data);
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
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			progressService.runInUI(progressService, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException {
					if (shouldLock)
						doRunInWorkspace(duration, monitor);
					else
						doRun(duration, monitor);
				}
			}, ResourcesPlugin.getWorkspace().getRoot());
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

	/**
	 * Run a workspace runnable in the application window.
	 *
	 */

	public void runnableInWindow() {

		final long time = getDuration();
		final long sleep = 10;
		IRunnableWithProgress runnableTest = new WorkspaceModifyOperation() {

			/* (non-Javadoc)
			 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				int ticks = (int) (time / sleep);
				monitor.beginTask("Spinning inside ApplicationWindow.run()", ticks); //$NON-NLS-1$
				monitor.setTaskName("Spinning inside ApplicationWindow.run()"); //$NON-NLS-1$
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

		};
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, runnableTest);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}