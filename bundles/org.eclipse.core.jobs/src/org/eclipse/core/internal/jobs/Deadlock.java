/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * The deadlock class stores information about a deadlock that just occurred.
 * It contains an array of the threads that were involved in the deadlock
 * as well as the thread that was chosen to be suspended and an array of locks
 * held by that thread that are going to be suspended to resolve the deadlock.
 */
class Deadlock {
	//all the threads which are involved in the deadlock
	private Thread[] threads;
	//the thread whose locks will be suspended to resolve deadlock
	private Thread candidate;
	//the locks that will be suspended
	private ISchedulingRule[] locks;

	public Deadlock(Thread[] threads, ISchedulingRule[] locks, Thread candidate) {
		this.threads = threads;
		this.locks = locks;
		this.candidate = candidate;
	}

	public ISchedulingRule[] getLocks() {
		return locks;
	}

	public Thread getCandidate() {
		return candidate;
	}

	public Thread[] getThreads() {
		return threads;
	}
}
