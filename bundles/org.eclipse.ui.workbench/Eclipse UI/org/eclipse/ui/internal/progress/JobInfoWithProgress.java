package org.eclipse.ui.internal.progress;


public class JobInfoWithProgress extends JobInfo {
	int totalWork;
	double workComplete = 0;

	JobInfoWithProgress(String taskName, int total) {
		super(taskName);
		totalWork = total;
	}

	void addWork(double workIncrement) {
		workComplete += workIncrement;
		if (workComplete > totalWork)
			workComplete = totalWork;
	}

	double percentDone() {
		return (100 * workComplete) / totalWork;
	}


	String getDisplayString() {
		return super.getDisplayString()
			+ " "
			+ String.valueOf(this.percentDone())
			+ "%";
	}
	

}
