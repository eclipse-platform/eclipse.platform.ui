package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/* package */ class SortDirectionAction extends TaskAction {

	/* direction = true		-> ascending
	 * direction = false	-> descending */
	 
	private boolean direction;
	/**
	 * Constructor for SortDirectionAction.
	 * @param tasklist
	 * @param id
	 */
	public SortDirectionAction(TaskList tasklist, String id, boolean direction) {
		super(tasklist, id);
		this.direction = direction;
		setChecked(false);		
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		TaskSorter oldSorter = (TaskSorter) getTaskList().getTableViewer().getSorter();
		if (oldSorter != null) {
			oldSorter.setReversed(!direction);
			getTaskList().getTableViewer().refresh();
			//update the menu to indicate how task are currently sorted
			getTaskList().updateSortingState();
		}
	}

}
