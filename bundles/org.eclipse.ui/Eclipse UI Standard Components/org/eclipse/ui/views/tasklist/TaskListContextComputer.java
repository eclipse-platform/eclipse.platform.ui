package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.IContextComputer;
import org.eclipse.ui.help.ViewContextComputer;

/**
 * Used to determining the help context for a task list
 */ 

/*package*/ class TaskListContextComputer implements IContextComputer {
	private TaskList taskList;
	private Object helpContext;
	
	/**
	 * Constructor for TaskListContextComputer.
	 * 
	 * @param taskList the task list
	 * @param helpContext the help context id for the task list
	 */
	public TaskListContextComputer(TaskList taskList, Object helpContext) {
		Assert.isNotNull(taskList);
		this.taskList = taskList;
		Assert.isNotNull(helpContext);
		this.helpContext = helpContext;
	}

	/* (non-Javadoc)
	 * Method declared on IContextComputer.
	 */
	public Object[] computeContexts(HelpEvent event) {
		Object context = null;
		// See if there is a context registered for the current selection
		if (getMarker() != null) {
			IWorkbench workbench = taskList.getViewSite().getWorkbenchWindow().getWorkbench();
			context = workbench.getMarkerHelpRegistry().getHelp(getMarker());
		}
		
		if (context == null)
			// use the task list help
			context = helpContext;
			
		return new Object[] {context};
	}
		
	/* (non-Javadoc)
	 * Method declared on IContextComputer.
	 */
	public Object[] getLocalContexts(HelpEvent event) {
		return new Object[] {helpContext};
	}
	
	/**
	 * Returns the selected marker (may be <code>null</code>).
	 * 
	 * @return the selected marker
	 */
	private IMarker getMarker() {
		return (IMarker)((IStructuredSelection)taskList.getSelection()).getFirstElement();
	}
	
}



