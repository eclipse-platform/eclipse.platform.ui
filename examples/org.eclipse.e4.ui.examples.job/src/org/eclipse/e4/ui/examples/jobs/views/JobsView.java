/*******************************************************************************
 * Copyright (c) 2014, 2019 Wojciech Sudol and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wojciech Sudol - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.jobs.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.examples.jobs.TestJob;
import org.eclipse.e4.ui.examples.jobs.TestJobRule;
import org.eclipse.e4.ui.examples.jobs.UITestJob;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.IProgressService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * A view that allows a user to create jobs of various types, and interact with
 * and test other job-related APIs.
 */
public class JobsView {
	private Combo durationField;
	private Button lockField, failureField, threadField, systemField,
			userField, groupField, rescheduleField, keepField, keepOneField,
			unknownField, gotoActionField;
	private Text quantityField, delayField, rescheduleDelay;
	private Button schedulingRuleField;
	private Button noPromptField;

	Composite parent;

	@Inject
	@Optional
	IProgressService progressService;

	@Inject
	@Optional
	@Active
	Shell shell;


	protected void busyCursorWhile() {
		try {
			final long duration = getDuration();
			final boolean shouldLock = lockField.getSelection();
			progressService.busyCursorWhile(
					monitor -> {
						if (shouldLock)
							doRunInWorkspace(duration, monitor);
						else
							doRun(duration, monitor);
					});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// ignore - interrupt means cancel in this context
		}
	}

