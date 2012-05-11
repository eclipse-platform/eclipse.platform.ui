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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;

public class JobChangeEvent implements IJobChangeEvent {
	/**
	 * The job on which this event occurred.
	 */
	Job job = null;
	/**
	 * The result returned by the job's run method, or <code>null</code> if
	 * not applicable.
	 */
	IStatus result = null;
	/**
	 * The amount of time to wait after scheduling the job before it should be run,
	 * or <code>-1</code> if not applicable for this type of event.
	 */
	long delay = -1;
	/**
	 * Whether this job is being immediately rescheduled.
	 */
	boolean reschedule = false;

	/* (non-Javadoc)
	 * Method declared on IJobChangeEvent
	 */
	public long getDelay() {
		return delay;
	}

	/* (non-Javadoc)
	 * Method declared on IJobChangeEvent
	 */
	public Job getJob() {
		return job;
	}

	/* (non-Javadoc)
	 * Method declared on IJobChangeEvent
	 */
	public IStatus getResult() {
		return result;
	}
}
