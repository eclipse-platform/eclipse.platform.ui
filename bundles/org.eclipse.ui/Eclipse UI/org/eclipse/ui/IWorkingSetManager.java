package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * A working set manager stores working sets and provides property 
 * change notification when a working set is added or removed.
 * <p>
 * The workbench working set manager can be accessed using 
 * <code>IWorkbench#getWorkingSetManager()</code>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IWorkingSet
 * @since 2.0
 */
public interface IWorkingSetManager {
	/**
	 * Change event id when a working set is added
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_ADD = "workingSetAdd";		//$NON-NLS-1$
	/**
	 * Change event id when a working set is removed
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_REMOVE = "workingSetRemove";	//$NON-NLS-1$
	/**
	 * Change event id when the working set contents changed
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_CONTENT_CHANGE = "workingSetContentChange";	//$NON-NLS-1$
	/**
	 * Change event id when the working set name changed
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_NAME_CHANGE = "workingSetNameChange";	//$NON-NLS-1$	
	 
	/**
	 * Adds a property change listener.
	 * 
	 * @param listener the property change listener to add
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener);
	/**
	 * Adds a working set to the receiver. The working set must 
	 * not exist yet.
	 * 
	 * @param workingSet the working set to add
	 */
	public void addWorkingSet(IWorkingSet workingSet);
	/**
	 * Creates a new working set.
	 * The working set is not added to the working set manager.
	 * 
	 * @param name the name of the new working set. Should not have 
	 * 	leading or trailing whitespace.
	 * @param elememts the working set contents
	 * @return a new working set with the specified name and content
	 */
	public IWorkingSet createWorkingSet(String name, IAdaptable[] elements);
	/**
	 * @deprecated use createWorkingSetSelectionDialog(parent, true) instead
	 */
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent);
	/**
	 * Creates a working set selection dialog that lists all working 
	 * sets and allows the user to add, remove and edit working sets.
	 * The caller is responsible for opening the dialog with 
	 * <code>IWorkingSetSelectionDialog#open</code>, and subsequently 
	 * extracting the selected working sets using 
	 * <code>IWorkingSetSelectionDialog#getSelection</code>.
	 * 
	 * @param shell the parent of the working set selection dialog
	 * @param multi true=more than one working set can be chosen 
	 * 	in the dialog. false=only one working set can be chosen. Multiple
	 * 	working sets can still be selected and removed from the list but
	 * 	the dialog can only be closed when a single working set is selected.
	 * @return a working set selection dialog
	 */
	public IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent, boolean multi);
	/**
	 * Returns the working set with the specified name.
	 * Returns null if there is no working set with that name.
	 * 
	 * @param name the name of the working set to return
	 * @return the working set with the specified name.
	 */
	public IWorkingSet getWorkingSet(String name);
	/**
	 * Returns an array of all working sets stored in the receiver.
	 * 
	 * @return the working sets stored in the receiver
	 */
	public IWorkingSet[] getWorkingSets();
	/**
	 * Removes the property change listener.
	 * 
	 * @param listener the property change listener to remove
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener);
	/**
	 * Removes the working set
	 * 
	 * @param workingSet the working set to remove
	 */
	public void removeWorkingSet(IWorkingSet workingSet);
}