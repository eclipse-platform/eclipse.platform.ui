package org.eclipse.ui.dialogs;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;

/**
 * A working set selection dialog displays the list of working
 * sets available in the workbench.
 * <p>
 * Use org.eclipse.ui.IWorkingSetManager#createWorkingSetSelectionDialog(Shell)
 * to create an instance of this dialog.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see org.eclipse.ui.IWorkingSetManager
 * @since 2.0
 */
public interface IWorkingSetSelectionDialog {
	/**
	 * Returns the working sets selected in the dialog or 
	 * <code>null</code> if the dialog was canceled.
	 * 
	 * @return the working sets selected in the dialog.
	 */	
	public IWorkingSet[] getSelection();
	/**
	 * Displays the working set selection dialog.
	 * 
	 * @return Window.OK if the dialog closes with the working 
	 * 	set selection confirmed.
	 * 	Window.CANCEL if the dialog closes with the working set 
	 * 	selection dismissed.
	 * @see org.eclipse.jface.Window
	 */	
	public int open();
	/**
	 * Sets the working sets that are initially selected in the dialog.
	 * 
	 * @param workingSets the working sets to select in the dialog.
	 */	
	public void setSelection(IWorkingSet[] workingSets);	
}
