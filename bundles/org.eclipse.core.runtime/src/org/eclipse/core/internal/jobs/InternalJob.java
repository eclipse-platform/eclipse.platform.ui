/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.internal.runtime.ListenerList;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;

/**
 * Internal implementation class for jobs. Clients must not implement this class
 * directly.  All jobs must be subclasses of the API <code>org.eclipse.core.runtime.jobs.Job</code> class.
 */
public abstract class InternalJob extends PlatformObject implements Comparable {
	/** 
	 * Job state code (value 16) indicating that a job has been removed from
	 * the wait queue and is about to start running. From an API point of view, 
	 * this is the same as RUNNING.
	 */
	static final int ABOUT_TO_RUN = 0x10;

	/** 
	 * Job state code (value 32) indicating that a job has passed scheduling
	 * precondition checks and is about to be added to the wait queue. From an API point of view, 
	 * this is the same as WAITING.
	 */
	static final int ABOUT_TO_SCHEDULE = 0x20;
	/** 
	 * Job state code (value 8) indicating that a job is blocked by another currently
	 * running job.  From an API point of view, this is the same as WAITING.
	 */
	static final int BLOCKED = 0x08;

	//flag mask bits
	private static final int M_STATE = 0xFF;
	private static final int M_SYSTEM = 0x0100;
	private static final int M_USER = 0x0200;

	private static final JobManager manager = JobManager.getInstance();
	private static int nextJobNumber = 0;

	/**
	 * Start time constant indicating a job should be started at
	 * a time in the infinite future, causing it to sleep forever.
	 */
	static final long T_INFINITE = Long.MAX_VALUE;
	/**
	 * Start time constant indicating that the job has no start time.
	 */
	static final long T_NONE = -1;

	private volatile int flags = Job.NONE;
	private final int jobNumber = nextJobNumber++;
	private ListenerList listeners = null;
	private IProgressMonitor monitor;
	private String name;
	/**
	 * The job ahead of me in a queue or list.
	 */
	private InternalJob next;
	/**
	 * The job behind me in a queue or list.
	 */
	private InternalJob previous;
	private int priority = Job.LONG;
	/**
	 * Arbitrary properties (key,value) pairs, attached
	 * to a job instance by a third party.
	 */
	private ObjectMap properties;
	private IStatus result;
	private ISchedulingRule schedulingRule;
	/**
	 * If the job is waiting, this represents the time the job should start by.  
	 * If this job is sleeping, this represents the time the job should wake up.
	 * If this job is running, this represents the delay automatic rescheduling,
	 * or -1 if the job should not be rescheduled.
	 */
	private long startTime;
	/*
	 * The thread that is currently running this job
	 */
	private volatile Thread thread = null;

