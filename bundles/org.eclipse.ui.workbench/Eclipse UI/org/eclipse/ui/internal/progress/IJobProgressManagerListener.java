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
package org.eclipse.ui.internal.progress;

/**
 * The IJobProgressManagerListener is a class that listeners to the JobProgressManager.
 */
interface IJobProgressManagerListener {

	/**
	 * Refresh the viewer as a result of an addition of info.
	 * @param info
	 */
	void add(final JobInfo info);

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in info.
	 * @param info
	 */
	public void refresh(JobInfo info);

	/**
	 * Refresh the viewer for all jobs.
	 * @param info
	 */
	void refreshAll();

	/**
	 * Refresh the viewer as a result of a removal of info.
	 * @param info
	 */
	void remove(final JobInfo info);
}
