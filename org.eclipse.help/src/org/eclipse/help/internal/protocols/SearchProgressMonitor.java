/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Progress monitor for search
 * @since 2.0
 */
public class SearchProgressMonitor implements IProgressMonitor {
	private boolean started, done, canceled;
	private int totalWork = IProgressMonitor.UNKNOWN;
	private int currWork;

	public SearchProgressMonitor() {
		started = done = canceled = false;
	}
	public void beginTask(String name, int totalWork) {
		this.totalWork = totalWork;
		this.started = true;
	}

	public void done() {
		currWork = totalWork;
		this.done = true;
		this.started = true;
	}

	public void setTaskName(String name) {
	}

	public void subTask(String name) {
	}

	public void worked(int work) {
		currWork += work;
		if (currWork > totalWork)
			currWork = totalWork;
		else if (currWork < 0)
			currWork = 0;
	}

	public void internalWorked(double work) {
	}

	public int getPercentage() {
		if (done) {
			return 100;
		}
		if (totalWork == IProgressMonitor.UNKNOWN)
			return 0;
		if (currWork >= totalWork)
			return 100;
		return (int) (100 * currWork / totalWork);
	}
	/**
	 * Gets the isCancelled.
	 * @return Returns a boolean
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Sets the isCanceled.
	 * @param isCancelled The isCanceled to set
	 */
	public void setCancelled(boolean canceled) {
		this.canceled = canceled;
	}

	/**
	 * Gets the isStarted.
	 * @return Returns a boolean
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * Gets the isDone.
	 * @return Returns a boolean
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Sets the isCanceled.
	 * @param isCanceled The isCanceled to set
	 */
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

}