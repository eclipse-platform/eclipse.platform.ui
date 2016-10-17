/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import com.ibm.icu.text.DateFormat;
import java.util.Date;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

/**
 * ErrorInfo is the info that displays errors.
 */
public class ErrorInfo extends JobTreeElement {
	private final IStatus errorStatus;
	private final Job job;
	private final long timestamp;

	/**
	 * Creates a new instance of ErrorInfo.
	 *
	 * @param status
	 * @param job
	 *            the Job to create
	 */
	public ErrorInfo(IStatus status, Job job) {
		errorStatus = status;
		this.job = job;
		timestamp = System.currentTimeMillis();
	}

	@Override
	boolean hasChildren() {
		return false;
	}

	@Override
	Object[] getChildren() {
		return ProgressManagerUtil.EMPTY_OBJECT_ARRAY;
	}

	@Override
	String getDisplayString() {
		return NLS.bind(ProgressMessages.JobInfo_Error, (new Object[] {
				job.getName(),
				DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG).format(new Date(timestamp)) }));
	}

	/**
	 * Returns the image for the receiver.
	 *
	 * @return Image
	 */
	Image getImage() {
		return JFaceResources.getImage(ProgressManager.ERROR_JOB_KEY);
	}

	@Override
	boolean isJobInfo() {
		return false;
	}

	/**
	 * Returns the current status of the receiver.
	 *
	 * @return IStatus
	 */
	IStatus getErrorStatus() {
		return errorStatus;
	}

	@Override
	boolean isActive() {
		return true;
	}

	/**
	 * Returns the job that generated the error.
	 *
	 * @return the job that generated the error
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * Returns the timestamp for the job.
	 *
	 * @return long
	 */
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int compareTo(JobTreeElement other) {
		if (other instanceof ErrorInfo) {
			// Order ErrorInfo by time received.
			return Long.compare(timestamp, ((ErrorInfo) other).timestamp);
		}

		return super.compareTo(other);
	}
}
