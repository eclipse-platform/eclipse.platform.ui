/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Represents an execution of a search query. This interface it supposed
 * to be implemented by clients.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ISearchJob {
	/**
	 * run the search job
	 * @param monitor The progress monitor to be used
	 * @return The status after completion of the search job.
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor);
	 */
	IStatus run(IProgressMonitor monitor);
	/**
	 * Returns the name of this search job. This will be used,
	 * for example to set the <code>Job</code> name if this search
	 * job is executed in the background.
	 * @return
	 */
	String getName();
}
