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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
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
import org.eclipse.ui.examples.jobs.TestJob;
import org.eclipse.ui.examples.jobs.UITestJob;
import org.eclipse.ui.part.ViewPart;
/**
 * A view that allows a user to create jobs of various types.
 */
public class JobsView extends ViewPart {
	private Combo durationField;
	private Button lockField, failureField, threadField, systemField, userField,  groupField ,unknownField;
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
		int total =IProgressMonitor.UNKNOWN;
			
		if(jobCount > 1){
			total = 100;
			groupIncrement = 100/ jobCount;
		}
			
		
		if(useGroup){
			
			group = Platform.getJobManager().createProgressGroup();
			group.beginTask("Group", total);
		}
		
		long delay = Integer.parseInt(delayField.getText());
		for (int i = 0; i < jobCount; i++) {
			Job result;
			if (ui)
				result = new UITestJob(duration, lock, failure,unknown);
			else
				result = new TestJob(duration, lock, failure,unknown);
			
			result.setProgressGroup(group,groupIncrement);
			result.setSystem(system);
			result.setUser(user);
			result.schedule(delay);
		}
		
		
	}
	/**
	 * @see ViewPart#createPartControl
	 */
	public void createPartControl(Composite parent) {
		Composite body = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		body.setLayout(layout);

		//duration
		Label label = new Label(body, SWT.NONE);
		label.setText("Duration:");
		durationField = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		durationField.setLayoutData(data);
		durationField.add("1 second");
		durationField.add("10 seconds");
		durationField.add("1 minute");
		durationField.add("10 minutes");
		durationField.select(2);

		//delay
		label = new Label(body, SWT.NONE);
		label.setText("Start delay (ms):");
		delayField = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		delayField.setLayoutData(data);
		delayField.setText("0");

		//quantity
		label = new Label(body, SWT.NONE);
		label.setText("Quantity:");
		quantityField = new Text(body, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		quantityField.setLayoutData(data);
		quantityField.setText("1");

		//lock
		lockField = new Button(body, SWT.CHECK);
		lockField.setText("Lock the workspace");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		lockField.setLayoutData(data);

		//system
		systemField = new Button(body, SWT.CHECK);
		systemField.setText("System job");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		systemField.setLayoutData(data);

		//thread
		threadField = new Button(body, SWT.CHECK);
		threadField.setText("Run in UI thread");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		threadField.setLayoutData(data);

		//failure
		failureField = new Button(body, SWT.CHECK);
		failureField.setText("Fail");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		failureField.setLayoutData(data);

		//groups
		groupField = new Button(body, SWT.CHECK);
		groupField.setText("Run in Group");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		groupField.setLayoutData(data);
		
//		//IProgressMonitor.UNKNOWN
		unknownField = new Button(body, SWT.CHECK);
		unknownField.setText("Indeterminate Progress");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		unknownField.setLayoutData(data);
		
//		//system
		userField = new Button(body, SWT.CHECK);
		userField.setText("User job");
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		userField.setLayoutData(data);

		//create jobs
		Button create = new Button(body, SWT.PUSH);
		create.setText("Create jobs");
		create.setToolTipText("Creates and schedules jobs according to above parameters");
		create.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createJobs();
			}
		});

		//touch workspace
		Button touch = new Button(body, SWT.PUSH);
		touch.setText("Touch workspace");
		touch.setToolTipText("Modifies the workspace in the UI thread");
		touch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				touchWorkspace();
			}
		});
		//busy cursor while
		Button busyWhile= new Button(body, SWT.PUSH);
		busyWhile.setText("Progress Service");
		busyWhile.setToolTipText("Uses IProgressService.busyCursorWhile");
		busyWhile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				busyCursorWhile();
			}
		});
		//progress monitor dialog with fork=false
		Button noFork= new Button(body, SWT.PUSH);
		noFork.setText("Dialog (fork==false");
		noFork.setToolTipText("ProgressMonitorDialog.run(fork==false)");
		noFork.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				progressNoFork();
			}
		});
		
		//progress monitor dialog with fork=false
		Button exception = new Button(body, SWT.PUSH);
		exception.setText("Runtime Exception");
		exception.setToolTipText("NullPointerException when running");
		exception.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				jobWithRuntimeException();
			}
		});
		
		
	}
	protected void doRun(long duration, IProgressMonitor monitor) {
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		monitor.beginTask("Spinning inside IProgressService.busyCursorWhile", ticks);
		monitor.setTaskName("Spinning inside IProgressService.busyCursorWhile");
		for (int i = 0; i < ticks; i++) {
			monitor.subTask("Processing tick #" + i);
			if (monitor.isCanceled())
				return;
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
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
	 * @see ViewPart#setFocus
	 */
	public void setFocus() {
	}
	protected void touchWorkspace() {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					//no-op
				}
			}, null);
		} catch (OperationCanceledException e) {
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	protected void jobWithRuntimeException() {
		Job runtimeExceptionJob = new Job("Job with Runtime exception"){
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				throw new NullPointerException();
				
			}
		};
		runtimeExceptionJob.schedule();
		
		
	}
	
}