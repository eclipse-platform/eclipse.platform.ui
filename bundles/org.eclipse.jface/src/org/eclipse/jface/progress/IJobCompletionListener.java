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

package org.eclipse.jface.progress;

import org.eclipse.core.runtime.IStatus;

/**
 * IJobCompletionListener is a class that listens to the result
 * of a job.
 */
public interface IJobCompletionListener {

	/**
	 * The job has finished with a result of status.
	 * @param status
		 */
	public void finished(IStatus status);

	/**
	 * The job never ran for a reason indicated by status.
	 * @param status
	 */
	public void aborted(IStatus status);

}
