/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Fraenkel <fraenkel@us.ibm.com> - Fix for bug 60698 -
 *     [Progress] ConcurrentModificationException in NewProgressViewer.
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * The GroupInfo is the object used to display group properties.
 */

class GroupInfo extends JobTreeElement implements IProgressMonitor {

    private List infos = new ArrayList();

    private Object lock = new Object();

    private String taskName;

    boolean isActive = false;

    double total = -1;

    double currentWork;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#getParent()
     */
    Object getParent() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#hasChildren()
     */
    boolean hasChildren() {
        synchronized (lock) {
            return !infos.isEmpty();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
     */
    Object[] getChildren() {
        synchronized (lock) {
            return infos.toArray();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
     */
    String getDisplayString() {
        if (total < 0) {
			return taskName;
		}
        String[] messageValues = new String[2];
        messageValues[0] = taskName;
        messageValues[1] = String.valueOf(getPercentDone());
        return NLS.bind(ProgressMessages.JobInfo_NoTaskNameDoneMessage, messageValues); 

    }

    /**
     * Return an integer representing the amount of work completed.
     * 
     * @return int
     */
    int getPercentDone() {
        return (int) (currentWork * 100 / total);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
     */
    boolean isJobInfo() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        return getDisplayString().compareTo(
                ((JobTreeElement) arg0).getDisplayString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
     *      int)
     */
    public void beginTask(String name, int totalWork) {
        taskName = name;
        total = totalWork;
        synchronized (lock) {
            isActive = true;
        }
        ProgressManager.getInstance().refreshGroup(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public void done() {
        synchronized (lock) {
            isActive = false;
        }
        ProgressManager.getInstance().removeGroup(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
     */
    public void internalWorked(double work) {
        synchronized (lock) {
            currentWork += work;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
     */
    public boolean isCanceled() {
        //Just a group so no cancel state
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
     */
    public void setCanceled(boolean value) {
        cancel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    public void setTaskName(String name) {
        synchronized (this) {
            isActive = true;
        }
        taskName = name;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(String name) {
        //Not interesting for this monitor
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    public void worked(int work) {
        internalWorked(work);
    }

    /**
     * Remove the job from the list of jobs.
     * 
     * @param job
     */
    void removeJobInfo(final JobInfo job) {
        synchronized (lock) {
            infos.remove(job);
            if (infos.isEmpty()) {
                done();
            }
        }
    }

    /**
     * Remove the job from the list of jobs.
     * 
     * @param job
     */
    void addJobInfo(final JobInfo job) {
        synchronized (lock) {
            infos.add(job);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#isActive()
     */
    boolean isActive() {
        return isActive;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#cancel()
     */
    public void cancel() {
        Object[] jobInfos = getChildren();
        for (int i = 0; i < jobInfos.length; i++) {
            ((JobInfo) jobInfos[i]).cancel();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.JobTreeElement#isCancellable()
     */
    public boolean isCancellable() {
        return true;
    }

    /**
     * Get the task name for the receiver.
     * @return String
     */
	String getTaskName() {
		return taskName;
	}

}
