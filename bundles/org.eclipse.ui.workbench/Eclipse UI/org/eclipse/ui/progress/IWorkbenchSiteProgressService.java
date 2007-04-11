/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * IWorkbenchPartProgressService is an IProgressService that adds API for 
 * jobs that change the state in a IWorkbenchPartSite while they are being 
 * run.
 * 
 * WorkbenchParts may access an instance of IWorkbenchSiteProgressService
 * by calling
 * <code>getSite.getAdapter(IWorkbenchSiteProgressService.class);</code>
 * 
 * This interface is not intended to be implemented by client
 * plug-ins.
 * @see IWorkbenchPartSite#getAdapter(Class)
 * @since 3.0
 */
public interface IWorkbenchSiteProgressService extends IProgressService {

    /**
     * The property that is sent with busy notifications.
     */
    public static final String BUSY_PROPERTY = "SITE_BUSY"; //$NON-NLS-1$

    /**
     * Jobs scheduled with this method will cause the part's presentation 
     * to be changed to indicate that the part is busy and in a transient 
     * state until the job completes. Parts can also add customized busy 
     * indication by overriding <code>WorkbenchPart.setBusy()</code>.
     * If useHalfBusyCursor is true then the cursor will change to
     * the half busy cursor for the duration of the job.
     * @param job The job to schedule
     * @param delay The delay in scheduling.
     * @param useHalfBusyCursor A boolean to indicate if the half busy
     * 		cursor should be used while this job is running.
     * @see Job#schedule(long)
     */
    public void schedule(Job job, long delay, boolean useHalfBusyCursor);

    /**
     * Jobs scheduled with this method will cause the part's presentation 
     * to be changed to indicate that the part is busy and in a transient 
     * state until the job completes. Parts can also add customized busy 
     * indication by overriding <code>WorkbenchPart.setBusy</code>.
     * @param job The job to schedule
     * @param delay The delay in scheduling.
     * @see Job#schedule(long)
     */
    public void schedule(Job job, long delay);

    /**
     * Jobs scheduled with this method will cause the part's presentation 
     * to be changed to indicate that the part is busy and in a transient 
     * state until the job completes. Parts can also add customized busy 
     * indication by overriding <code>WorkbenchPart.setBusy</code>.
     * @param job The job to schedule
     * @see Job#schedule()
     */
    public void schedule(Job job);

    /**
     * Show busy state if any job of the specified family is running.
     * @param family Object
     * @see Job#belongsTo(Object)
     */
    public void showBusyForFamily(Object family);

    /**
     * Warn that the content of the receiver has 
     * changed. The method of this is determined by
     * how the presentation shows this. 
     * @see IPresentablePart#PROP_HIGHLIGHT_IF_BACK
     */
    public void warnOfContentChange();
    
    /**
	 * Increments the busy counter for this workbench site. This API should only
	 * be used for background work that does not use jobs. As long as there have
	 * been more calls to incrementBusy() than to decrementBusy(), the part will
	 * show a busy affordance. Each call to incrementBusy must be followed by a
	 * call to decrementBusy once the caller no longer needs the part to show
	 * the busy affordance.
	 * <p>
	 * Note that the job-related methods on this class are another way to let
	 * the part show a busy affordance.  A part will only appear non-busy if no
	 * jobs have been scheduled through this service, and the internal busy
	 * counter is not positive.
	 * </p>
	 * 
	 * @since 3.3
	 */
	public void incrementBusy();

    /**
	 * Decrements the busy counter for this workbench site. This API should only
	 * be used for background work that does not use jobs. It is an error to call
	 * this method without first making a matching call to {@link #incrementBusy()}.
	 * 
	 * @since 3.3
	 */
	public void decrementBusy();
	
}
