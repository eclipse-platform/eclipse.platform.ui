package org.eclipse.update.ui.internal.model;

import org.eclipse.core.runtime.IProgressMonitor;
import java.util.*;

public class BackgroundProgressMonitor implements IProgressMonitor {
	private Vector monitors=new Vector();
	private boolean canceled=false;
	private String taskName;
	private String subTaskName;
	private int totalWorked=0;
	private int taskCount;
	private boolean inProgress;
	
	public void addProgressMonitor(IProgressMonitor monitor) {
		if (monitors.contains(monitor)==false) {
		   monitors.add(monitor);
		   if (inProgress) {
		   	  // we are late - catch up
		      monitor.beginTask(taskName, taskCount);
		      monitor.worked(totalWorked);
		      if (subTaskName!=null)
		         monitor.subTask(subTaskName);
		   }
		}
	}
	
	public void removeProgressMonitor(IProgressMonitor monitor) {
		if (monitors.contains(monitor))
		   monitors.remove(monitor);
	}

	/**
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	public void beginTask(String taskName, int count) {
		totalWorked = 0;
		taskCount = count;
		this.taskName = taskName;
		subTaskName = null;
		for (Iterator iter=monitors.iterator(); iter.hasNext();) {
			IProgressMonitor m = (IProgressMonitor)iter.next();
			m.beginTask(taskName, count);
		}
	}

	/**
	 * @see IProgressMonitor#done()
	 */
	public void done() {
		for (Iterator iter=monitors.iterator(); iter.hasNext();) {
			IProgressMonitor m = (IProgressMonitor)iter.next();
			m.done();
		}
		inProgress = false;
	}

	/**
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double arg0) {
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
	public void setCanceled(boolean canceled) {
		for (Iterator iter=monitors.iterator(); iter.hasNext();) {
			IProgressMonitor m = (IProgressMonitor)iter.next();
			m.setCanceled(canceled);
		}
		this.canceled = canceled;
	}

	/**
	 * @see IProgressMonitor#setTaskName(String)
	 */
	public void setTaskName(String name) {
		for (Iterator iter=monitors.iterator(); iter.hasNext();) {
			IProgressMonitor m = (IProgressMonitor)iter.next();
			m.setTaskName(name);
		}
		this.taskName = name;
	}

	/**
	 * @see IProgressMonitor#subTask(String)
	 */
	public void subTask(String name) {
		subTaskName = name;
		for (Iterator iter=monitors.iterator(); iter.hasNext();) {
			IProgressMonitor m = (IProgressMonitor)iter.next();
			m.subTask(name);
		}
	}

	/**
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int amount) {
		totalWorked += amount;
		for (Iterator iter=monitors.iterator(); iter.hasNext();) {
			IProgressMonitor m = (IProgressMonitor)iter.next();
			m.worked(amount);
		}
	}

}

