/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.*;

/**
 * Internal implementation class for jobs.
 */
public abstract class InternalJob implements Comparable {
	private static final JobManager manager = JobManager.getInstance();
	private static int nextJobNumber = 0;

	private final int jobNumber = nextJobNumber++;
	private List listeners;
	private IProgressMonitor monitor;
	private int priority = Job.LONG;
	private ISchedulingRule schedulingRule;
	/**
	 * If the job is waiting, this represents the time the job should start by.  If
	 * this job is sleeping, this represents the time the job should wake up.
	 */
	private long startTime;
	private int state = Job.NONE;
	/**
	 * The job ahead of me in a queue or list.
	 */
	private InternalJob next;
	/**
	 * The job behind me in a queue or list.
	 */
	private InternalJob previous;
	/* (non-Javadoc)
	 * @see Job#addJobListener(IJobChangeListener)
	 */
	protected void addJobChangeListener(IJobChangeListener listener) {
		if (listeners == null)
			listeners = Collections.synchronizedList(new ArrayList(2));
		listeners.add(listener);
	}
	protected boolean cancel() {
		return manager.cancel((Job) this);
	}
	public final int compareTo(Object otherJob) {
		return (int) (((InternalJob) otherJob).startTime - startTime);
	}
	protected void done(IStatus result) {
		manager.endJob((Job)this, result);
	}
	/**
	 * Returns the job listeners that are only listening to this job.  Returns null
	 * if this job has no listeners.
	 */
	final List getListeners() {
		return listeners;
	}
	final IProgressMonitor getMonitor() {
		return monitor;
	}
	protected int getPriority() {
		return priority;
	}
	protected ISchedulingRule getRule() {
		return schedulingRule;
	}
	/*package*/
	final long getStartTime() {
		return startTime;
	}
	protected synchronized int getState() {
		return state;
	}
	void internalSetPriority(int newPriority) {
		this.priority = newPriority;
	}
	/**
	 * Returns true if this job conflicts with the given job, and false otherwise.
	 */
	boolean isConflicting(InternalJob otherJob) {
		ISchedulingRule myRule = schedulingRule;
		ISchedulingRule otherRule = otherJob.getRule();
		return myRule != null && otherRule != null && myRule.isConflicting(otherRule);
	}
	/* (non-Javadoc)
	 * @see Job#removeJobListener(IJobChangeListener)
	 */
	protected void removeJobChangeListener(IJobChangeListener listener) {
		if (listeners != null)
			listeners.remove(listener);
		if (listeners.isEmpty())
			listeners = null;
	}
	protected void schedule(long delay) {
		manager.schedule(this, delay);
	}
	final void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	protected void setPriority(int NewPriority) {
		manager.setPriority(this, NewPriority);
	}
	protected void setRule(ISchedulingRule rule) {
		schedulingRule = rule;
	}
	final void setStartTime(long time) {
		startTime = time;
	}
	final synchronized void setState(int i) {
		state = i;
	}
	protected boolean sleep() {
		return manager.sleep(this);
	}
	public String toString() {
		return getClass().getName() + "(" + jobNumber + ")"; //$NON-NLS-1$//$NON-NLS-2$
	}
	protected void wakeUp() {
		manager.wakeUp(this);
	}
	/**
	 * Adds an entry at the end of the list of which this item is the head.
	 */
	final void addLast(InternalJob entry) {
		if (previous == null) {
			previous = entry;
			entry.previous = null;
		} else
			previous.addLast(entry);
	}
	public boolean belongsTo(Object family) {
		return false;
	}
	/**
	 * Returns the next entry (ahead of this one) in the list, or null if there is no next entry
	 */
	final InternalJob next() {
		return next;
	}
	/**
	 * Returns the previous entry (behind this one) in the list, or null if there is no previous entry
	 */
	final InternalJob previous() {
		return previous;
	}
	final void setNext(InternalJob entry) {
		this.next = entry;
	}
	final void setPrevious(InternalJob entry) {
		this.previous = entry;
	}
	/**
	 * Removes this entry from any list it belongs to.  Returns the receiver.
	 */
	final InternalJob remove() {
		if (next != null)
			next.setPrevious(previous);
		if (previous != null)
			previous.setNext(next);
		next = null;
		previous = null;
		return this;
	}
}