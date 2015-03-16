/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.IStatus;

/**
 * Represents status relating to the execution of jobs.
 *
 * @see org.eclipse.core.runtime.IStatus
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IJobStatus extends IStatus {
	/**
	 * Returns the job associated with this status.
	 *
	 * @return the job associated with this status
	 */
	public Job getJob();
}
