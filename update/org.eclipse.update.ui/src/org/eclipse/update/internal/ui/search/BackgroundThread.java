package org.eclipse.update.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.operation.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.util.Assert;

public class BackgroundThread extends Thread {
	/**
	 * The operation to be run.
	 */
	private IRunnableWithProgress runnable;
		
	/** 
	 * The exception thrown by the operation starter.
	 */
	private Throwable throwable;
		
	/**
	 * The progress monitor used for progress and cancelation.
	 */
	private IProgressMonitor progressMonitor;
		
	/**
	 * The display used for event dispatching.
	 */
	private Display display;
		
	/**
	 * Indicates whether to continue event queue dispatching.
	 */
	private volatile boolean continueEventDispatching = true;

	public BackgroundThread(IRunnableWithProgress operation, IProgressMonitor monitor, Display display) {
		super("BackgroundThread"); //$NON-NLS-1$
		Assert.isTrue(monitor != null && display != null);
		runnable = operation;
		this.progressMonitor = monitor;
		this.display = display;
	}
	/* (non-Javadoc)
	 * Method declared on Thread.
	 */
	public void run() {
		try {
			if (runnable != null)
				runnable.run(progressMonitor);
		} catch (InvocationTargetException e) {
			throwable= e;
		} catch (InterruptedException e) {
			throwable= e;
		} catch (RuntimeException e) {
			//throwable= e;
			throw e;
		} catch (ThreadDeath e) {
			// Make sure to propagate ThreadDeath, or threads will never fully terminate
			throw e;
		} catch (Error e) {
			throwable= e;
		} finally {
			// Make sure that all events in the asynchronous event queue
			// are dispatched.
			display.syncExec(new Runnable() {
				public void run() {
					// do nothing
				}
			});
				
			// Stop event dispatching
			continueEventDispatching= false;
				
			// Force the event loop to return from sleep () so that
			// it stops event dispatching.
			display.asyncExec(null);
			if (throwable!=null) {
				display.asyncExec(new Runnable() {
					public void run() {
						UpdateUIPlugin.logException(throwable);
					}
				});
			}
		}	
	}
	public Throwable getThrowable() {
		return throwable;
	}
}

