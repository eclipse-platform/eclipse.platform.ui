/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;

import org.eclipse.core.runtime.*;
import org.eclipse.compare.contentmergeviewer.*;

/**
 * A modal dialog that displays progress during a long running operation.
 */
public class TimeoutContext {

	private static class ModalContextThread extends Thread {
		
		private boolean fWorking;
		private IRunnableWithProgress fRunnable;
		private Throwable fThrowable;
		private IProgressMonitor fProgressMonitor;
		private boolean fContinueEventDispatching= true;
		private Display fDisplay;

		private ModalContextThread(IRunnableWithProgress operation, IProgressMonitor monitor) {
			super("TimeoutContext"); //$NON-NLS-1$
			fRunnable= operation;
			fProgressMonitor= monitor;
		}
		
		synchronized boolean timeout(Display display) {
			fDisplay= display;
			return fWorking;
		}
		
		public void run() {
			try {
				fWorking= true;
				if (fRunnable != null)
					fRunnable.run(fProgressMonitor);
			} catch (InvocationTargetException e) {
				fThrowable= e;
			} catch (InterruptedException e) {
				fThrowable= e;
			} catch (RuntimeException e) {
				fThrowable= e;
			} catch (ThreadDeath e) {
				// Make sure to propagate ThreadDeath, or threads will never fully terminate
				throw e;
			} catch (Error e) {
				fThrowable= e;
			} finally {
				fWorking= false;
	
				if (fDisplay != null) {
					// Make sure that all events in the asynchronous event queue
					// are dispatched.
					fDisplay.syncExec(
						new Runnable() {
							public void run() {
								// do nothing
							}
						}
					);
					
					// Stop event dispatching
					fContinueEventDispatching= false;
					
					// Force the event loop to return from sleep () so that
					// it stops event dispatching.
					fDisplay.asyncExec(null);
				}
			}	
		}
		
		public void block() {
			while (fContinueEventDispatching)
				if (!fDisplay.readAndDispatch())
					fDisplay.sleep();
		}		
	}
	
	static class ProgressMonitorDialog extends org.eclipse.jface.dialogs.Dialog {

		protected ProgressIndicator fProgressIndicator;
		protected Label fTaskLabel;
		protected Label fSubTaskLabel;
		protected Button fCancel;
		protected boolean fEnableCancelButton;
		private ProgressMonitor fProgressMonitor;
		private Cursor fArrowCursor;
		private Cursor fWaitCursor;
		private Shell fParentShell;
		
		private ProgressMonitorDialog(Shell parent, boolean cancelable, ProgressMonitor pm) {
			super(parent);
			fProgressMonitor= pm;
			fParentShell= parent;
			fEnableCancelButton= cancelable;
			this.setBlockOnOpen(false);
			setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL); // no close button
		}
		
