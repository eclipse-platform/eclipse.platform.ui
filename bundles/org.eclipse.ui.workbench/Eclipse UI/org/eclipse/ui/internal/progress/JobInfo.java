package org.eclipse.ui.internal.progress;

import java.util.ArrayList;

class JobInfo {
	String name;
	ArrayList children = new ArrayList();
	JobInfo parent;
	JobInfo currentChild;

	JobInfo(String taskName, JobInfo parentObject) {
		this(taskName);
		parent = parentObject;
		parent.addChild(this);
	}

	JobInfo(String taskName) {
		name = taskName;
	}

	/**
	 * Return the name of the task info.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the displayString for the receiver.
	 * @return
	 */
	String getDisplayString() {
		return getName();
	}

	boolean hasParent() {
		return parent != null;
	}

	boolean hasChildren() {
		return children.size() > 0;
	}

	Object[] getChildren() {
		return children.toArray();
	}

	JobInfo getParent() {
		return parent;
	}

	void addChild(JobInfo child) {
		children.add(child);
		currentChild = child;
	}

	//Add the new job info to the lowest level child you
	//currently have. If there is a currentChild add it to them,
	//if not add it to the receiver.
	void addToLeafChild(JobInfo child) {
		if (currentChild == null)
			addChild(child);
		else
			currentChild.addChild(child);
	}
	
	/**
	 * Add the amount of work to the job info.
	 * @param workIncrement
	 */
	void addWork(int workIncrement) {
		//No work on a simple label- pass it down
		if(currentChild != null)
			currentChild.addWork(workIncrement);
	}

}
