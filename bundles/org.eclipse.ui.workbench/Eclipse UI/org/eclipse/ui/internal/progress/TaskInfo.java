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

import org.eclipse.core.runtime.jobs.Job;

/**
 * The TaskInfo is the info on a task with a job. It is 
 * assumed that there is only one task running at a time -
 * any previous tasks in a Job will be deleted.
 */
public class TaskInfo extends SubTaskInfo{
	double preWork = 0;
	int totalWork = 0;

	

	/**
	 * Create a new instance of the receiver with the supplied total
	 * work and task name.
	 * @param parentJob
	 * @param taskName
	 * @param total
	 */
	TaskInfo(Job parentJob, String taskName, int total) {	
		super(parentJob,taskName);
		totalWork = total;
	}

	/**
	 * Add the work increment to the total.
	 * @param workIncrement
	 */
	void addWork(double workIncrement) {
		preWork += workIncrement;

	}

	/**
	 * Get the display string for the task.
	 */
	String getDisplayString() {
		int done = (int) (preWork * 100 / totalWork);
		String[] messageValues = new String[3];
		messageValues[0] = job.getName();
		messageValues[1] = taskName;
		messageValues[2] = String.valueOf(done);
		return ProgressMessages.format("JobInfo.DoneMessage",messageValues); //$NON-NLS-1$
		
	}

}
