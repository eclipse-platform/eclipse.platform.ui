/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Distributes progress information from this monitor
 * to multiple monitors.
 */
public class ProgressDirstributor implements IProgressMonitor {
	private int totalWork = -1;
	private int worked = 0;
	private boolean done = false;
	private boolean canceled = false;
	String taskName;
	String subTaskName;
	/**
	 * Map work indexed by montitor
	 */
	private Collection monitors = new ArrayList();

	/**
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public synchronized void beginTask(String name, int totalWork) {
		this.totalWork = totalWork;
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			IProgressMonitor m = (IProgressMonitor) it.next();
			m.beginTask(name, totalWork);
		}
	}

	/**
	 * @see IProgressMonitor#done()
	 */
	public synchronized void done() {
		done = true;
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			IProgressMonitor m = (IProgressMonitor) it.next();
			m.done();
		}
	}

	/**
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
	}

	/**
	 * @see IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public synchronized void setCanceled(boolean value) {
		canceled = value;
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			IProgressMonitor m = (IProgressMonitor) it.next();
			m.setCanceled(value);
		}
	}

	/**
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public synchronized void setTaskName(String name) {
		taskName = name;
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			IProgressMonitor m = (IProgressMonitor) it.next();
			m.setTaskName(name);
		}
	}

	/**
	 * @see IProgressMonitor#subTask(String)
	 */
	public synchronized void subTask(String name) {
		subTaskName = name;
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			IProgressMonitor m = (IProgressMonitor) it.next();
			m.subTask(name);
		}
	}

	/**
	 * @see IProgressMonitor#worked(int)
	 */
	public synchronized void worked(int work) {
		worked += work;
		for (Iterator it = monitors.iterator(); it.hasNext();) {
			IProgressMonitor m = (IProgressMonitor) it.next();
			m.worked(work);
		}
	}
	public synchronized void addMonitor(IProgressMonitor m) {
		if (totalWork > -1)
			m.beginTask(taskName, totalWork);
		if (subTaskName != null)
			m.subTask(subTaskName);
		if (worked > 0)
			m.worked(worked);
		m.setCanceled(canceled);
		if (done)
			m.done();
		monitors.add(m);
	}
	public synchronized void removeMonitor(IProgressMonitor m) {
		monitors.remove(m);
	}
}