		public boolean close() {
			if (fCancel != null && !fCancel.isDisposed())
				fCancel.setCursor(null);
			Shell shell= getShell();
			if (shell != null && !shell.isDisposed())
				shell.setCursor(null);
			if (fArrowCursor != null)
				fArrowCursor.dispose();
			if (fWaitCursor != null)
				fWaitCursor.dispose();
			fArrowCursor= null;
			fWaitCursor= null;
			return super.close();
		}
		
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText(JFaceResources.getString("ProgressMonitorDialog.title")); //$NON-NLS-1$
			if (fWaitCursor == null)
				fWaitCursor= new Cursor(shell.getDisplay(),SWT.CURSOR_WAIT);
			shell.setCursor(fWaitCursor);
		}
		
		protected void createButtonsForButtonBar(Composite parent) {
			// cancel button		
			fCancel= createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
			if(fArrowCursor == null)
				fArrowCursor= new Cursor(fCancel.getDisplay(),SWT.CURSOR_ARROW);		
			fCancel.setCursor(fArrowCursor);
			fCancel.addListener(SWT.Selection,
				new Listener() {
					public void handleEvent(Event e) {
						if (fCancel != null && !fCancel.isDisposed())
							fCancel.setEnabled(false);
						fProgressMonitor.setCanceled(true);
					}
				}
			);
			fCancel.setEnabled(fEnableCancelButton);
		}
		
		protected Control createDialogArea(Composite parent) {
		
			Composite c= (Composite)super.createDialogArea(parent);
			((GridLayout)c.getLayout()).numColumns= 2;
	
			// icon
			Label iconLabel= new Label(c, SWT.LEFT);
			GridData gd= new GridData();
			iconLabel.setLayoutData(gd);
			iconLabel.setFont(parent.getFont());
			Image i= JFaceResources.getImageRegistry().get(org.eclipse.jface.dialogs.Dialog.DLG_IMG_INFO);
			if (i != null)
				iconLabel.setImage(i);
			else
				iconLabel.setText(JFaceResources.getString("Image_not_found")); //$NON-NLS-1$
	
			// label on right hand side of icon
			fTaskLabel= new Label(c, SWT.LEFT | SWT.WRAP);
			fTaskLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fTaskLabel.setFont(parent.getFont());
	
			// progress indicator
			fProgressIndicator= new ProgressIndicator(c);
			gd= new GridData();
			gd.heightHint= 15;
			gd.horizontalAlignment= gd.FILL;
			gd.grabExcessHorizontalSpace= true;
			gd.horizontalSpan= 2;
			fProgressIndicator.setLayoutData(gd);
	
			// label showing current task
			fSubTaskLabel= new Label(c, SWT.LEFT | SWT.WRAP);
			gd= new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint= 35;	
			gd.horizontalSpan= 2;
			fSubTaskLabel.setLayoutData(gd);
			fSubTaskLabel.setFont(parent.getFont());
						
			// update with values fProgressMonitor
			String s= fProgressMonitor.fTask;
			if (s == null)
				s= ""; //$NON-NLS-1$
			fTaskLabel.setText(s);
			
			if (fProgressMonitor.fMaxWork == IProgressMonitor.UNKNOWN)
				fProgressIndicator.beginAnimatedTask();
			else
				fProgressIndicator.beginTask(fProgressMonitor.fMaxWork);

			if (fProgressMonitor.fSubTask != null)
				fSubTaskLabel.setText(fProgressMonitor.fSubTask);
			fProgressIndicator.worked(fProgressMonitor.fWorked);
	
			fProgressMonitor.activate(this);

			return c;
		}
		
		void beginTask(final String name, final int totalWork) {
			fParentShell.getDisplay().syncExec(
				new Runnable() {
					public void run() {
						fTaskLabel.setText(name);
						
						if (fProgressIndicator != null && fProgressIndicator.isDisposed()) {
							if (totalWork == IProgressMonitor.UNKNOWN)
								fProgressIndicator.beginAnimatedTask();
							else
								fProgressIndicator.beginTask(totalWork);
						}
					}
				}
			);
		}
		
		void setTaskName(final String name) {
			fParentShell.getDisplay().syncExec(
				new Runnable() {
					public void run() {
						if (fTaskLabel != null && fTaskLabel.isDisposed())
							fTaskLabel.setText(name);
					}
				}
			);
		}

		void setSubTaskName(final String name) {
			fParentShell.getDisplay().syncExec(
				new Runnable() {
					public void run() {
						String n= (name == null) ? "" : name;	//$NON-NLS-1$
						if (fSubTaskLabel != null && !fSubTaskLabel.isDisposed())
							fSubTaskLabel.setText(n);
					}
				}
			);	
		}
		
		void done() {
			fParentShell.getDisplay().syncExec(
				new Runnable() {
					public void run() {
						if (fProgressIndicator != null && !fProgressIndicator.isDisposed()) {
							fProgressIndicator.sendRemainingWork();
							fProgressIndicator.done();
						}
					}
				}
			);
		}
		
		void worked(final double work) {
			fParentShell.getDisplay().syncExec(
				new Runnable() {
					public void run() {
						if (fProgressIndicator != null && !fProgressIndicator.isDisposed())
							fProgressIndicator.worked(work);
					}
				}
			);
		}

		protected Point getInitialSize() {
			return getShell().computeSize(450, SWT.DEFAULT);
		}
	}

	private static class ProgressMonitor implements IProgressMonitor {
		
		private int fMaxWork;
		private String fTask;
		private String fSubTask;
		private int fWorked;
		private ProgressMonitorDialog fProgressMonitorDialog;
		private boolean fIsCanceled;
		
		public void beginTask(String name, int totalWork) {
			
			fTask= name;
			fMaxWork= totalWork;
			fWorked= 0;
			
			if (fProgressMonitorDialog != null)	
				fProgressMonitorDialog.beginTask(name, totalWork);			
		}
		
		void activate(ProgressMonitorDialog dialog) {
			fProgressMonitorDialog= dialog;
		}
		
		public void done() {
			if (fProgressMonitorDialog != null)				
				fProgressMonitorDialog.done();
		}
		
		public void setTaskName(String name) {
			fTask= name;
			if (fProgressMonitorDialog != null)				
				fProgressMonitorDialog.setTaskName(name);
		}
				
		public boolean isCanceled() {
			return fIsCanceled;
		}
		
		public void setCanceled(boolean b) {
			fIsCanceled= b;
		}
		
		public void subTask(String name) {
			fSubTask= name;
			if (fProgressMonitorDialog != null)				
				fProgressMonitorDialog.setSubTaskName(name);
		}
		
		public void worked(int work) {
			if (fProgressMonitorDialog != null)
				internalWorked(work);
			else
				fWorked+= work;
		}
		
		public void internalWorked(double work) {
			if (fProgressMonitorDialog != null)				
				fProgressMonitorDialog.worked(work);
		}
	}
	
	public static void run(boolean cancelable, int timeout, Shell parent, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		
		Display display= parent.getDisplay();
		
		ProgressMonitor pm= new ProgressMonitor();
				
		ModalContextThread t= new ModalContextThread(runnable, pm);
		t.start();	// start the long running operation
		
		// wait until long operations terminates or timeout
		try {
			t.join(timeout);
		} catch (InterruptedException e) {
		}
		
		if (t.timeout(display)) {	// timeout
			ProgressMonitorDialog dialog= new ProgressMonitorDialog(parent, cancelable, pm);
			dialog.open();
			t.block();
			dialog.close();
		}
		
		Throwable throwable= t.fThrowable;
		if (throwable != null) {
			if (throwable instanceof InvocationTargetException) {
				throw (InvocationTargetException) throwable;
			} else if (throwable instanceof InterruptedException) {
				throw (InterruptedException) throwable;
			} else if (throwable instanceof OperationCanceledException) {
				throw new InterruptedException(throwable.getMessage());
			} else {
				throw new InvocationTargetException(throwable);
			}	
		}
	}
}

