/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime;

import org.eclipse.core.internal.runtime.Assert;

/**
 * An abstract wrapper around a progress monitor which,
 * unless overridden, forwards <code>IProgressMonitor</code>
 * methods to the wrapped progress monitor.
 * <p>
 * Clients may subclass.
 * </p>
 */
public abstract class ProgressMonitorWrapper implements IProgressMonitor {

	/** The wrapped progress monitor. */
	private IProgressMonitor progressMonitor;

/** 
 * Creates a new wrapper around the given monitor.
 *
 * @param monitor the progress monitor to forward to
 */
protected ProgressMonitorWrapper(IProgressMonitor monitor) {
	Assert.isNotNull(monitor);
	progressMonitor = monitor;
}
/** 
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#beginTask
 */
public void beginTask(String name, int totalWork) {
	progressMonitor.beginTask(name, totalWork);
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#done
 */
public void done() {
	progressMonitor.done();
}
/**
 * Returns the wrapped progress monitor.
 *
 * @return the wrapped progress monitor
 */
public IProgressMonitor getWrappedProgressMonitor() {
	return progressMonitor;
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#internalWorked
 */
public void internalWorked(double work) {
	progressMonitor.internalWorked(work);
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#isCanceled
 */
public boolean isCanceled() {
	return progressMonitor.isCanceled();
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#setCanceled
 */
public void setCanceled(boolean b) {
	progressMonitor.setCanceled(b);
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#setTaskName
 */
public void setTaskName(String name) {
	progressMonitor.setTaskName(name);
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#subTask
 */
public void subTask(String name) {
	progressMonitor.subTask(name);
}
/**
 * This implementation of a <code>IProgressMonitor</code>
 * method forwards to the wrapped progress monitor.
 * Clients may override this method to do additional
 * processing.
 *
 * @see IProgressMonitor#worked
 */
public void worked(int work) {
	progressMonitor.worked(work);
}
}
