/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.jobs.Job;

/**
 * IWorkbenchPartProgressService is an IProgressService that adds API for 
 * context sensitive jobs.
 * @since 3.0
 */
public interface IWorkbenchSiteProgressService extends IProgressService {
	
	/**
	 * Schedule the job in the progress service. Ask the pane and the view
	 * part for job listeners and add them if required.
	 * @param job. The job to schedule
	 * @param delay. The delay in scheduling.
	 * @see Job.schedule(long)
	 */
	public void schedule(Job job, long delay);
	
	/**
	 * Schedule the job in the progress service. Ask the pane and the view
	 * part for job listeners and add them if required.
	 * @param job. The job to schedule
	 * @see Job.schedule()
	 */
	public void schedule(Job job);

}
