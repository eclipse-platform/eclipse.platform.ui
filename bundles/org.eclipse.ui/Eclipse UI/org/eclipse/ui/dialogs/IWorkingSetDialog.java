package org.eclipse.ui.dialogs;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;

/**
 * A working set dialog allows the user to edit an existing 
 * working set and create a new working set.
 * <p>
 * Clients should implement this interface and include the 
 * name of their class in an extension contributed to the 
 * workbench's working set extension point 
 * (named <code>"org.eclipse.ui.workingSets"</code>) if they 
 * want to provide a special dialog for a particular working 
 * set element type.
 * </p>
 *
 * @since 2.0
 */
public interface IWorkingSetDialog {
	/**
	 * Returns the working set edited in the dialog after the 
	 * dialog has closed.
	 * Returns the working set that was initially set using 
	 * <code>setSelection</code>if the dialog has not been 
	 * closed yet.
	 * 
	 * @return the working set edited or created in the 
	 * 	dialog.
	 */
	public IWorkingSet getSelection();
	/**
	 * Initializes the working set dialog
	 * 
	 * @param shell the dialog parent
	 */
	public void init(Shell shell);
	/**
	 * Displays the working set dialog.
	 * 
	 * @return Window.OK if the dialog closes with the working 
	 * 	set changes confirmed.
	 * 	Window.CANCEL if the dialog closes with the working set 
	 * 	changes dismissed.
	 * @see org.eclipse.jface.Window
	 */
	public int open();
	/**
	 * Sets the working set edited in the dialog.
	 * Must not be called after #open() is called
	 * 
	 * @param workingSet the working set edited in the dialog.
	 */
	public void setSelection(IWorkingSet workingSet);	
}
