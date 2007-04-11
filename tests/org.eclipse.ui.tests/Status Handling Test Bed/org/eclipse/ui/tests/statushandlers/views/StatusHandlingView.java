/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.RegistryReader;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.tests.statushandlers.jobs.TestJob;
import org.eclipse.ui.tests.statushandlers.jobs.UITestJob;

/**
 * A view dedicated to generating status (error.
 */
public class StatusHandlingView extends ViewPart {

	private Combo durationField, exceptionField, statusSeverityField;

	private Spinner percent;

	private Text delayField, quantityField, rescheduleDelay, statusPluginID;

	private Button progressNoForkLockField, threadField, jobLockField,
			systemField, userField, groupField, rescheduleField,
			returnErrorStatus, showStatusField, logStatusField;

	private TableViewer statusTableViever;

	private ContentProvider contentProvider;

	private List statusItems = Collections.synchronizedList(new ArrayList());

	private int statusNo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		ScrolledComposite scroller = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);

		Composite body = new Composite(scroller, SWT.NONE);
		GridLayout layout = new GridLayout();
		body.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		body.setLayoutData(gd);

		Composite jobsAndRunnables = new Composite(body, SWT.BORDER);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		jobsAndRunnables.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		jobsAndRunnables.setLayoutData(gd);

		createEntryFieldGroup(jobsAndRunnables);
		createJobAndRunnableSpecificGroups(jobsAndRunnables);

		Composite views = new Composite(body, SWT.BORDER);
		layout = new GridLayout();
		views.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		views.setLayoutData(gd);

		createShowViewsGroup(views);

		Composite invalidExtensions = new Composite(body, SWT.BORDER);
		layout = new GridLayout();
		invalidExtensions.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		invalidExtensions.setLayoutData(gd);

		createInvalidExtGroup(invalidExtensions);

		Composite generateStatusToBeShown = new Composite(body, SWT.BORDER);
		layout = new GridLayout();
		generateStatusToBeShown.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		generateStatusToBeShown.setLayoutData(gd);

		createShowStatusToBeShowGroup(generateStatusToBeShown);

		scroller.setContent(body);
		scroller.setMinSize(body.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);

	}

	/**
	 * Creates global entry group.
	 * 
	 * @param body
	 *            parent
	 */
	private void createEntryFieldGroup(Composite body) {
		// duration
		Label label = new Label(body, SWT.NONE);
		label.setText("Duration:"); //$NON-NLS-1$
		durationField = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		durationField.setLayoutData(data);
		durationField.setToolTipText("Duration of the job/runnable");
		durationField.add("0"); //$NON-NLS-1$
		durationField.add("1 millisecond"); //$NON-NLS-1$
		durationField.add("1 second"); //$NON-NLS-1$
		durationField.add("10 seconds"); //$NON-NLS-1$
		durationField.add("1 minute"); //$NON-NLS-1$
		durationField.add("10 minutes"); //$NON-NLS-1$
		durationField.select(3);

		label = new Label(body, SWT.NONE);
		label.setText("Throw exception after (% of duration):"); //$NON-NLS-1$
		percent = new Spinner(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		percent.setLayoutData(data);
		percent
				.setToolTipText("Exception/Error will be thrown after percent*duration");
		percent.setMinimum(1);
		percent.setMaximum(100);
		percent.setSelection(50);

		label = new Label(body, SWT.NONE);
		label.setText("Exception:"); //$NON-NLS-1$
		exceptionField = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		exceptionField.setLayoutData(data);
		exceptionField
				.setToolTipText("Exception/Error to be thrown from the job/runnable");
		exceptionField.add("NullPointerException"); //$NON-NLS-1$
		exceptionField.add("OutOfMemoryError"); //$NON-NLS-1$
		exceptionField.add("IndexOutOfBoundsException");//$NON-NLS-1$
		exceptionField.select(0);

	}

	/**
	 * Creates groups for run in UI / run in user thread and run job actions.
	 * 
	 * @param parent
	 */
	private void createJobAndRunnableSpecificGroups(Composite parent) {

		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);

		createRunInUIGroup(group);
		createRunInUserInterfaceThread(group);
		createRunJob(group);

	}

	private void createRunInUIGroup(Composite parent) {
		Composite group = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button noFork = new Button(group, SWT.PUSH);
		noFork.setText("Run In UI thread (does not fork)"); //$NON-NLS-1$
		noFork.setToolTipText("Use IProgressService.runInUI(...)"); //$NON-NLS-1$
		noFork.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		noFork.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				progressNoFork();
			}
		});

		progressNoForkLockField = new Button(group, SWT.CHECK);
		progressNoForkLockField
				.setText("Lock the workspace (run in workspace)"); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		progressNoForkLockField.setLayoutData(data);
		progressNoForkLockField
				.setToolTipText("Use ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable(){...}) inside the IProgressService.runInUI(...)");

	}

	private void createRunInUserInterfaceThread(Composite parent) {

		Composite group = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button window = new Button(group, SWT.PUSH);
		window.setText("Runnable in Window"); //$NON-NLS-1$
		window
				.setToolTipText("Use PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, new IRunnableWithProgress(){...})"); //$NON-NLS-1$
		window.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		window.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runnableInWindow();
			}
		});
	}

	/**
	 * @param parent
	 */
	private void createRunJob(Composite parent) {
		Composite group = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(group, SWT.NONE);
		label.setText("Start delay (ms):"); //$NON-NLS-1$
		delayField = new Text(group, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		delayField.setLayoutData(data);
		delayField.setToolTipText("Delay to start the job");
		delayField.setText("0"); //$NON-NLS-1$

		label = new Label(group, SWT.NONE);
		label.setText("Quantity:"); //$NON-NLS-1$
		quantityField = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		quantityField.setLayoutData(data);
		quantityField.setToolTipText("How many jobs ought to be started ?");
		quantityField.setText("1"); //$NON-NLS-1$

		// reschedule delay
		label = new Label(group, SWT.NONE);
		label.setText("Reschedule Delay (ms):"); //$NON-NLS-1$
		rescheduleDelay = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		rescheduleDelay.setLayoutData(data);
		rescheduleDelay
				.setToolTipText("Reschedule after ... (see the reschedule checkbox)");
		rescheduleDelay.setText("1000"); //$NON-NLS-1$

		// thread
		threadField = new Button(group, SWT.CHECK);
		threadField.setText("Run in UI thread"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		threadField.setLayoutData(data);
		threadField
				.setToolTipText("Use org.eclipse.ui.progress.UIJob (instead of org.eclipse.core.runtime.jobs.Job)");

		// lock
		jobLockField = new Button(group, SWT.CHECK);
		jobLockField.setText("Lock the workspace"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		jobLockField.setLayoutData(data);
		jobLockField
				.setToolTipText("Reserve exclusive access to workspace - setRule(ResourcesPlugin.getWorkspace().getRoot())");

		// system
		systemField = new Button(group, SWT.CHECK);
		systemField.setText("System job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		systemField.setLayoutData(data);
		systemField
				.setToolTipText("Decide whether the job is a system job /is presented on UI/");

		// whether the job is a user job
		userField = new Button(group, SWT.CHECK);
		userField.setText("User job"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		userField.setLayoutData(data);
		userField
				.setToolTipText("Decide whether the job is initiated directly by the UI end user");

		// groups
		groupField = new Button(group, SWT.CHECK);
		groupField.setText("Run in Group"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		groupField.setLayoutData(data);
		groupField
				.setToolTipText("Run all of the jobs as a single group (if there is more than one job)");

		// reschedule
		rescheduleField = new Button(group, SWT.CHECK);
		rescheduleField.setText("Reschedule"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		rescheduleField.setLayoutData(data);
		rescheduleField
				.setToolTipText("Reschedule the job ('Reschedule Delay' is the period between reschedules)");

		// create jobs
		Button create = new Button(group, SWT.PUSH);
		create.setText("Create jobs"); //$NON-NLS-1$
		create
				.setToolTipText("Creates and schedules jobs according to above parameters"); //$NON-NLS-1$
		create.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		create.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createJobs();
			}
		});

		returnErrorStatus = new Button(group, SWT.CHECK);
		returnErrorStatus
				.setText("Return an exception wrapped in a error status"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		returnErrorStatus.setLayoutData(data);
		returnErrorStatus
				.setToolTipText("Wrap the exception in a MultiStatus instance");

	}

	/**
	 * @param parent
	 */
	private void createShowViewsGroup(Composite parent) {

		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button showPartInitExceptionView = new Button(group, SWT.PUSH);
		showPartInitExceptionView
				.setText("Show view throwing PartInitException"); //$NON-NLS-1$
		showPartInitExceptionView.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		showPartInitExceptionView.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView(
									"org.eclipseinternal.ui.tests.statushandling.PartInitExceptionView");
				} catch (PartInitException e1) {
					// should not be thrown !
					e1.printStackTrace();
				}
			}
		});

		Button showRuntimeExceptionView = new Button(group, SWT.PUSH);
		showRuntimeExceptionView.setText("Show view throwing RuntimeException"); //$NON-NLS-1$
		showRuntimeExceptionView.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		showRuntimeExceptionView.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView(
									"org.eclipseinternal.ui.tests.statushandling.RuntimeExceptionView");
				} catch (PartInitException e1) {
					// should not be thrown !
					e1.printStackTrace();
				}
			}
		});

		Button showRuntimeExceptionEditor = new Button(group, SWT.PUSH);
		showRuntimeExceptionEditor
				.setText("Show editor throwing RuntimeException"); //$NON-NLS-1$
		showRuntimeExceptionEditor.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		showRuntimeExceptionEditor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				try {
					PlatformUI
							.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.openEditor(new IEditorInput() {

								public boolean exists() {

									return false;
								}

								public ImageDescriptor getImageDescriptor() {

									return null;
								}

								public String getName() {

									return null;
								}

								public IPersistableElement getPersistable() {

									return null;
								}

								public String getToolTipText() {

									return null;
								}

								public Object getAdapter(Class adapter) {

									return null;
								}

							},
									"org.eclipseinternal.ui.tests.statushandling.RuntimeExceptionEditor");
				} catch (PartInitException e1) {
					// should not be thrown !
					e1.printStackTrace();
				}
			}
		});

		Button showPartInitExceptionEditor = new Button(group, SWT.PUSH);
		showPartInitExceptionEditor.setText("Show editor throwing PartInit"); //$NON-NLS-1$
		showPartInitExceptionEditor.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		showPartInitExceptionEditor
				.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {

						try {
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.openEditor(
											new IEditorInput() {

												public boolean exists() {

													return false;
												}

												public ImageDescriptor getImageDescriptor() {

													return null;
												}

												public String getName() {

													return null;
												}

												public IPersistableElement getPersistable() {

													return null;
												}

												public String getToolTipText() {

													return null;
												}

												public Object getAdapter(
														Class adapter) {

													return null;
												}

											},
											"org.eclipseinternal.ui.tests.statushandling.PartInitExceptionEditor");
						} catch (PartInitException e1) {
							// should not be thrown !
							e1.printStackTrace();
						}
					}
				});

	}

	/**
	 * Creates a group for invalid extension initialization.
	 * 
	 * @param parent
	 */
	private void createInvalidExtGroup(Composite parent) {

		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button loadInvalidExt = new Button(group, SWT.PUSH);
		loadInvalidExt
				.setText("Extensions - invalid references determined at load time"); //$NON-NLS-1$
		loadInvalidExt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		loadInvalidExt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				SampleRegistryReader reader = new SampleRegistryReader();
				reader.readRegistry(Platform.getExtensionRegistry(),
						"org.eclipse.ui.tests", "sample");
			}
		});
	}

	private void createShowStatusToBeShowGroup(Composite parent) {

		Composite group = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button handleStatuses = new Button(group, SWT.PUSH);
		handleStatuses.setText("Handle statuses (StatusManager)"); //$NON-NLS-1$
		handleStatuses.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		handleStatuses.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				for (Iterator it = statusItems.iterator(); it.hasNext();) {
					DisplayedItem di = (DisplayedItem) it.next();
					// see SampleStatusHandler
					// hints and statuses could be easily modified there
					StatusManager.getManager().handle(di.getStatus(),
							di.getHint());
				}
			}
		});

		statusTableViever = new TableViewer(group, SWT.MULTI | SWT.BORDER
				| SWT.V_SCROLL | SWT.VIRTUAL);
		contentProvider = new ContentProvider();
		statusTableViever.setContentProvider(contentProvider);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		statusTableViever.getTable().setLayoutData(gd);
		statusTableViever.setInput(new Object[0]);

		Button addStatus = new Button(group, SWT.PUSH);
		addStatus.setText("Add status to the list"); //$NON-NLS-1$
		addStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addStatus.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean log = logStatusField.getSelection();
				boolean show = showStatusField.getSelection();

				int hint = StatusManager.NONE;
				if (log && show)
					hint = StatusManager.SHOW | StatusManager.LOG;
				else if (log)
					hint = StatusManager.LOG;
				else if (show)
					hint = StatusManager.SHOW;

				int severity = IStatus.OK;
				switch (statusSeverityField.getSelectionIndex()) {
				case 0:
					severity = IStatus.OK;
					break;
				case 1:
					severity = IStatus.INFO;
					break;
				case 2:
					severity = IStatus.WARNING;
					break;
				case 3:
					severity = IStatus.CANCEL;
					break;
				case 4:
					severity = IStatus.ERROR;
					break;
				}

				statusNo++;

				statusItems.add(new DisplayedItem(new Status(severity,
						statusPluginID.getText(), "A test status No. "
								+ statusNo + " !"), hint));
				statusTableViever.refresh();
			}
		});

		Button removeStatus = new Button(group, SWT.PUSH);
		removeStatus.setText("Remove status from the list"); //$NON-NLS-1$
		removeStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeStatus.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				StructuredSelection sel = (StructuredSelection) statusTableViever
						.getSelection();
				for (Iterator it = sel.iterator(); it.hasNext();) {
					statusItems.remove(it.next());
				}
				statusTableViever.refresh();
			}
		});

		showStatusField = new Button(group, SWT.CHECK);
		showStatusField.setText("Show status (UI dialog)"); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		showStatusField.setLayoutData(data);
		showStatusField
				.setToolTipText("Sets one of the StatusManager hints for showing statuses (SHOW or SHOWANDLOG)!");

		logStatusField = new Button(group, SWT.CHECK);
		logStatusField.setText("Log status"); //$NON-NLS-1$
		data = new GridData(GridData.FILL_HORIZONTAL);
		logStatusField.setLayoutData(data);
		logStatusField
				.setToolTipText("Sets one of the StatusManager hints for logging statuses (LOG or SHOWANDLOG)!");

		// duration
		Label label = new Label(group, SWT.NONE);
		label.setText("Severity level:"); //$NON-NLS-1$
		statusSeverityField = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		statusSeverityField.setLayoutData(data);
		statusSeverityField.setToolTipText("Severity of the generated status");//$NON-NLS-1$
		statusSeverityField.add("OK"); //$NON-NLS-1$
		statusSeverityField.add("INFO"); //$NON-NLS-1$
		statusSeverityField.add("WARNING"); //$NON-NLS-1$
		statusSeverityField.add("CANCEL"); //$NON-NLS-1$
		statusSeverityField.add("ERROR"); //$NON-NLS-1$
		statusSeverityField.select(4);

		Label labelID = new Label(group, SWT.NONE);
		labelID.setText("ID of the source plugin:"); //$NON-NLS-1$
		statusPluginID = new Text(group, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		statusPluginID.setLayoutData(data);
		statusPluginID
				.setToolTipText("Sets the ID of plugin which 'generated' the status");
		statusPluginID.setText(WorkbenchPlugin.PI_WORKBENCH);

	}

	public void setFocus() {
		if (durationField != null && !durationField.isDisposed())
			durationField.setFocus();
	}

	/**
	 * 
	 */
	private void progressNoFork() {
		try {
			final long duration = getDuration();
			final boolean shouldLock = progressNoForkLockField.getSelection();
			final Throwable throwable = getException();
			final long throwAfter = getThrowAfter(duration);

			IProgressService progressService = PlatformUI.getWorkbench()
					.getProgressService();
			progressService.runInUI(progressService,
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InterruptedException {
							if (shouldLock)
								doRunInWorkspace(duration, throwAfter,
										throwable, monitor);
							else
								doRun(duration, throwAfter, throwable, monitor);
						}
					}, ResourcesPlugin.getWorkspace().getRoot());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param duration
	 * @param throwAfter
	 * @param toBeThrown
	 * @param monitor
	 */
	protected void doRunInWorkspace(final long duration, final long throwAfter,
			final Throwable toBeThrown, IProgressMonitor monitor) {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doRun(duration, throwAfter, toBeThrown, monitor);
				}
			}, monitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param duration
	 * @param throwAfter
	 * @param toBeThrown
	 * @param monitor
	 */
	protected void doRun(long duration, final long throwAfter,
			final Throwable toBeThrown, IProgressMonitor monitor) {
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		int ticksToThrow = (int) (throwAfter / sleep);
		monitor.beginTask("Spinning inside IProgressService", ticks); //$NON-NLS-1$
		monitor.setTaskName("Spinning inside IProgressService"); //$NON-NLS-1$
		for (int i = 0; i < ticks; i++) {
			monitor.subTask("Processing tick #" + i); //$NON-NLS-1$
			if (monitor.isCanceled())
				return;
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// ignore
			}
			if (i == ticksToThrow) {
				throwException(toBeThrown);
				// toBeThrown is neither a runtime exception nor an error
				return;
			}
			monitor.worked(1);
		}
	}

	/**
	 * 
	 */
	protected void createJobs() {
		int jobCount = Integer.parseInt(quantityField.getText());
		boolean ui = threadField.getSelection();
		long duration = getDuration();
		boolean lock = jobLockField.getSelection();
		boolean system = systemField.getSelection();
		boolean useGroup = groupField.getSelection();
		boolean user = userField.getSelection();
		boolean reschedule = rescheduleField.getSelection();
		final long rescheduleWait = Long.parseLong(rescheduleDelay.getText());
		boolean returnError = returnErrorStatus.getSelection();
		Throwable throwable = getException();
		long throwAfter = getThrowAfter(duration);

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
				result = new UITestJob(duration, lock, throwAfter, throwable,
						returnError);
			else
				result = new TestJob(duration, lock, reschedule,
						rescheduleWait, throwAfter, throwable, returnError);

			result.setProgressGroup(group, groupIncrement);
			result.setSystem(system);
			result.setUser(user);
			result.schedule(delay);
		}
	}

	/**
	 * 
	 */
	public void runnableInWindow() {

		final long duration = getDuration();
		final long sleep = 10;

		final Throwable toBeThrown = getException();
		final long throwAfter = getThrowAfter(duration);

		IRunnableWithProgress runnableTest = new WorkspaceModifyOperation() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {

				int ticks = (int) (duration / sleep);
				int ticksToThrow = (int) (throwAfter / sleep);

				monitor.beginTask(
						"Spinning inside ApplicationWindow.run()", ticks); //$NON-NLS-1$
				monitor.setTaskName("Spinning inside ApplicationWindow.run()"); //$NON-NLS-1$
				for (int i = 0; i < ticks; i++) {
					monitor.subTask("Processing tick #" + i); //$NON-NLS-1$
					if (monitor.isCanceled())
						return;
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						// ignore
					}
					if (i == ticksToThrow) {
						throwException(toBeThrown);
						// toBeThrown is neither a runtime exception nor an
						// error
						return;
					}
					monitor.worked(1);
				}
			}

		};

		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true,
					true, runnableTest);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return duration
	 */
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

	/**
	 * @return throwable
	 */
	protected Throwable getException() {

		switch (exceptionField.getSelectionIndex()) {
		case 0:
			return new NullPointerException("A sample NullPointerException");
		case 1:
			return new OutOfMemoryError("A sample OutOfMemoryError");
		case 2:
			return new IndexOutOfBoundsException(
					"A sample IndexOutOfBoundsException");
		default:
			return new NullPointerException("A sample NullPointerException");

		}
	}

	private long getThrowAfter(long duration) {
		return Math.round(((double) duration)
				* (((double) percent.getSelection()) / 100.0));
	}

	private void throwException(Throwable th) {
		if (th == null)
			return;

		if (th instanceof RuntimeException)
			throw (RuntimeException) th;

		if (th instanceof Error) {
			throw (Error) th;
		}

		// TODO do something !! the exception cannot be thrown
	}

	/**
	 * A registry reader for sample extension point.
	 */
	private static class SampleRegistryReader extends RegistryReader {

		/**
		 * Create a new instance of the receiver.
		 * 
		 */
		public SampleRegistryReader() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.registry.RegistryReader#readElement(org.eclipse.core.runtime.IConfigurationElement)
		 */
		protected boolean readElement(IConfigurationElement element) {

			String name = element.getName();

			if (name != null) {
				if (!name.equals("requiredElement"))
					return false;
			}
			return true;
		}
	}

	private class ContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return statusItems.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	/**
	 * An item to be displayed in the table.
	 */
	private static class DisplayedItem {

		private int hint;

		private IStatus status;

		/**
		 * Constructs a new item.
		 * 
		 * @param status
		 * @param hint
		 */
		public DisplayedItem(IStatus status, int hint) {
			if (status == null)
				throw new IllegalArgumentException();
			this.status = status;
			this.hint = hint;
		}

		public String toString() {

			String severity = "?";

			switch (status.getSeverity()) {
			case IStatus.CANCEL:
				severity = "Cancel";
				break;
			case IStatus.ERROR:
				severity = "Error";
				break;
			case IStatus.INFO:
				severity = "Info";
				break;
			case IStatus.OK:
				severity = "Ok";
				break;
			case IStatus.WARNING:
				severity = "Warning";
				break;
			}

			String stringHint = "?";

			switch (hint) {
			case StatusManager.LOG:
				stringHint = "LOG";
				break;
			case StatusManager.NONE:
				stringHint = "NONE";
				break;
			case StatusManager.SHOW:
				stringHint = "SHOW";
				break;
			case StatusManager.SHOW | StatusManager.LOG:
				stringHint = "SHOWANDLOG";
				break;
			}

			return "Severity: " + severity + "   PluginID: "
					+ status.getPlugin() + "   Hint: " + stringHint;
		}

		/**
		 * @return the hint
		 */
		public int getHint() {
			return hint;
		}

		/**
		 * @return the status
		 */
		public IStatus getStatus() {
			return status;
		}
	}

}
