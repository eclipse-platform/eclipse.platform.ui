package org.eclipse.ui.internal.progress;

import java.util.ArrayList;

public class TaskInfoWithProgress extends TaskInfo {
	int totalWork;
	int workComplete = 0;
	ArrayList subTasks = new ArrayList();

	TaskInfoWithProgress(String taskName, int total) {
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

	TaskInfo addSubtask(String subtask) {
		TaskInfo newInfo = new TaskInfo(subtask);
		subTasks.add(newInfo);
		return newInfo;
	}

	String getDisplayString() {
		return super.getDisplayString()
			+ " "
			+ String.valueOf(this.percentDone())
			+ "%";
	}
	
	TaskInfo[] getSubtasks(){
		TaskInfo[] infos = new TaskInfo[subTasks.size()];
		subTasks.toArray(infos);
		return infos;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.TaskInfo#hasChildren()
	 */
	boolean hasChildren() {
		return !(subTasks.isEmpty());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.TaskInfo#hasParent()
	 */
	boolean hasParent() {
		// XXX Auto-generated method stub
		return false;
	}

}
