package org.eclipse.ui.internal.progress;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.widgets.Label;

public class JobInfoWithProgress extends JobInfo {
	int multiplier;
	int preWork = 0;
	ProgressIndicator indicator;
	Label label;

	JobInfoWithProgress(String taskName, int total) {
		super(taskName);
		multiplier = 10000 / total;
	}

	void addWork(double workIncrement) {
		int newWork = (int) (multiplier * workIncrement);
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

	/**
	 * Set the label to use for this job info.
	 * @param indicator
	 */
	public void setLabel(Label newLabel) {
		label = newLabel;
	}

	public ProgressIndicator getProgressIndicator() {
		return indicator;
	}

	public Label getLabel() {
		return label;
	}

}