	protected void createJobs() {
		int jobCount = Integer.parseInt(quantityField.getText());
		boolean ui = threadField.getSelection();
		long duration = getDuration();
		boolean lock = lockField.getSelection();
		boolean failure = failureField.getSelection();
		boolean noPrompt = noPromptField.getSelection();
		boolean system = systemField.getSelection();
		boolean useGroup = groupField.getSelection();
		boolean unknown = unknownField.getSelection();
		boolean user = userField.getSelection();
		boolean reschedule = rescheduleField.getSelection();
		final long rescheduleWait = Long.parseLong(rescheduleDelay.getText());
		boolean keep = keepField.getSelection();
		boolean keepOne = keepOneField.getSelection();
		boolean gotoAction = gotoActionField.getSelection();
		boolean schedulingRule = schedulingRuleField.getSelection();

		int groupIncrement = IProgressMonitor.UNKNOWN;
		IProgressMonitor group = new NullProgressMonitor();
		int total = IProgressMonitor.UNKNOWN;

		if (jobCount > 1) {
			total = 100;
			groupIncrement = 100 / jobCount;
		}

		if (useGroup) {
			group = Job.getJobManager().createProgressGroup();
			group.beginTask("Group", total); //$NON-NLS-1$
		}

		long delay = Integer.parseInt(delayField.getText());
		for (int i = 0; i < jobCount; i++) {
			Job result;
			if (ui)
				result = new UITestJob(duration, lock, failure, unknown);
			else
				result = new TestJob(duration, lock, failure, unknown,
						reschedule, rescheduleWait);

			result.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean
					.valueOf(keep));
			result.setProperty(IProgressConstants.KEEPONE_PROPERTY, Boolean
					.valueOf(keepOne));
			result.setProperty(
					IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
					Boolean.valueOf(noPrompt));
			if (gotoAction)
				result.setProperty(IProgressConstants.ACTION_PROPERTY,
						new Action("Pop up a dialog") { //$NON-NLS-1$
							@Override
							public void run() {
								MessageDialog
										.openInformation(
												parent.getShell(),
												"Goto Action", "The job can have an action associated with it"); //$NON-NLS-1$ //$NON-NLS-2$
							}
						});

			result.setProgressGroup(group, groupIncrement);
			result.setSystem(system);
			result.setUser(user);

			if (schedulingRule)
				result.setRule(new TestJobRule(i));
			result.schedule(delay);
		}
	}

	@PostConstruct
	public void createPartControl(Composite parent) {
		this.parent = parent;
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
	 */
	private void createPushButtonGroup(Composite parent) {
		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create jobs
		Button create = new Button(group, SWT.PUSH);
		create.setText("Create jobs"); //$NON-NLS-1$
		create
				.setToolTipText("Creates and schedules jobs according to above parameters"); //$NON-NLS-1$
		create.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		create.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createJobs();
			}
		});

		// busy cursor while
		Button busyWhile = new Button(group, SWT.PUSH);
		busyWhile.setText("busyCursorWhile"); //$NON-NLS-1$
		busyWhile.setToolTipText("Uses IProgressService.busyCursorWhile"); //$NON-NLS-1$
		busyWhile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		busyWhile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				busyCursorWhile();
			}
		});
		// progress monitor dialog with fork=false
		Button noFork = new Button(group, SWT.PUSH);
		noFork.setText("runInUI"); //$NON-NLS-1$
		noFork.setToolTipText("Uses IProgressService.runInUI"); //$NON-NLS-1$
		noFork.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		noFork.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				progressNoFork();
			}
		});

		// progress monitor dialog with fork=false
		Button exception = new Button(group, SWT.PUSH);
		exception.setText("Runtime Exception"); //$NON-NLS-1$
		exception.setToolTipText("NullPointerException when running"); //$NON-NLS-1$
		exception.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exception.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				jobWithRuntimeException();
			}
		});

		// join the running test jobs
		Button join = new Button(group, SWT.PUSH);
		join.setText("Join Test Jobs"); //$NON-NLS-1$
		join.setToolTipText("IJobManager.join() on test jobs"); //$NON-NLS-1$
		join.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		join.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				joinTestJobs();
			}
		});

		// join the running test jobs
		Button sleep = new Button(group, SWT.PUSH);
		sleep.setText("Sleep"); //$NON-NLS-1$
		sleep.setToolTipText("Calls sleep() on all TestJobs"); //$NON-NLS-1$
		sleep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sleep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSleep();
			}
		});

		// join the running test jobs
		Button wake = new Button(group, SWT.PUSH);
		wake.setText("WakeUp"); //$NON-NLS-1$
		wake.setToolTipText("Calls wakeUp() on all TestJobs"); //$NON-NLS-1$
		wake.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		wake.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				doWakeUp();
			}
		});

		// show in dialog
		Button showInDialog = new Button(group, SWT.PUSH);
		showInDialog.setText("showInDialog"); //$NON-NLS-1$
		showInDialog.setToolTipText("Uses IProgressService.showInDialog"); //$NON-NLS-1$
		showInDialog.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showInDialog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showInDialog();
			}
		});

	}

	/**
	 * Test the showInDialog API
	 */
	protected void showInDialog() {

		Job showJob = new Job("Show In Dialog") {//$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, "Run in dialog", 100);//$NON-NLS-1$

				for (int i = 0; i < 100; i++) {
					subMonitor.split(1);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						return Status.CANCEL_STATUS;
					}

				}
				return Status.OK_STATUS;

			}
		};
		showJob.schedule();
		progressService.showInDialog(shell, showJob);

	}

	/**
	 * Wakes up all sleeping test jobs.
	 */
	protected void doWakeUp() {
		Job.getJobManager().wakeUp(TestJob.FAMILY_TEST_JOB);
	}

	/**
	 * Puts to sleep all waiting test jobs.
	 */
	protected void doSleep() {
		Job.getJobManager().sleep(TestJob.FAMILY_TEST_JOB);
	}

	private void createEntryFieldGroup(Composite body) {
		// duration
		Label label = new Label(body, SWT.NONE);
		label.setText("Duration:"); //$NON-NLS-1$
		durationField = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		durationField.setLayoutData(data);
		durationField.add("0"); //$NON-NLS-1$
		durationField.add("1 millisecond"); //$NON-NLS-1$
		durationField.add("1 second"); //$NON-NLS-1$
		durationField.add("10 seconds"); //$NON-NLS-1$
		durationField.add("1 minute"); //$NON-NLS-1$
		durationField.add("10 minutes"); //$NON-NLS-1$
		durationField.select(4);

		// delay
		label = new Label(body, SWT.NONE);
		label.setText("Start delay (ms):"); //$NON-NLS-1$
		delayField = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		delayField.setLayoutData(data);
		delayField.setText("0"); //$NON-NLS-1$

		// quantity
		label = new Label(body, SWT.NONE);
		label.setText("Quantity:"); //$NON-NLS-1$
		quantityField = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		quantityField.setLayoutData(data);
		quantityField.setText("1"); //$NON-NLS-1$

		// reschedule delay
		label = new Label(body, SWT.NONE);
		label.setText("Reschedule Delay (ms):"); //$NON-NLS-1$
		rescheduleDelay = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		rescheduleDelay.setLayoutData(data);
		rescheduleDelay.setText("1000"); //$NON-NLS-1$
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

		// lock
		lockField = new Button(group, SWT.CHECK);
		lockField.setText("Lock the workspace"); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		lockField.setLayoutData(data);

		// system
		systemField = new Button(group, SWT.CHECK);
		systemField.setText("System job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		systemField.setLayoutData(data);

		// thread
		threadField = new Button(group, SWT.CHECK);
		threadField.setText("Run in UI thread"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		threadField.setLayoutData(data);

		// groups
		groupField = new Button(group, SWT.CHECK);
		groupField.setText("Run in Group"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		groupField.setLayoutData(data);

		// reschedule
		rescheduleField = new Button(group, SWT.CHECK);
		rescheduleField.setText("Reschedule"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		rescheduleField.setLayoutData(data);

		// keep
		keepField = new Button(group, SWT.CHECK);
		keepField.setText("Keep"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		keepField.setLayoutData(data);

		// keep one
		keepOneField = new Button(group, SWT.CHECK);
		keepOneField.setText("KeepOne"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		keepOneField.setLayoutData(data);

		// IProgressMonitor.UNKNOWN
		unknownField = new Button(group, SWT.CHECK);
		unknownField.setText("Indeterminate Progress"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		unknownField.setLayoutData(data);

		// whether the job is a user job
		userField = new Button(group, SWT.CHECK);
		userField.setText("User job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		userField.setLayoutData(data);

		// whether the job has a goto action
		gotoActionField = new Button(group, SWT.CHECK);
		gotoActionField.setText("Goto action"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		gotoActionField.setLayoutData(data);

		// whether the job should use a scheduling rule
		schedulingRuleField = new Button(group, SWT.CHECK);
		schedulingRuleField.setText("Schedule sequentially"); //$NON-NLS-1$
		schedulingRuleField
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// failure
		failureField = new Button(group, SWT.CHECK);
		failureField.setText("Fail"); //$NON-NLS-1$
		failureField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// failure
		noPromptField = new Button(group, SWT.CHECK);
		noPromptField.setText("No Prompt"); //$NON-NLS-1$
		noPromptField
				.setToolTipText("Set the IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY to true");
		noPromptField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void doRun(long duration, IProgressMonitor monitor) {
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				"Spinning inside IProgressService.busyCursorWhile", ticks); //$NON-NLS-1$
		subMonitor.setTaskName("Spinning inside IProgressService.busyCursorWhile"); //$NON-NLS-1$
		for (int i = 0; i < ticks; i++) {
			subMonitor.subTask("Processing tick #" + i); //$NON-NLS-1$
			subMonitor.split(1);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	protected void doRunInWorkspace(final long duration,
			IProgressMonitor monitor) {
		try {
			ResourcesPlugin.getWorkspace().run((IWorkspaceRunnable) monitor1 -> doRun(duration, monitor1), monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	protected long getDuration() {
		switch (durationField.getSelectionIndex()) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 1000;
		case 3:
			return 10000;
		case 4:
			return 60000;
		case 5:
		default:
			return 600000;
		}
	}

	protected void jobWithRuntimeException() {
		Job runtimeExceptionJob = new Job("Job with Runtime exception") { //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 *
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
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
			// note that when a null progress monitor is used when in the UI
			// thread, the workbench will create a default progress monitor
			// that reports progress in a modal dialog with details area
			progressService.busyCursorWhile(
					monitor -> Job.getJobManager().join(TestJob.FAMILY_TEST_JOB,
							monitor));
		} catch (InterruptedException | InvocationTargetException e) {
			// Thrown when the operation running within busyCursorWhile throws
			// an
			// exception. This should either be propagated or displayed to the
			// user
			e.printStackTrace();
		}
	}

	protected void progressNoFork() {
		try {
			final long duration = getDuration();
			final boolean shouldLock = lockField.getSelection();
			progressService.runInUI(progressService,
					monitor -> {
if (shouldLock)
					doRunInWorkspace(duration, monitor);
else
					doRun(duration, monitor);
}, ResourcesPlugin.getWorkspace().getRoot());
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Focus
	public void setFocus() {
		if (durationField != null && !durationField.isDisposed())
			durationField.setFocus();
	}

}