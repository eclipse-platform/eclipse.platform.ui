/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
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
	 * newValue of the PropertyChangeEvent will be the added working set.
	 * oldValue will be null.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_ADD = "workingSetAdd";		//$NON-NLS-1$
	/**
	 * Change event id when a working set is removed
	 * newValue of the PropertyChangeEvent will be null.
	 * oldValue will be the removed working set.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_REMOVE = "workingSetRemove";	//$NON-NLS-1$
	/**
	 * Change event id when the working set contents changed
	 * newValue of the PropertyChangeEvent will be the changed working set.
	 * oldValue will be null.
	 *
	 * @see IPropertyChangeListener
	 */
	public static final String CHANGE_WORKING_SET_CONTENT_CHANGE = "workingSetContentChange";	//$NON-NLS-1$
	/**
	 * Change event id when the working set name changed.
	 * newValue of the PropertyChangeEvent will be the changed working set.
	 * oldValue will be null.
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
	 * Adds a working set to the top of the list of most recently used 
	 * working sets, making it the most recently used working set.
	 * The last (oldest) item will be deleted if the list exceeds the 
	 * size limit.
	 * 
	 * @param workingSet the working set to add to the list of most 
	 * 	recently used working sets.
	 */
	public void addRecentWorkingSet(IWorkingSet workingSet);
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
	 * Creates a working set edit wizard for the specified working set.
	 * The working set will already be set in the wizard.
	 * The caller is responsible for creating and opening a wizard dialog.
	 *
	 * Example:
	 * <code>
	 *  IWorkingSetEditWizard wizard = workingSetManager.createWorkingSetEditWizard(workingSet);
	 *	WizardDialog dialog = new WizardDialog(shell, wizard);
	 *
	 *	dialog.create();		
	 *	if (dialog.open() == Window.OK) {		
	 *		workingSet = wizard.getSelection();
	 *	}
	 * </code>
	 * 
	 * @param workingSet working set to create a working set edit wizard 
	 * 	for.
	 * @return a working set edit wizard to edit the specified working set. 
	 * 	If the specific edit wizard for the working set could not be loaded 
	 * 	a default IResource based wizard will be returned. 
	 * 	If the default edit wizard can not be loaded null is returned.
	 * @since 2.1
	 */
	public IWorkingSetEditWizard createWorkingSetEditWizard(IWorkingSet workingSet);
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
	 * Returns the list of most recently used working sets.
	 * The most recently used working set appears first in the list.
	 * 
	 * @return the list of most recently used working sets
	 */
	public IWorkingSet[] getRecentWorkingSets();
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
