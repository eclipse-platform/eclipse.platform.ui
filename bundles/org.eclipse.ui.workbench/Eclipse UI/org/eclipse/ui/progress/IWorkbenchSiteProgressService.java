/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others. All rights reserved.   This
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
 * jobs that change the state in a PartSite while they are being 
 * run.
 * 
 * WorkbenchParts may access an instance of IWorkbenchSiteProgressService
 * by calling
 * <code>getSite.getAdapter(IWorkbenchSiteProgressService.class);</code>
 * 
 * This interface is not intended to be implemented by client
 * plug-ins.
 * 
 * @see WorkbenchPart.getJobChangeListener()
 * @since 3.0
 */
public interface IWorkbenchSiteProgressService extends IProgressService {
	
	/**
	 * Jobs scheduled with this method will cause the part's presentation 
	 * to be changed to indicate that the part is busy and in a transient 
	 * state until the job completes. Parts can also add customized busy 
	 * indication by overriding <code>WorkbenchPart.getJobChangeListener</code>.
	 * @param job. The job to schedule
	 * @param delay. The delay in scheduling.
	 * @see Job.schedule(long)
	 */
	public void schedule(Job job, long delay);
	
	/**
	 * Jobs scheduled with this method will cause the part's presentation 
	 * to be changed to indicate that the part is busy and in a transient 
	 * state until the job completes. Parts can also add customized busy 
	 * indication by overriding <code>WorkbenchPart.getJobChangeListener</code>.
	 * @param job. The job to schedule
	 * @see Job.schedule()
	 */
	public void schedule(Job job);
	
	/**
	 * Use the half busy cursor in this part during the execution
	 * of this job.
	 * @param job
	 */
	public void useHalfBusyCursor(Job job);

}
