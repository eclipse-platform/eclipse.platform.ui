package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import java.lang.reflect.InvocationTargetException;

/**
 * A modal dialog that displays progress during a long running operation.
 * <p>
 * This concete dialog class can be instantiated as is, 
 * or further subclassed as required.
 * </p>
 * <p>
 * Typical usage is:
 * <pre>
 * try {
 *    IRunnableWithProgress op = ...;
 *    new ProgressMonitorDialog(activeShell).run(true, true, op);
 * } catch (InvocationTargetException e) {
 *    // handle exception
 * } catch (InterruptedException e) {
 *    // handle cancelation
 * }
 * </pre>
 * </p>
 * <p>
 * Note that the ProgressMonitorDialog is not intended to be used
 * with multiple runnables - this dialog should be discarded
 * after completion of one IRunnableWithProgress and a new one instantiated
 * for use by a second or sebsequent IRunnableWithProgress to ensure
 * proper initialization.
 * </p> 
 */
public class ProgressMonitorDialog extends Dialog implements IRunnableContext {

	/**
	 * Name to use for task when normal task name is empty string.
	 */
	private static String DEFAULT_TASKNAME= JFaceResources.getString("ProgressMonitorDialog.message"); //$NON-NLS-1$
	
	/**
	 * Constants for label and monitor size
	 */
	private static int LABEL_DLUS = 21;
	private static int BAR_DLUS = 9;


	/**
	 * The progress indicator control.
	 */
	protected ProgressIndicator progressIndicator;

	/**
	 * The label control for the task.
	 */
	protected Label taskLabel;

	/**
	 * The label control for the subtask.
	 */
	protected Label subTaskLabel;

	/**
	 * The Cancel button control.
	 */
	protected Button cancel;

	/**
	 * Indicates whether the Cancel button is to be shown.
	 */
	protected boolean operationCancelableState = false;

	/**
	 * Indicates whether the Cancel button is to be enabled.
	 */
	protected boolean enableCancelButton;
		
	/**
	 * The progress monitor.
	 */
	private ProgressMonitor progressMonitor = new ProgressMonitor();

	/**
	 * The name of the current task (used by ProgressMonitor).
	 */
	private String task;

	/**
	 * The number of currently running runnables.
	 */
	private int runningRunnables;
	
	/**
	 * The cursor used in the cancel button;
	 */
	private Cursor arrowCursor;

	/**
	 * The cursor used in the shell;
	 */
	private Cursor waitCursor;
	
	/**
	 * Internal progress monitor implementation.
	 */
	private class ProgressMonitor implements IProgressMonitor {
		
		private String fSubTask= "";//$NON-NLS-1$
		private boolean fIsCanceled;
		protected boolean forked = false;
		
		public void beginTask(String name, int totalWork) {
			//System.out.println("beginTask: " + name + " " + totalWork);
			if (progressIndicator.isDisposed())
				return;
				
			if (name == null)
				task= "";//$NON-NLS-1$
			else	
				task= name;

			String s= task;
			if (s.length() <= 0)
				s= DEFAULT_TASKNAME;
			taskLabel.setText(s);	
			if(!forked)
				taskLabel.update();
			
			if (totalWork == UNKNOWN) {
				progressIndicator.beginAnimatedTask();
			} else {
				progressIndicator.beginTask(totalWork);
			}	
		}
		
		public void done() {
			//System.out.println("done");
			if (!progressIndicator.isDisposed()) {
				progressIndicator.sendRemainingWork();
				progressIndicator.done();
			}
		}
		
		public void setTaskName(String name) {
			//System.out.println("setTaskName " + name);
			if (taskLabel.isDisposed())
				return;
			if (name == null)
				task= "";//$NON-NLS-1$
			else	
				task= name;

			String s= task;
			if (s.length() <= 0)
				s= DEFAULT_TASKNAME;
			taskLabel.setText(s);	
			if(!forked)
				taskLabel.update();
		}
				
		public boolean isCanceled() {
			return fIsCanceled;
		}
		
		public void setCanceled(boolean b) {
			fIsCanceled= b;
		}
		
		public void subTask(String name) {
			//System.out.println("subTask " + name);
			if (subTaskLabel.isDisposed())
				return;
				
			if (name == null)
				fSubTask= "";//$NON-NLS-1$
			else
				fSubTask= name;
		
			subTaskLabel.setText(fSubTask);
			if(!forked)
				subTaskLabel.update();
		}
		
		public void worked(int work) {
			//System.out.println("worked " + work);
			internalWorked(work);
		}
		
		public void internalWorked(double work) {
			//System.out.println("internalWorked " + work);
			if (!progressIndicator.isDisposed())
				progressIndicator.worked(work);
		}
	}
/**
 * Creates a progress monitor dialog under the given shell.
 * The dialog has a standard title and no image. 
 * <code>open</code> is non-blocking.
 *
 * @param parent the parent shell
 */
public ProgressMonitorDialog(Shell parent) {
	super(parent);
	setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL); // no close button
	setBlockOnOpen(false);
}
/**
 * Enables the cancel button (asynchronously).
 */
private void asyncSetOperationCancelButtonEnabled(final boolean b) {
	if (getShell() != null) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setOperationCancelButtonEnabled(b);
			}
		});
	}
}
/* (non-Javadoc)
 * Method declared on Window.
 */
/**
 * The <code>ProgressMonitorDialog</code> implementation of this method
 * only closes the dialog if there are no currently running runnables.
 */
