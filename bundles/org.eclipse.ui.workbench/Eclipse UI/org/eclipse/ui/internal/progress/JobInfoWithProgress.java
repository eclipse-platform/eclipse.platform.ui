package org.eclipse.ui.internal.progress;

import org.eclipse.jface.dialogs.ProgressIndicator;

public class JobInfoWithProgress extends JobInfo {
	int multiplier;
	int preWork = 0;
	ProgressIndicator indicator;

	JobInfoWithProgress(String taskName, int total) {
		super(taskName);
		multiplier = 10000/total;
	}

	void addWork(double workIncrement) {
		int newWork =(int) (multiplier * workIncrement);
		if (indicator != null)
			indicator.worked(newWork);
		else
			preWork += newWork;
	}

	/**
	 * Set the progress indicator to use for this job info.
	 * @param indicator
	 */
	public void setProgressIndicator(ProgressIndicator anIndicator) {
		this.indicator = anIndicator;
		this.indicator.beginTask(10000);
		this.indicator.worked(preWork);
	}

}
