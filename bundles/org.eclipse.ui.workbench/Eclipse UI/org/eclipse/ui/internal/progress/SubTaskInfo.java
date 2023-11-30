/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.progress;

/**
 * SubTaskInfo is the class that displays a subtask in the tree.
 */
class SubTaskInfo extends JobTreeElement {
	protected String taskName;
	protected final JobInfo jobInfo;

	/**
	 * Creates a new instance of the receiver.
	 */
	SubTaskInfo(JobInfo parentJob, String name) {
		taskName = name;
		jobInfo = parentJob;
	}

	@Override
	Object[] getChildren() {
		return ProgressManagerUtil.EMPTY_OBJECT_ARRAY;
	}

	@Override
	String getDisplayString() {
		if (taskName == null) {
			return ProgressMessages.SubTaskInfo_UndefinedTaskName;
		}
		return taskName;
	}

	@Override
	boolean hasChildren() {
		return false;
	}

	/**
	 * Sets the taskName of the receiver.
	 */
	void setTaskName(String name) {
		if (name == null)
			taskName = ProgressMessages.SubTaskInfo_UndefinedTaskName;
		else
			this.taskName = name;
	}

	/**
	 * Returns the taskName of the receiver.
	 */
	String getTaskName() {
		return taskName;
	}

	@Override
	public JobInfo getParent() {
		return jobInfo;
	}

	@Override
	boolean isJobInfo() {
		return false;
	}

	@Override
	boolean isActive() {
		return jobInfo.isActive();
	}
}
