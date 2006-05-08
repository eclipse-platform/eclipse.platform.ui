/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

/**
 * SubTaskInfo is the class that displays a subtask in the 
 * tree.
 */
class SubTaskInfo extends JobTreeElement {

    protected String taskName;

    JobInfo jobInfo;

    /**
     * Create a new instance of the receiver.
     * @param parentJob
     * @param name
     */
    SubTaskInfo(JobInfo parentJob, String name) {
        taskName = name;
        jobInfo = parentJob;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
     */
    Object[] getChildren() {
        return ProgressManagerUtil.EMPTY_OBJECT_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
     */
    String getDisplayString() {
        if (taskName == null) {
			return ProgressMessages.SubTaskInfo_UndefinedTaskName;
		}
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

    /**
     * Returns the taskName of the receiver.
     */
    String getTaskName() {
        return taskName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.JobTreeElement#getParent()
     */
    Object getParent() {
        return jobInfo;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
     */
    boolean isJobInfo() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.JobTreeElement#isActive()
     */
    boolean isActive() {
        return jobInfo.isActive();
    }
}
