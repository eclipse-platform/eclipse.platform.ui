package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.Shell;
 
/**
 * FOR USE BY TESTS ONLY!
 * <p>
 * Stub class that provides access to classes visible to the package
 * <code>org.eclipse.ui.views.tasklist</code>.  For the purpose
 * of testing.
 * </p>
 * @private
 */
public class TaskListTestStub {
	//Prevent instantiation
	private TaskListTestStub(){}
	
	/**
	 * Gives access to an instance of FiltersDialog.
	 * @return FiltersDialog an instance of FiltersDialog.
	 */
	public static FiltersDialog newFiltersDialog(Shell parentShell) {
		return new FiltersDialog(parentShell);
	}
}

