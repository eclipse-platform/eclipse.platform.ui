package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/* package */ class SortByAction extends TaskAction {

	private int columnNumber;
		
	public SortByAction(TaskList tasklist, String id, int columnNumber) {
		super(tasklist, id);
		this.columnNumber=columnNumber;
		setChecked(false);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		TaskSorter oldSorter = (TaskSorter) getTaskList().getTableViewer().getSorter();
		if (oldSorter == null) { //start with default direction
			getTaskList().getTableViewer().setSorter(new TaskSorter(getTaskList(), columnNumber));
			//update the menu to indicate how task are currently sorted		
			getTaskList().updateSortingState();			
		} else if (columnNumber != oldSorter.getColumnNumber()) {
			//remember the previous direction
			TaskSorter newSorter = new TaskSorter(getTaskList(), columnNumber);
			newSorter.setReversed(oldSorter.isReversed());
			getTaskList().getTableViewer().setSorter(newSorter);
			//update the menu to indicate how task are currently sorted		
			getTaskList().updateSortingState();			
		}

	}

	/**
	 * Returns the columnNumber.
	 * @return int
	 */
	public int getColumnNumber() {
		return columnNumber;
	}

}
