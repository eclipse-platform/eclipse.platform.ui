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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The GroupInfo is the object used to display group properties.
 */

class GroupInfo extends JobTreeElement implements IProgressMonitor {

	private List infos = new ArrayList();
	private Object lock = new Object();
	private String taskName;
	boolean isActive = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getParent()
	 */
	Object getParent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#hasChildren()
	 */
	boolean hasChildren() {
		synchronized (lock) {
			return !infos.isEmpty();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
	 */
	Object[] getChildren() {
		synchronized (lock) {
			return infos.toArray();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
	 */
	String getDisplayString() {
		return taskName;
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
		return getDisplayString().compareTo(((JobTreeElement) arg0).getDisplayString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int totalWork) {
		taskName = name;
		synchronized(this){
			isActive = true;
		}
		ProgressManager.getInstance().addGroup(this);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		synchronized(this){
			isActive = false;
		}
		ProgressManager.getInstance().removeGroup(this);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		//No behavior as this is a display item
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		//Just a group so no cancel state
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		//Just a group so no cancel state
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		synchronized(this){
			isActive = true;
		}
		taskName = name;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(String name) {
		//Not interesting for this monitor
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		//Not interesting for this monitor
	}

	/**
	 * Remove the job from the list of jobs.
	 * @param job
	 */
	void removeJobInfo(final JobInfo job) {
		synchronized (lock) {
			infos.remove(job);
		}
	}
	
	/**
	 * Remove the job from the list of jobs.
	 * @param job
	 */
	void addJobInfo(final JobInfo job) {
		synchronized (lock) {
			infos.add(job);
		}
	}
	/**
	 * @return Returns whether or not there is
	 * an active task.
	 */
	public boolean isActive() {
		return isActive;
	}

}
