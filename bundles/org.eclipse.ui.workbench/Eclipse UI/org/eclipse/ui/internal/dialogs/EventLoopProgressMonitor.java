/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ExceptionHandler;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.progress.BlockedJobsDialog;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.progress.WorkbenchJob;
/**
 * Used to run an event loop whenever progress monitor methods
 * are invoked.  <p>
 * This is needed since editor save operations are done in the UI thread.  
 * Although save operations should be written to do the work in the non-UI thread, 
 * this was not done for 1.0, so this was added to keep the UI live
 * (including allowing the cancel button to work).
 */
public class EventLoopProgressMonitor extends ProgressMonitorWrapper
		implements
			IProgressMonitorWithBlocking {
	/**
	 * Threshold for how often the event loop is spun, in ms.
	 */
	private static int T_THRESH = 100;
	/**
	 * Maximum amount of time to spend processing events, in ms.
	 */
	private static int T_MAX = 50;
	/**
	 * The dialog that is shown when the operation is blocked
	 */
	private BlockedJobsDialog dialog;
	/**
	 * Last time the event loop was spun.
	 */
	private long lastTime = System.currentTimeMillis();
	/**
	 * Constructs a new monitor.
	 */
	public EventLoopProgressMonitor(IProgressMonitor monitor) {
		super(monitor);
	}
	/** 
	 * @see IProgressMonitor#beginTask
	 */
	public void beginTask(String name, int totalWork) {
		super.beginTask(name, totalWork);
		runEventLoop();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#clearBlocked()
	 */
	public void clearBlocked() {
		//the UI operation is no longer blocked so get rid of the progress dialog
		if (dialog == null || dialog.getShell() == null || dialog.getShell().isDisposed()){
			dialog = null;
			return;
		}
		dialog.close();
		dialog = null;
	}
	/**
	 * @see IProgressMonitor#done
	 */
	public void done() {
		super.done();
		runEventLoop();
	}
	/**
	 * @see IProgressMonitor#internalWorked
	 */
	public void internalWorked(double work) {
		super.internalWorked(work);
		runEventLoop();
	}
	/**
	 * @see IProgressMonitor#isCanceled
	 */
	public boolean isCanceled() {
		runEventLoop();
		return super.isCanceled();
	}
	/**
	 * Runs an event loop.
	 */
	private void runEventLoop() {
		// Only run the event loop so often, as it is expensive on some platforms
		// (namely Motif).
		long t = System.currentTimeMillis();
		if (t - lastTime < T_THRESH) {
			return;
		}
		lastTime = t;
		// Run the event loop.
		Display disp = Display.getDefault();
		if (disp == null) {
			return;
		}
		
		//Initialize an exception handler from the window class.
		ExceptionHandler handler = new ExceptionHandler();
		
		for (;;) {
			try {
				if(!disp.readAndDispatch())
					break;
			} catch (Throwable e) {//Handle the exception the same way as the workbench
				handler.handleException(e);
				break;
			}			
			
			// Only run the event loop for so long.
			// Otherwise, this would never return if some other thread was 
			// constantly generating events.
			if (System.currentTimeMillis() - t > T_MAX) {
				break;
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitorWithBlocking#setBlocked(org.eclipse.core.runtime.IStatus)
	 */
	public void setBlocked(IStatus reason) {
		
		//The UI operation has been blocked.  Open a progress dialog
		//to report the situation and give the user an opportunity to cancel.
		
		dialog = new BlockedJobsDialog(null, EventLoopProgressMonitor.this,reason);
		dialog.setBlockOnOpen(false);
		
		WorkbenchJob dialogJob = new WorkbenchJob(WorkbenchMessages.getString("EventLoopProgressMonitor.OpenDialogJobName")){ //$NON-NLS-1$
			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				
				if(dialog == null)
					return Status.CANCEL_STATUS;
				if(ProgressManagerUtil.rescheduleIfModalShellOpen(this))
					return Status.CANCEL_STATUS;
				dialog.open();
				return Status.OK_STATUS;
			}
		};
		
		//Wait for long operation time to prevent a proliferation
		//of dialogs
		dialogJob.setSystem(true);
		dialogJob.schedule(PlatformUI.getWorkbench().getProgressService().getLongOperationTime());
		
	}
	/**
	 * @see IProgressMonitor#setCanceled
	 */
	public void setCanceled(boolean b) {
		super.setCanceled(b);
		runEventLoop();
	}
	/**
	 * @see IProgressMonitor#setTaskName
	 */
	public void setTaskName(String name) {
		super.setTaskName(name);
		runEventLoop();
	}
	/**
	 * @see IProgressMonitor#subTask
	 */
	public void subTask(String name) {
		super.subTask(name);
		runEventLoop();
	}
	/**
	 * @see IProgressMonitor#worked
	 */
	public void worked(int work) {
		super.worked(work);
		runEventLoop();
	}
}
