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
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.swt.widgets.Display;

/**
 * Used to run an event loop whenever progress monitor methods
 * are invoked.  <p>
 * This is needed since editor save operations are done in the UI thread.  
 * Although save operations should be written to do the work in the non-UI thread, 
 * this was not done for 1.0, so this was added to keep the UI live
 * (including allowing the cancel button to work).
 */
public class EventLoopProgressMonitor extends ProgressMonitorWrapper {
	
	/**
	 * Threshold for how often the event loop is spun, in ms.
	 */
	private static int T_THRESH = 100;
	
	/**
	 * Maximum amount of time to spend processing events, in ms.
	 */
	private static int T_MAX = 50;
	
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
	for (;;) {
		if (!disp.readAndDispatch()) {	// Exceptions walk back to parent.
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
