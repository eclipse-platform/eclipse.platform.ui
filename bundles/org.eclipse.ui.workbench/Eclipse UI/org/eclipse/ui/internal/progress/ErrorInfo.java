/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.JFaceResources;

/**
 * ErrorInfo is the info that displays errors.
 */
public class ErrorInfo extends JobTreeElement {
	
	private IStatus errorStatus;
	private String jobName;

	/**
	 * Create a new instance of the receiver.
	 * @param status
	 */
	public ErrorInfo(IStatus status, String name) {
		errorStatus = status;
		jobName = name;
	}

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
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
	 */
	Object[] getChildren() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
	 */
	String getDisplayString() {
		return ProgressMessages.format("JobInfo.Error", //$NON-NLS-1$
		new Object[] { jobName, errorStatus.getMessage()});
	}

	/**
	 * Return the image for the receiver.
	 * @return Image
	 */
	Image getImage() {
		return JFaceResources.getImage(ErrorNotificationManager.ERROR_JOB_KEY);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
	 */
	boolean isJobInfo() {
		return false;
	}


	/**
	 * Return the current status of the receiver. 
	 * @return IStatus
	 */
	IStatus getErrorStatus() {
		return errorStatus;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isActive()
	 */
	boolean isActive() {
		return true;
	}
}
