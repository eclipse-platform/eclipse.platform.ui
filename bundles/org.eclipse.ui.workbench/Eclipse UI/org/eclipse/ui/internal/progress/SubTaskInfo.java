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
 * SubTaskInfo is the class that displays a subtask in the 
 * tree.
 */
class SubTaskInfo extends JobTreeElement {

	protected String taskName;
	Job job;

	/**
	 * Create a new instance of the receiver.
	 * @param parentJob
	 * @param name
	 */
	SubTaskInfo(Job parentJob, String name) {
		taskName = name;
		job = parentJob;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
	 */
	Object[] getChildren() {
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
	 */
	String getDisplayString() {
		return taskName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#hasChildren()
	 */
	boolean hasChildren() {
		return false;
	}

	/**
	 * Set the taskName of the receiver.
	 * @param taskName
	 */
	void setTaskName(String name) {
		this.taskName = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getParent()
	 */
	Object getParent() {
		return job;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
	 */
	boolean isJobInfo() {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		return taskName.compareTo(((SubTaskInfo) arg0).taskName);
	}
}
