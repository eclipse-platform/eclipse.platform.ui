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

public class JobInfoWithProgress extends JobInfo {
	int multiplier;
	int preWork = 0;
	ProgressBar indicator;

	JobInfoWithProgress(String taskName, int total) {
		super(taskName);
		multiplier = 10000 / total;
	}

	void addWork(double workIncrement) {
		int newWork = (int) (multiplier * workIncrement);
		preWork += newWork;
		if (indicator == null || indicator.isDisposed())
			return;
		indicator.getDisplay().asyncExec(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				if (indicator == null || indicator.isDisposed())
					return;
				indicator.setSelection(preWork);
			}
		});

	}

	/**
	 * Set the progress indicator to use for this job info.
	 * @param indicator
	 */
	public void setProgressBar(ProgressBar anIndicator) {
		this.indicator = anIndicator;
		this.indicator.setMaximum(10000);
		this.indicator.setSelection(preWork);
	}

	
	public ProgressBar getProgressBar() {
		return indicator;
	}



}
