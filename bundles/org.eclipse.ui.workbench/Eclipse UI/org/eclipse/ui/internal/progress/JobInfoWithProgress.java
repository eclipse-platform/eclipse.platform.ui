package org.eclipse.ui.internal.progress;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class JobInfoWithProgress extends JobInfo {
	int multiplier;
	int preWork = 0;
	ProgressBar indicator;
	Label label;

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

	/**
	 * Set the label to use for this job info.
	 * @param indicator
	 */
	public void setLabel(Label newLabel) {
		label = newLabel;
	}

	public ProgressBar getProgressBar() {
		return indicator;
	}

	public Label getLabel() {
		return label;
	}

}
