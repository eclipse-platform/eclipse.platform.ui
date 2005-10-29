/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 *   	should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetNewWizard;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * Abstract baseclass for various working set dialogs.
 * 
 * @since 3.2
 */
public abstract class AbstractWorkingSetDialog extends SelectionDialog
		implements IWorkingSetSelectionDialog {

	private Button newButton;

	private Button detailsButton;

	private Button removeButton;

	private IWorkingSet[] result;

	private List addedWorkingSets;

	private List removedWorkingSets;

	private Map editedWorkingSets;

	private List removedMRUWorkingSets;

	private Set workingSetIds;

	protected AbstractWorkingSetDialog(Shell parentShell, String[] workingSetIds) {
		super(parentShell);
		if (workingSetIds != null) {
			this.workingSetIds = new HashSet();
			for (int i = 0; i < workingSetIds.length; i++) {
				this.workingSetIds.add(workingSetIds[i]);
			}
		}
	}

	/**
	 * Return the set of supported working set types.
	 * 
	 * @return the supported working set types
	 */
	protected Set getSupportedWorkingSetIds() {
		return workingSetIds;
	}

	/**
	 * Adds the modify buttons to the dialog.
	 * 
	 * @param composite
	 *            Composite to add the buttons to
	 */
	protected void addModifyButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		int id = IDialogConstants.CLIENT_ID + 1;
		newButton = createButton(buttonComposite, id++,
				WorkbenchMessages.WorkingSetSelectionDialog_newButton_label,
				false);
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet();
			}
		});

		detailsButton = createButton(
				buttonComposite,
				id++,
				WorkbenchMessages.WorkingSetSelectionDialog_detailsButton_label,
				false);
		detailsButton.setEnabled(false);
		detailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelectedWorkingSet();
			}
		});

		removeButton = createButton(buttonComposite, id++,
				WorkbenchMessages.WorkingSetSelectionDialog_removeButton_label,
				false);
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedWorkingSets();
			}
		});
	}

	/**
	 * Opens a working set wizard for editing the currently selected working
	 * set.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 */
	void editSelectedWorkingSet() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		IWorkingSet editWorkingSet = (IWorkingSet) getSelectedWorkingSets()
				.get(0);
		IWorkingSetEditWizard wizard = manager
				.createWorkingSetEditWizard(editWorkingSet);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		IWorkingSet originalWorkingSet = (IWorkingSet) editedWorkingSets
				.get(editWorkingSet);
		boolean firstEdit = originalWorkingSet == null;

		// save the original working set values for restoration when selection
		// dialog is cancelled.
		if (firstEdit) {
			originalWorkingSet = new WorkingSet(editWorkingSet.getName(),
					editWorkingSet.getLabel(), editWorkingSet.getElements());
		} else {
			editedWorkingSets.remove(editWorkingSet);
		}
		dialog.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_EDIT_WIZARD);
		if (dialog.open() == Window.OK) {
			editWorkingSet = wizard.getSelection();
			availableWorkingSetsChanged();
			// make sure ok button is enabled when the selected working set
			// is edited. Fixes bug 33386.
			updateButtonAvailability();
		}
		editedWorkingSets.put(editWorkingSet, originalWorkingSet);
	}

	/**
	 * Opens a working set wizard for creating a new working set.
	 */
	void createWorkingSet() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		String ids[] = null;
		if (workingSetIds != null) {
			ids = (String[]) workingSetIds.toArray(new String[workingSetIds
					.size()]);
		}
		IWorkingSetNewWizard wizard = manager.createWorkingSetNewWizard(ids);
		// the wizard can never be null since we have at least a resource
		// working set
		// creation page
		WizardDialog dialog = new WizardDialog(getShell(), wizard);

		dialog.create();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);
		if (dialog.open() == Window.OK) {
			IWorkingSet workingSet = wizard.getSelection();
			manager.addWorkingSet(workingSet);
			addedWorkingSets.add(workingSet);
			availableWorkingSetsChanged();
		}
	}

	protected abstract List getSelectedWorkingSets();

	/**
	 * Notifies the dialog that there has been a change to the sets available
	 * for use. In other words, the user has either added, deleted or renamed a
	 * set.
	 */
	protected abstract void availableWorkingSetsChanged();


	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#getSelection()
	 */
	public IWorkingSet[] getSelection() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(org.eclipse.ui.IWorkingSet[])
	 */
	public void setSelection(IWorkingSet[] selection) {
		result = selection;
	}

	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#open()
	 */
	public int open() {
		addedWorkingSets = new ArrayList();
		removedWorkingSets = new ArrayList();
		editedWorkingSets = new HashMap();
		removedMRUWorkingSets = new ArrayList();
		return super.open();
	}

	/**
	 * Return the list of working sets that were added during the life of this
	 * dialog.
	 * 
	 * @return the working sets
	 */
	protected final List getAddedWorkingSets() {
		return addedWorkingSets;
	}

	/**
	 * Return the map of working sets that were edited during the life of this
	 * dialog.
	 * 
	 * @return the working sets
	 */
	protected final Map getEditedWorkingSets() {
		return editedWorkingSets;
	}

	/**
	 * Return the list of working sets that were removed from the MRU list
	 * during the life of this dialog.
	 * 
	 * @return the working sets
	 */
	protected final List getRemovedMRUWorkingSets() {
		return removedMRUWorkingSets;
	}

	/**
	 * Return the list of working sets that were removed during the life of this
	 * dialog.
	 * 
	 * @return the working sets
	 */
	protected final List getRemovedWorkingSets() {
		return removedWorkingSets;
	}

	/**
	 * Updates the modify buttons' enabled state based on the current seleciton.
	 */
	protected void updateButtonAvailability() {
		List selection = getSelectedWorkingSets();
		boolean hasSelection = selection != null && !selection.isEmpty();
		boolean hasSingleSelection = hasSelection;
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault()
				.getWorkingSetRegistry();

		newButton.setEnabled(registry.hasNewPageWorkingSetDescriptor());

		removeButton.setEnabled(hasSelection);

		IWorkingSet selectedWorkingSet = null;
		if (hasSelection) {
			hasSingleSelection = selection.size() == 1;
			if (hasSingleSelection)
				selectedWorkingSet = (IWorkingSet) selection
						.get(0);
		}
		detailsButton.setEnabled(hasSingleSelection
				&& selectedWorkingSet.isEditable());

		getOkButton().setEnabled(true);
	}

	/**
	 * Removes the selected working sets from the workbench.
	 */
	protected void removeSelectedWorkingSets() {
		List selection = getSelectedWorkingSets();
		removeSelectedWorkingSets(selection);
	}

	/**
	 * Remove the working sets contained in the provided selection from the
	 * working set manager.
	 * 
	 * @param selection
	 *            the sets
	 */
	protected void removeSelectedWorkingSets(List selection) {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault()
				.getWorkingSetManager();
		Iterator iter = selection.iterator();
		while (iter.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet) iter.next();
			if (getAddedWorkingSets().contains(workingSet)) {
				getAddedWorkingSets().remove(workingSet);
			} else {
				IWorkingSet[] recentWorkingSets = manager
						.getRecentWorkingSets();
				for (int i = 0; i < recentWorkingSets.length; i++) {
					if (workingSet.equals(recentWorkingSets[i])) {
						getRemovedMRUWorkingSets().add(workingSet);
						break;
					}
				}
				getRemovedWorkingSets().add(workingSet);
			}
			manager.removeWorkingSet(workingSet);
		}
		availableWorkingSetsChanged();
	}
}