public boolean close() {
	if (runningRunnables <= 0) {
		if (cancel != null && !cancel.isDisposed()) {
			cancel.setCursor(null);
		}
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.setCursor(null);
		}
		if (arrowCursor != null)
			arrowCursor.dispose();
		if (waitCursor != null)
			waitCursor.dispose();
		arrowCursor = null;
		waitCursor = null;
		return super.close();
	}
	return false;
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(JFaceResources.getString("ProgressMonitorDialog.title")); //$NON-NLS-1$
	if(waitCursor == null)
		waitCursor = new Cursor(shell.getDisplay(),SWT.CURSOR_WAIT);
	shell.setCursor(waitCursor);
}

/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected void createButtonsForButtonBar(Composite parent) {
	// cancel button		
	cancel = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	if(arrowCursor == null)
		arrowCursor = new Cursor(cancel.getDisplay(),SWT.CURSOR_ARROW);		
	cancel.setCursor(arrowCursor);
	cancel.addListener(SWT.Selection,
		new Listener() {
			public void handleEvent(Event e) {
				cancel.setEnabled(false);
				progressMonitor.setCanceled(true);
			}
		}
	);
	setOperationCancelButtonEnabled(enableCancelButton);
}
/*
 * @see Dialog.createContents(Composite)
 */
protected Control createContents(Composite parent) {
	
	// initialize the dialog units
	initializeDialogUnits(parent);
	
	GridLayout layout = new GridLayout();
	layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	layout.makeColumnsEqualWidth = false;
	layout.numColumns = 2;
	parent.setLayout(layout);
	parent.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	// create the dialog area and button bar
	dialogArea = createDialogArea(parent);
	buttonBar = createButtonBar(parent);
	
	GridData barData = (GridData) buttonBar.getLayoutData();
	barData.horizontalSpan = 2;
	buttonBar.setLayoutData(barData);
	
	return parent;
}


/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	
	// icon
	Label iconLabel= new Label(parent, SWT.LEFT);
	iconLabel.setLayoutData(new GridData(
		GridData.HORIZONTAL_ALIGN_CENTER |
		GridData.VERTICAL_ALIGN_BEGINNING));
	Image i= JFaceResources.getImageRegistry().get(Dialog.DLG_IMG_INFO);
	if (i != null)
		iconLabel.setImage(i);
	else {
		iconLabel.setText(JFaceResources.getString("Image_not_found")); //$NON-NLS-1$
	}

	// label on right hand side of icon
	taskLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
	GridData gd = new GridData(
		GridData.GRAB_HORIZONTAL |
		GridData.GRAB_VERTICAL |
		GridData.HORIZONTAL_ALIGN_FILL |
		GridData.VERTICAL_ALIGN_CENTER);
	gd.heightHint= convertVerticalDLUsToPixels(LABEL_DLUS);
	taskLabel.setLayoutData(gd);
	taskLabel.setText(DEFAULT_TASKNAME);
	taskLabel.setFont(parent.getFont());

	// progress indicator
	progressIndicator= new ProgressIndicator(parent);
	gd= new GridData();
	gd.heightHint= convertVerticalDLUsToPixels(BAR_DLUS);
	gd.horizontalAlignment= gd.FILL;
	gd.grabExcessHorizontalSpace= true;
	gd.horizontalSpan= 2;
	progressIndicator.setLayoutData(gd);

	// label showing current task
	subTaskLabel= new Label(parent, SWT.LEFT | SWT.WRAP);
	gd= new GridData(GridData.FILL_HORIZONTAL);
	gd.heightHint= convertVerticalDLUsToPixels(LABEL_DLUS);
	gd.horizontalSpan= 2;
	subTaskLabel.setLayoutData(gd);
	subTaskLabel.setFont(parent.getFont());

	return parent;
}

/* (non-Javadoc)
 * Method declared in Window.
 */
protected Point getInitialSize() {
	return getShell().computeSize(450, SWT.DEFAULT);
}
/**
 * Returns the progress monitor to use for operations run in 
 * this progress dialog.
 *
 * @return the progress monitor
 */
public IProgressMonitor getProgressMonitor() {
	return progressMonitor;
}
/* (non-Javadoc)
 * Method declared on IRunnableContext.
 * Runs the given <code>IRunnableWithProgress</code> with the progress monitor for this
 * progress dialog.  The dialog is opened before it is run, and closed after it completes.
 */
public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
	setCancelable(cancelable);
	open();
	try {
		runningRunnables++;
		
		//Let the progress monitor know if they need to update in UI Thread
		progressMonitor.forked = fork;
		ModalContext.run(runnable, fork, getProgressMonitor(), getShell().getDisplay());
	} finally {	
		runningRunnables--;
		close();
	}
}
/**
 * Sets whether the progress dialog is cancelable or not.
 *
 * @param cancelable <code>true</code> if the end user can cancel
 *   this progress dialog, and <code>false</code> if it cannot be canceled
 */
public void setCancelable(boolean cancelable) {
	if (cancel == null)
		enableCancelButton = cancelable;
	else
		asyncSetOperationCancelButtonEnabled(cancelable);
}
/**
 * Helper to enable/disable Cancel button for this dialog.
 *
 * @param b <code>true</code> to enable the cancel button,
 *   and <code>false</code> to disable it
 */
private void setOperationCancelButtonEnabled(boolean b) {
	operationCancelableState = b;
	cancel.setEnabled(b);
}
}
