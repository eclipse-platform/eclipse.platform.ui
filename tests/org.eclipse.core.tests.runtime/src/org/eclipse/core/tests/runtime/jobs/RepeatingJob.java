/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that runs the given number of times with a small delay between runs
 * Can report how many times it has run.
 */
public class RepeatingJob extends Job {
	final int repeats;
	int runCount = 0;
	private static final int DELAY = 20;
	private Object myFamily;

	public RepeatingJob(String name, int repeats) {
		super(name);
		this.repeats = repeats;
	}

	public boolean belongsTo(Object family) {
		return family == myFamily;
	}

	/**
	 * Returns the number of times this job has executed.
	 */
	public int getRunCount() {
		return runCount;
	}

	protected IStatus run(IProgressMonitor monitor) {
		schedule(DELAY);
		runCount++;
		return Status.OK_STATUS;
	}

	public void setFamily(Object family) {
		this.myFamily = family;
	}

	public boolean shouldRun() {
		return runCount < repeats;
	}
}