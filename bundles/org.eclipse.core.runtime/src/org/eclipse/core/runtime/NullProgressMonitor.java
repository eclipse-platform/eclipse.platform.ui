package org.eclipse.core.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A default progress monitor implementation suitable for
 * subclassing.
 * <p>
 * This implementation supports cancelation. The default
 * implementations of the other methods do nothing.
 * </p>
 */
public class NullProgressMonitor implements IProgressMonitor {

	/**
	 * Indicates whether cancel has been requested.
	 */
	private boolean cancelled = false;
/**
 * Constructs a new progress monitor.
 */
public NullProgressMonitor() {
}
/**
 * This implementation does nothing. 
 * Subclasses may override this method to do interesting
 * processing when a task begins.
 * 
 * @see IProgressMonitor#beginTask
 */
public void beginTask(String name, int totalWork) {
}
/**
 * This implementation does nothing.
 * Subclasses may override this method to do interesting
 * processing when a task is done.
 * 
 * @see IProgressMonitor#done
 */
public void done() {
}
/**
 * This implementation does nothing.
 * Subclasses may override this method.
 * 
 * @see IProgressMonitor#internalWorked
 */
public void internalWorked(double work) {
}
/**
 * This implementation returns the value of the internal 
 * state variable set by <code>setCanceled</code>.
 * Subclasses which override this method should
 * override <code>setCanceled</code> as well.
 *
 * @see IProgressMonitor#isCanceled
 * @see IProgressMonitor#setCanceled
 */
public boolean isCanceled() {
	return cancelled;
}
/**
 * This implementation sets the value of an internal state variable.
 * Subclasses which override this method should override 
 * <code>isCanceled</code> as well.
 *
 * @see IProgressMonitor#isCanceled
 * @see IProgressMonitor#setCanceled
 */
public void setCanceled(boolean cancelled) {
	this.cancelled = cancelled;
}
/**
 * This implementation does nothing.
 * Subclasses may override this method to do something
 * with the name of the task.
 * 
 * @see IProgressMonitor#setTaskName
 */
public void setTaskName(String name) {
}
/**
 * This implementation does nothing.
 * Subclasses may override this method to do interesting
 * processing when a subtask begins.
 * 
 * @see IProgressMonitor#subTask
 */
public void subTask(String name) {
}
/**
 * This implementation does nothing.
 * Subclasses may override this method to do interesting
 * processing when some work has been completed.
 * 
 * @see IProgressMonitor#worked
 */
public void worked(int work) {
}
}
