package org.eclipse.ui.internal.progress;

class TaskInfo {
	String name;

	TaskInfo(String taskName) {
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
		return true;
	}

	boolean hasChildren() {
		return false;
	}

}
