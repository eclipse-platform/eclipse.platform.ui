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
package org.eclipse.jface.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.util.Assert;

/**
 * A progress monitor that accumulates <code>worked</code> and <code>subtask</code> 
 * calls in the following way by wrapping a standard progress monitor:
 * <ul>
 * <li> When a <code>worked</code> or <code>subtask</code> call occurs the first time,
 *		the progress monitor posts a runnable into the asynchronous SWT event queue.
 * </li>
 * <li> Subsequent calls to <code>worked</code> or <code>subtask</code> do not post
 *		a new runnable as long as a previous runnable still exists in the SWT event
 *		queue. In this case, the progress monitor just updates the internal state of
 *		the runnable that waits in the SWT event queue for its execution. If no runnable
 *		exists, a new one is created and posted into the event queue.
 * </ul>
 * <p>
 * This class is internal to the framework; clients outside JFace should not
 * use this class.
 * </p>
 */
/* package */ class AccumulatingProgressMonitor extends ProgressMonitorWrapper {

	/**
	 * The display.
	 */
	private Display display;

	/**
	 * The collector, or <code>null</code> if none.
	 */
	private Collector collector;
	
	private class Collector implements Runnable {
		private String subTask;
		private double worked;
		private IProgressMonitor monitor;
		
		public Collector(String subTask, double work, IProgressMonitor monitor) {
			this.subTask = subTask;
			this.worked = work;
			this.monitor = monitor;
		}
		
		public void worked(double worked) {
			this.worked = this.worked + worked;
		}
		
		public void subTask(String subTask) {
			this.subTask = subTask;
		}
		
		public void run() {
			clearCollector(this);
			if (subTask != null)
				monitor.subTask(subTask);
			if (worked > 0)
				monitor.internalWorked(worked);
		}
	}		
/**
 * Creates an accumulating progress monitor wrapping the given one
 * that uses the given display.
 * 
 * @param monitor the actual progress monitor to be wrapped
 * @param display the SWT display used to forward the calls 
 *  to the wrapped progress monitor
 */
public AccumulatingProgressMonitor(IProgressMonitor monitor, Display display) {
	super(monitor);
	Assert.isNotNull(display);
	this.display = display;
}
/* (non-Javadoc)
 * Method declared on IProgressMonitor.
 */
public void beginTask(final String name, final int totalWork) {
	synchronized(this) {
		collector = null;
	}	
	display.syncExec(new Runnable() {
		public void run() {
			getWrappedProgressMonitor().beginTask(name, totalWork);
		}
	});
}
/**
 * Clears the collector object used to accumulate work and subtask calls
 * if it matches the given one.
 */
private synchronized void clearCollector(Collector collector) {
	// Check if the accumulator is still using the given collector.
	// If not, don't clear it.
	if (this.collector == collector)
		this.collector = null;
}
/**
 * Creates a collector object to accumulate work and subtask calls.
 */
private void createCollector(String subTask, double work) {
	collector = new Collector(subTask, work, getWrappedProgressMonitor());
	display.asyncExec(collector);
}
/* (non-Javadoc)
 * Method declared on IProgressMonitor.
 */
public void done() {
	synchronized(this) {
		collector= null;
	}	
	display.syncExec(new Runnable() {
		public void run() {
			getWrappedProgressMonitor().done();
		}
	});
}
/* (non-Javadoc)
 * Method declared on IProgressMonitor.
 */
public synchronized void internalWorked(final double work) {
	if (collector == null) {
		createCollector(null, work);
	} else {
		collector.worked(work);
	}
}
/* (non-Javadoc)
 * Method declared on IProgressMonitor.
 */
public void setTaskName(final String name) {
	synchronized(this) {
		collector= null;
	}	
	display.syncExec(new Runnable() {
		public void run() {
			getWrappedProgressMonitor().setTaskName(name);
		}
	});
}
/* (non-Javadoc)
 * Method declared on IProgressMonitor.
 */
public synchronized void subTask(final String name) {
	if (collector == null) {
		createCollector(name, 0);
	} else {
		collector.subTask(name);
	}
}
/* (non-Javadoc)
 * Method declared on IProgressMonitor.
 */
public synchronized void worked(int work) {
	internalWorked(work);
}
}
