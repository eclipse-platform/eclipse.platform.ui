package org.eclipse.ui.internal.progress;

import java.util.ArrayList;

public class JobInfoWithProgress extends JobInfo {
	int totalWork;
	int workComplete = 0;

	JobInfoWithProgress(String taskName, int total) {
		super(taskName);
		totalWork = total;
	}

	void addWork(int workIncrement) {
		workComplete += workIncrement;
		if (workComplete > totalWork)
			workComplete = totalWork;
	}

	long percentDone() {
		return (100 * workComplete) / totalWork;
	}


	String getDisplayString() {
		return super.getDisplayString()
			+ " "
			+ String.valueOf(this.percentDone())
			+ "%";
	}
	

}
