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

import org.eclipse.swt.widgets.ProgressBar;

/**
 * The JobInfoWithProgress is a JobInfo that also keeps track of progress.
 */
public class JobInfoWithProgress extends JobInfo {
	double preWork = 0;
	int totalWork = 0;
	ProgressBar indicator;

	/**
	 * Create a new instance of the receiver with the supplied total
	 * work.
	 * @param taskName
	 * @param total
	 */
	JobInfoWithProgress(String taskName, int total) {
		super(taskName);
		totalWork = total;
	}

	/**
	 * Add the work increment to the total.
	 * @param workIncrement
	 */
	void addWork(double workIncrement) {
		preWork += workIncrement;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobInfo#getDisplayString()
	 */
	String getDisplayString() {
		int done = (int) (preWork * 100 / totalWork);
		String[] messageValues = new String[2];
		messageValues[0] = super.getDisplayString();
		messageValues[1] = String.valueOf(done);
		return ProgressMessages.format("JobInfo.DoneMessage",messageValues);
		
	}

}