	protected InternalJob(String name) {
		Assert.isNotNull(name);
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see Job#addJobListener(IJobChangeListener)
	 */
	protected void addJobChangeListener(IJobChangeListener listener) {
		if (listeners == null)
			listeners = new ListenerList(ListenerList.IDENTITY);
		listeners.add(listener);
	}

	/**
	 * Adds an entry at the end of the list of which this item is the head.
	 */
	final void addLast(InternalJob entry) {
		if (previous == null) {
			previous = entry;
			entry.next = this;
			entry.previous = null;
		} else {
			Assert.isTrue(previous.next() == this);
			previous.addLast(entry);
		}
	}

	/* (non-Javadoc)
	 * @see Job#belongsTo(Object)
	 */
	protected boolean belongsTo(Object family) {
		return false;
	}

	/* (non-Javadoc)
	 * @see Job#cancel()
	 */
	protected boolean cancel() {
		return manager.cancel(this);
	}

	/* (on-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public final int compareTo(Object otherJob) {
		return ((InternalJob) otherJob).startTime >= startTime ? 1 : -1;
	}

	/* (non-Javadoc)
	 * @see Job#done(IStatus)
	 */
	protected void done(IStatus endResult) {
		manager.endJob(this, endResult, true);
	}

	/**
	 * Returns the job listeners that are only listening to this job.  Returns 
	 * <code>null</code> if this job has no listeners.
	 */
	final ListenerList getListeners() {
		return listeners;
	}

	/* (non-Javadoc)
	 * @see Job#getName()
	 */
	protected String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see Job#getPriority()
	 */
	protected int getPriority() {
		return priority;
	}

	/**
	 * Returns the job's progress monitor, or null if it is not running.
	 */
	final IProgressMonitor getProgressMonitor() {
		return monitor;
	}

	/* (non-Javadoc)
	 * @see Job#getProperty
	 */
	protected Object getProperty(QualifiedName key) {
		// thread safety: (Concurrency001 - copy on write)
		Map temp = properties;
		if (temp == null)
			return null;
		return temp.get(key);
	}

	/* (non-Javadoc)
	 * @see Job#getResult
	 */
	protected IStatus getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see Job#getRule
	 */
	protected ISchedulingRule getRule() {
		return schedulingRule;
	}

	/**
	 * Returns the time that this job should be started, awakened, or
	 * rescheduled, depending on the current state.
	 * @return time in milliseconds
	 */
	final long getStartTime() {
		return startTime;
	}

	/* (non-Javadoc)
	 * @see Job#getState()
	 */
	protected int getState() {
		int state = flags & M_STATE;
		switch (state) {
			//blocked state is equivalent to waiting state for clients
			case BLOCKED :
				return Job.WAITING;
			case ABOUT_TO_RUN :
				return Job.RUNNING;
			case ABOUT_TO_SCHEDULE :
				return Job.WAITING;
			default :
				return state;
		}
	}

	/* (non-javadoc)
	 * @see Job.getThread
	 */
	protected Thread getThread() {
		return thread;
	}

	/**
	 * Returns the raw job state, including internal states no exposed as API.
	 */
	final int internalGetState() {
		return flags & M_STATE;
	}

	/**
	 * Must be called from JobManager#setPriority
	 */
	final void internalSetPriority(int newPriority) {
		this.priority = newPriority;
	}

	/**
	 * Must be called from JobManager#setRule
	 */
	final void internalSetRule(ISchedulingRule rule) {
		this.schedulingRule = rule;
	}

	/**
	 * Must be called from JobManager#changeState
	 */
	final void internalSetState(int i) {
		flags = (flags & ~M_STATE) | i;
	}

	/* (non-Javadoc)
	 * @see Job#isBlocking()
	 */
	protected boolean isBlocking() {
		return manager.isBlocking(this);
	}

	/**
	 * Returns true if this job conflicts with the given job, and false otherwise.
	 */
	final boolean isConflicting(InternalJob otherJob) {
		ISchedulingRule otherRule = otherJob.getRule();
		if (schedulingRule == null || otherRule == null)
			return false;
		//if one of the rules is a compound rule, it must be asked the question.
		if (schedulingRule.getClass() == MultiRule.class)
			return schedulingRule.isConflicting(otherRule);
		return otherRule.isConflicting(schedulingRule);
	}

	/* (non-javadoc)
	 * @see Job.isSystem()
	 */
	protected boolean isSystem() {
		return (flags & M_SYSTEM) != 0;
	}

	/* (non-javadoc)
	 * @see Job.isUser()
	 */
	protected boolean isUser() {
		return (flags & M_USER) != 0;
	}

	/* (non-Javadoc)
	 * @see Job#join()
	 */
	protected void join() throws InterruptedException {
		manager.join(this);
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

	/**
	 * Removes this entry from any list it belongs to.  Returns the receiver.
	 */
	final InternalJob remove() {
		if (next != null)
			next.setPrevious(previous);
		if (previous != null)
			previous.setNext(next);
		next = previous = null;
		return this;
	}

	/* (non-Javadoc)
	 * @see Job#removeJobListener(IJobChangeListener)
	 */
	protected void removeJobChangeListener(IJobChangeListener listener) {
		if (listeners != null)
			listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see Job#run(IProgressMonitor)
	 */
	protected abstract IStatus run(IProgressMonitor progressMonitor);

	/* (non-Javadoc)
	 * @see Job#schedule(long)
	 */
	protected void schedule(long delay) {
		if (shouldSchedule())
			manager.schedule(this, delay, false);
	}

	/* (non-Javadoc)
	 * @see Job#setName(String)
	 */
	protected void setName(String name) {
		Assert.isNotNull(name);
		this.name = name;
	}

	/**
	 * Sets the next entry in this linked list of jobs.
	 * @param entry
	 */
	final void setNext(InternalJob entry) {
		this.next = entry;
	}

	/**
	 * Sets the previous entry in this linked list of jobs.
	 * @param entry
	 */
	final void setPrevious(InternalJob entry) {
		this.previous = entry;
	}

	/* (non-Javadoc)
	 * @see Job#setPriority(int)
	 */
	protected void setPriority(int newPriority) {
		switch (newPriority) {
			case Job.INTERACTIVE :
			case Job.SHORT :
			case Job.LONG :
			case Job.BUILD :
			case Job.DECORATE :
				manager.setPriority(this, newPriority);
				break;
			default :
				throw new IllegalArgumentException(String.valueOf(newPriority));
		}
	}

	/* (non-Javadoc)
	 * @see Job#setProgressGroup(IProgressMonitor, int)
	 */
	protected void setProgressGroup(IProgressMonitor group, int ticks) {
		Assert.isNotNull(group);
		IProgressMonitor pm = manager.createMonitor(this, group, ticks);
		if (pm != null)
			setProgressMonitor(pm);
	}

	/**
	 * Sets the progress monitor to use for the next execution of this job,
	 * or for clearing the monitor when a job completes.
	 * @param monitor a progress monitor
	 */
	final void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/* (non-Javadoc)
	 * @see Job#setProperty(QualifiedName,Object)
	 */
	protected void setProperty(QualifiedName key, Object value) {
		// thread safety: (Concurrency001 - copy on write)
		if (value == null) {
			if (properties == null)
				return;
			ObjectMap temp = (ObjectMap) properties.clone();
			temp.remove(key);
			if (temp.isEmpty())
				properties = null;
			else
				properties = temp;
		} else {
			ObjectMap temp = properties;
			if (temp == null)
				temp = new ObjectMap(5);
			else
				temp = (ObjectMap) properties.clone();
			temp.put(key, value);
			properties = temp;
		}
	}

	/**
	 * Sets or clears the result of an execution of this job.
	 * @param result a result status, or <code>null</code>
	 */
	final void setResult(IStatus result) {
		this.result = result;
	}

	/* (non-Javadoc)
	 * @see Job#setRule(ISchedulingRule)
	 */
	protected void setRule(ISchedulingRule rule) {
		manager.setRule(this, rule);
	}

	/**
	 * Sets a time to start, wake up, or schedule this job, 
	 * depending on the current state
	 * @param time a time in milliseconds
	 */
	final void setStartTime(long time) {
		startTime = time;
	}

	/* (non-javadoc)
	 * @see Job.setSystem
	 */
	protected void setSystem(boolean value) {
		if (getState() != Job.NONE)
			throw new IllegalStateException();
		flags = value ? flags | M_SYSTEM : flags & ~M_SYSTEM;
	}

	/* (non-javadoc)
	 * @see Job.setThread
	 */
	protected void setThread(Thread thread) {
		this.thread = thread;
	}

	/* (non-javadoc)
	 * @see Job.setUser
	 */
	protected void setUser(boolean value) {
		if (getState() != Job.NONE)
			throw new IllegalStateException();
		flags = value ? flags | M_USER : flags & ~M_USER;
	}

	/* (Non-javadoc)
	 * @see Job#shouldSchedule
	 */
	protected boolean shouldSchedule() {
		return true;
	}

	/* (non-Javadoc)
	 * @see Job#sleep()
	 */
	protected boolean sleep() {
		return manager.sleep(this);
	}

	/* (non-Javadoc)
	 * Prints a string-based representation of this job instance. 
	 * For debugging purposes only.
	 */
	public String toString() {
		return getName() + "(" + jobNumber + ")"; //$NON-NLS-1$//$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see Job#wakeUp(long)
	 */
	protected void wakeUp(long delay) {
		manager.wakeUp(this, delay);
	}
}
