/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * The progress provider supplies the job manager with progress monitors for
 * running jobs.  There can only be one progress provider at any given time.
 * <p>
 * This class is for internal use by the platform-related plug-ins.
 * Clients outside of the base platform should not reference or subclass this class.
 * </p>
 * 
 * @see IJobManager#setProgressProvider
 * @since 3.0
 */
public abstract class ProgressProvider {
	/**
	 * Provides a new progress monitor instance to be used by the given job.
	 * @param job the job to create a progress monitor for
	 * @return a progress monitor, or <code>null</code> if no progress monitoring 
	 * is needed.
	 */
	public abstract IProgressMonitor createMonitor(Job job);
	/**
	 * Returns a progress monitor to use when none has been provided
	 * by the client running the job.  
	 * <p>
	 * This default implementation returns a new
	 * <code>NullProgressMonitor</code>  Subclasses may override.
	 * 
	 * @return a progress monitor
	 */
	public IProgressMonitor getDefaultMonitor() {
		return new NullProgressMonitor();
	}
}