package org.eclipse.ui.internal.dialogs;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.*;

/**
 * Used to run an event loop whenever progress monitor methods
 * are invoked. 
 */
public class EventLoopProgressMonitor extends ProgressMonitorWrapper {
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
	Display disp = Display.getDefault();
	if (disp == null)
		return;
	boolean run = true;
	while (run) {
		run = disp.readAndDispatch();	// Exceptions walk back to parent.
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
