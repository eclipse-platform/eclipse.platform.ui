/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tomasz Zarna <tomasz.zarna@tasktop.com> - Bug 37183
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;

/**
 * A working set manager stores working sets and provides property change
 * notification when a working set is added or removed.
 * <p>
 * The workbench working set manager can be accessed using
 * <code>IWorkbench#getWorkingSetManager()</code>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkingSet
 * @since 2.0 initial version
 * @since 3.0 added createWorkingSet(IMemento)
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkingSetManager {

	/**
	 * Change event id when a working set is added newValue of the
	 * PropertyChangeEvent will be the added working set. oldValue will be null.
	 *
	 * @see IPropertyChangeListener
	 */
	String CHANGE_WORKING_SET_ADD = "workingSetAdd"; //$NON-NLS-1$

	/**
	 * Change event id when a working set is removed newValue of the
	 * PropertyChangeEvent will be null. oldValue will be the removed working set.
	 *
	 * @see IPropertyChangeListener
	 */
	String CHANGE_WORKING_SET_REMOVE = "workingSetRemove"; //$NON-NLS-1$

	/**
	 * Change event id when the working set contents changed newValue of the
	 * PropertyChangeEvent will be the changed working set.
	 *
	 * @see IPropertyChangeListener
	 */
	String CHANGE_WORKING_SET_CONTENT_CHANGE = "workingSetContentChange"; //$NON-NLS-1$

	/**
	 * Change event id when the working set name changed. newValue of the
	 * PropertyChangeEvent will be the changed working set.
	 *
	 * @see IPropertyChangeListener
	 */
	String CHANGE_WORKING_SET_NAME_CHANGE = "workingSetNameChange"; //$NON-NLS-1$

	/**
	 * Change event id when the working set label changed. newValue of the
	 * PropertyChangeEvent will be the changed working set.
	 *
	 * @see IPropertyChangeListener
	 * @since 3.2
	 */
	String CHANGE_WORKING_SET_LABEL_CHANGE = "workingSetLabelChange"; //$NON-NLS-1$

	/**
	 * Change event id when a working set updater got installed. NewValue of the
	 * PropertyChangeEvent will be the installed updater. OldValue will be
	 * <code>null</code>
	 *
	 * @since 3.1
	 */
	String CHANGE_WORKING_SET_UPDATER_INSTALLED = "workingSetUpdaterInstalled"; //$NON-NLS-1$

	/**
	 * Change event id when a working set updater got uninstalled. NewValue will be
	 * <code>null</code> OldValue of the PropertyChangeEvent will be the uninstalled
	 * updater.
	 *
	 * @since 3.3
	 */
	String CHANGE_WORKING_SET_UPDATER_UNINSTALLED = "workingSetUpdaterUninstalled"; //$NON-NLS-1$

	/**
	 * Adds a property change listener.
	 *
	 * @param listener the property change listener to add
	 */
	void addPropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Adds a working set to the top of the list of most recently used working sets,
	 * making it the most recently used working set. The last (oldest) item will be
	 * deleted if the list exceeds the size limit.
	 *
	 * @param workingSet the working set to add to the list of most recently used
	 *                   working sets.
	 */
	void addRecentWorkingSet(IWorkingSet workingSet);

	/**
	 * Adds a working set to the receiver. The working set must not exist yet.
	 *
	 * @param workingSet the working set to add
	 */
	void addWorkingSet(IWorkingSet workingSet);

	/**
	 * Creates a new working set. The working set is not added to the working set
	 * manager.
	 *
	 * @param name     the name of the new working set. Should not have leading or
	 *                 trailing whitespace.
	 * @param elements the working set contents
	 * @return a new working set with the specified name and content
	 */
	IWorkingSet createWorkingSet(String name, IAdaptable[] elements);

	/**
	 * Create a working set that is the union of a collection of other working sets.
	 * One connected (via {@link IWorkingSetManager#addWorkingSet(IWorkingSet)} this
	 * working set will be automatically updated to reflect the contents of the
	 * component sets, should they themselves change.
	 *
	 * @param name       the name of the new working set. Should not have leading or
	 *                   trailing whitespace.
	 * @param label      the user-friendly label the working set
	 * @param components the component working sets
	 * @return a new working set with the specified name and content
	 *
	 * @since 3.2
	 */
	IWorkingSet createAggregateWorkingSet(String name, String label, IWorkingSet[] components);

	/**
	 * Re-creates and returns a working set from the state captured within the given
	 * memento.
	 *
	 * @param memento a memento containing the state for the working set
	 * @return the restored working set, or <code>null</code> if it could not be
	 *         created
	 *
	 * @since 3.0
	 */
	IWorkingSet createWorkingSet(IMemento memento);

	/**
	 * Creates a working set edit wizard for the specified working set. The working
	 * set will already be set in the wizard. The caller is responsible for creating
	 * and opening a wizard dialog.
	 *
	 * Example: <code>
	 *  IWorkingSetEditWizard wizard = workingSetManager.createWorkingSetEditWizard(workingSet);
	 *  if (wizard != null) {
	 *	  WizardDialog dialog = new WizardDialog(shell, wizard);
	 *
	 *	  dialog.create();
	 *	  if (dialog.open() == Window.OK) {
	 *		  workingSet = wizard.getSelection();
	 *    }
	 *	}
	 * </code>
	 *
	 * @param workingSet working set to create a working set edit wizard for.
	 * @return a working set edit wizard to edit the specified working set or
	 *         <code>null</code> if no edit wizard has been defined for the working
	 *         set. If the defined edit wizard for the working set could not be
	 *         loaded a default IResource based wizard will be returned. If the
	 *         default edit wizard can not be loaded <code>null</code> is returned.
	 * @since 2.1
	 */
	IWorkingSetEditWizard createWorkingSetEditWizard(IWorkingSet workingSet);

	/**
	 * Creates a working set new wizard. The wizard will allow creating new working
	 * sets. Returns <code>null</code> if there aren't any working set definitions
	 * that support creation of working sets.
	 * <p>
	 * Example: <code>
	 *   IWorkingSetNewWizard wizard= workingSetManager.createWorkingSetNewWizard(null);
	 *   if (wizard != null) {
	 *	     WizardDialog dialog = new WizardDialog(shell, wizard);
	 *
	 *	     dialog.create();
	 *	     if (dialog.open() == Window.OK) {
	 *		    ...
	 *       }
	 *   }
	 * </code>
	 * </p>
	 *
	 * @param workingSetIds a list of working set ids which are valid workings sets
	 *                      to be created or <code>null</code> if all currently
	 *                      available working set types are valid
	 *
	 * @return the working set new wizard or <code>null</code>
	 *
	 * @since 3.1
	 */
	IWorkingSetNewWizard createWorkingSetNewWizard(String[] workingSetIds);

	/**
	 * @param parent the parent shell
	 * @return the dialog
	 * @deprecated use createWorkingSetSelectionDialog(parent, true) instead
	 */
	@Deprecated
	IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parent);

	/**
	 * Creates a working set selection dialog that lists all working sets and allows
	 * the user to add, remove and edit working sets. The caller is responsible for
	 * opening the dialog with <code>IWorkingSetSelectionDialog#open</code>, and
	 * subsequently extracting the selected working sets using
	 * <code>IWorkingSetSelectionDialog#getSelection</code>.
	 *
	 * @param parentShell the parent shell of the working set selection dialog
	 * @param multi       true=
	 *                    <code>IWorkingSetSelectionDialog#getSelection()</code>
	 *                    returns the working sets chosen in the dialog as an array
	 *                    of working set. false=
	 *                    <code>IWorkingSetSelectionDialog#getSelection()</code>
	 *                    returns an array having a single aggregate working set of
	 *                    all working sets selected in the dialog.
	 * @return a working set selection dialog
	 */
	IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parentShell, boolean multi);

	/**
	 * Creates a working set selection dialog that lists all working sets with the
	 * specified ids and allows the user to add, remove and edit working sets with
	 * the specified ids. The caller is responsible for opening the dialog with
	 * <code>IWorkingSetSelectionDialog#open</code>, and subsequently extracting the
	 * selected working sets using
	 * <code>IWorkingSetSelectionDialog#getSelection</code>.
	 *
	 * @param parentShell    the parent shell of the working set selection dialog
	 * @param multi          true=
	 *                       <code>IWorkingSetSelectionDialog#getSelection()</code>
	 *                       returns the working sets chosen in the dialog as an
	 *                       array of working set. false=
	 *                       <code>IWorkingSetSelectionDialog#getSelection()</code>
	 *                       returns an array having a single aggregate working set
	 *                       of all working sets selected in the dialog.
	 * @param workingsSetIds a list of working set ids which are valid workings sets
	 *                       to be selected, created, removed or edited, or
	 *                       <code>null</code> if all currently available working
	 *                       set types are valid
	 * @return a working set selection dialog
	 * @since 3.1
	 */
	IWorkingSetSelectionDialog createWorkingSetSelectionDialog(Shell parentShell, boolean multi,
			String[] workingsSetIds);

	/**
	 * Returns the list of most recently used working sets. The most recently used
	 * working set appears first in the list.
	 *
	 * @return the list of most recently used working sets
	 */
	IWorkingSet[] getRecentWorkingSets();

	/**
	 * Returns the working set with the specified name. Returns null if there is no
	 * working set with that name.
	 *
	 * @param name the name of the working set to return
	 * @return the working set with the specified name.
	 */
	IWorkingSet getWorkingSet(String name);

	/**
	 * Returns an array of all working sets stored in the receiver. The array is
	 * sorted by names. Any working set whose {@link IWorkingSet#isVisible()} method
	 * returns false will not be included in this array. For a complete list of
	 * working sets please use {@link #getAllWorkingSets()}.
	 *
	 * @return the working sets stored in the receiver
	 */
	IWorkingSet[] getWorkingSets();

	/**
	 * Returns an array of all working sets stored in the receiver including those
	 * that are marked as being not visible.
	 *
	 * @see IWorkingSet#isVisible()
	 * @return the working sets stored in the receiver
	 * @since 3.2
	 */
	IWorkingSet[] getAllWorkingSets();

	/**
	 * Removes the property change listener.
	 *
	 * @param listener the property change listener to remove
	 */
	void removePropertyChangeListener(IPropertyChangeListener listener);

	/**
	 * Removes the working set
	 *
	 * @param workingSet the working set to remove
	 */
	void removeWorkingSet(IWorkingSet workingSet);

	/**
	 * Disposes the working set manager.
	 *
	 * @since 3.1
	 */
	void dispose();

	/**
	 * Utility method that will add the <code>element</code> to each given working
	 * set in <code>workingSets</code> if possible. This method will invoke
	 * {@link IWorkingSet#adaptElements(IAdaptable[])} for the element on each
	 * working set and the result of this method will be used rather than the
	 * original element in the addition operation.
	 *
	 * @param element     the element to adapt and then add to the working sets
	 * @param workingSets the working sets to add the element to
	 * @since 3.4
	 */
	void addToWorkingSets(IAdaptable element, IWorkingSet[] workingSets);

	/**
	 * Sets maximum length of the recent working sets list.
	 *
	 * @param length maximum number of recent working sets to be kept in the list
	 * @since 3.7
	 */
	void setRecentWorkingSetsLength(int length);

	/**
	 * Returns the maximum length of the recent working sets list.
	 *
	 * @return the maximum length of the recent working sets list.
	 * @since 3.7
	 */
	int getRecentWorkingSetsLength();
}
