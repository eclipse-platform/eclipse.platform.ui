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
	 * @return
	 */
	public long getDelay() {
		return delay;
	}
	/**
	 * @return the job for this event
	 */
	public Job getJob() {
		return job;
	}
	/**
	 * @return
	 */
	public IStatus getResult() {
		return result;
	}
}
