/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * An event describing a change to the state of a job.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see IJobChangeListener
 */
public interface IJobChangeEvent {
	/**
	 * The amount of time in milliseconds to wait after scheduling the job before it 
	 * should be run, or <code>-1</code> if not applicable for this type of event.  
	 * This value is only applicable for the <code>scheduled</code> event.
	 */
	public long getDelay();
	/**
	 * The job on which this event occurred.
	 */
	public Job getJob();
	/**
	 * The result returned by the job's run method, or <code>null</code> if
	 * not applicable.  This value is only applicable for the <code>done</code> event.
	 */
	public IStatus getResult();
}