/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

/**
 * The IJobProgressManagerListener is a class that listeners to the JobProgressManager.
 */
interface IJobProgressManagerListener {

	/**
	 * Refresh the viewer as a result of an addition of info.
	 */
	void addJob(final JobInfo info);

	/**
	 * Refresh the viewer as a result of an addition of group.
	 */
	void addGroup(final GroupInfo info);

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in info.
	 */
	public void refreshJobInfo(JobInfo info);

	/**
	 * Refresh the IJobProgressManagerListeners as a result of a change in groups.
	 */
	public void refreshGroup(GroupInfo info);

	/**
	 * Refresh the viewer for all jobs.
	 */
	void refreshAll();

	/**
	 * Refresh the viewer as a result of a removal of info.
	 */
	void removeJob(final JobInfo info);

	/**
	 * Refresh the viewer as a result of a removal of group.
	 */
	void removeGroup(final GroupInfo group);

	/**
	 * Return whether or not this listener shows debug information.
	 * @return boolean
	 */
	boolean showsDebug();
}
