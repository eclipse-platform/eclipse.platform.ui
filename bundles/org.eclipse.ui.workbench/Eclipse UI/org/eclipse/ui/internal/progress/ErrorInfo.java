/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

import com.ibm.icu.text.DateFormat;

/**
 * ErrorInfo is the info that displays errors.
 */
public class ErrorInfo extends JobTreeElement {

	private final IStatus errorStatus;

	private final Job job;

	private final long timestamp;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param status
	 * @param job
	 *            The Job to create
	 */
	public ErrorInfo(IStatus status, Job job) {
		errorStatus = status;
		this.job = job;
		timestamp = System.currentTimeMillis();
	}

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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
	 */
	Object[] getChildren() {
		return ProgressManagerUtil.EMPTY_OBJECT_ARRAY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
	 */
	String getDisplayString() {
		return NLS.bind(ProgressMessages.JobInfo_Error, (new Object[] {
				job.getName(),
				DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(new Date(timestamp)) }));
	}

	/**
	 * Return the image for the receiver.
	 * 
	 * @return Image
	 */
	Image getImage() {
		return JFaceResources.getImage(ProgressManager.ERROR_JOB_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
	 */
	boolean isJobInfo() {
		return false;
	}

	/**
	 * Return the current status of the receiver.
	 * 
	 * @return IStatus
	 */
	IStatus getErrorStatus() {
		return errorStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isActive()
	 */
	boolean isActive() {
		return true;
	}

	/**
	 * Return the job that generated the error.
	 * 
	 * @return the job that generated the error
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * Return the timestamp for the job.
	 * 
	 * @return long
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		if (arg0 instanceof ErrorInfo) {
			// Order ErrorInfo by time received
			long otherTimestamp = ((ErrorInfo) arg0).timestamp;
			if (timestamp < otherTimestamp) {
				return -1;
			} else if (timestamp > otherTimestamp) {
				return 1;
			} else {
				return 0;
			}
		}
		return super.compareTo(arg0);
	}
}
