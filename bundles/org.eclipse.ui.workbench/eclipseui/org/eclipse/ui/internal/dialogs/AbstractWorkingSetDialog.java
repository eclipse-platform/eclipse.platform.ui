/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 * 		Sebastian Davids <sdavids@gmx.de> - Bug 19346
 *      Lars Vogel <Lars.Vogel@vogella.com> - Bug 481473
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
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
public abstract class AbstractWorkingSetDialog extends SelectionDialog implements IWorkingSetSelectionDialog {

	private static final int ID_NEW = IDialogConstants.CLIENT_ID + 1;
	private static final int ID_DETAILS = ID_NEW + 1;
	private static final int ID_REMOVE = ID_DETAILS + 1;
	private static final int ID_SELECTALL = ID_REMOVE + 1;
	private static final int ID_DESELECTALL = ID_SELECTALL + 1;

	private Button newButton;

	private Button detailsButton;

	private Button removeButton;

	private Button selectAllButton;

	private Button deselectAllButton;

	private IWorkingSet[] result;

	private List<IWorkingSet> addedWorkingSets;

	private List<IWorkingSet> removedWorkingSets;

	private Map<IWorkingSet, IWorkingSet> editedWorkingSets;

	private List<IWorkingSet> removedMRUWorkingSets;

	private Set<String> workingSetIds;

	private boolean canEdit;

	protected AbstractWorkingSetDialog(Shell parentShell, String[] workingSetIds, boolean canEdit) {
		super(parentShell);
		if (workingSetIds != null) {
			this.workingSetIds = new HashSet<>();
			this.workingSetIds.addAll(Arrays.asList(workingSetIds));
		}
		this.canEdit = canEdit;
	}

	/**
	 * Return the set of supported working set types.
	 *
	 * @return the supported working set types
	 */
	protected Set<String> getSupportedWorkingSetIds() {
		return workingSetIds;
	}

	/**
	 * Adds the modify buttons to the dialog.
	 *
	 * @param composite Composite to add the buttons to
	 */
	protected void addModifyButtons(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.GRAB_VERTICAL);
		Composite buttonComposite = WidgetFactory.composite(SWT.RIGHT).layout(layout).layoutData(data)
				.create(composite);
		newButton = createButton(buttonComposite, ID_NEW, WorkbenchMessages.WorkingSetSelectionDialog_newButton_label,
				false);
		newButton.addSelectionListener(widgetSelectedAdapter(e -> createWorkingSet()));

		if (canEdit) {
			detailsButton = createButton(buttonComposite, ID_DETAILS,
					WorkbenchMessages.WorkingSetSelectionDialog_detailsButton_label, false);
			detailsButton.setEnabled(false);
			detailsButton.addSelectionListener(widgetSelectedAdapter(e -> editSelectedWorkingSet()));

			removeButton = createButton(buttonComposite, ID_REMOVE,
					WorkbenchMessages.WorkingSetSelectionDialog_removeButton_label, false);
			removeButton.setEnabled(false);
			removeButton.addSelectionListener(widgetSelectedAdapter(e -> removeSelectedWorkingSets()));
		}

		layout.numColumns = 1; // must manually reset the number of columns
								// because createButton increments it - we want
								// these buttons to be laid out vertically.
	}

	/**
	 * Add the select/deselect buttons.
	 *
	 * @param composite Composite to add the buttons to
	 */
	protected void addSelectionButtons(Composite composite) {
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);

		Composite buttonComposite = WidgetFactory.composite(SWT.NONE).layout(layout)
				.layoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING)).create(composite);

		selectAllButton = createButton(buttonComposite, ID_SELECTALL, WorkbenchMessages.SelectionDialog_selectLabel,
				false);
		selectAllButton.addSelectionListener(widgetSelectedAdapter(e -> selectAllSets()));

		deselectAllButton = createButton(buttonComposite, ID_DESELECTALL,
				WorkbenchMessages.SelectionDialog_deselectLabel, false);
		deselectAllButton.addSelectionListener(widgetSelectedAdapter(e -> deselectAllSets()));
	}

	/**
	 * Select all working sets.
	 */
	protected abstract void selectAllSets();

	/**
	 * Deselect all working sets.
	 */
	protected abstract void deselectAllSets();

	/**
	 * Opens a working set wizard for editing the currently selected working set.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage
	 */
	void editSelectedWorkingSet() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		IWorkingSet editWorkingSet = getSelectedWorkingSets().get(0);
		IWorkingSetEditWizard wizard = manager.createWorkingSetEditWizard(editWorkingSet);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		IWorkingSet originalWorkingSet = editedWorkingSets.get(editWorkingSet);
		boolean firstEdit = originalWorkingSet == null;

		// save the original working set values for restoration when selection
		// dialog is cancelled.
		if (firstEdit) {
			originalWorkingSet = new WorkingSet(editWorkingSet.getName(), editWorkingSet.getLabel(),
					editWorkingSet.getElements());
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
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		String ids[] = null;
		if (workingSetIds != null) {
			ids = workingSetIds.toArray(new String[workingSetIds.size()]);
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
			workingSetAdded(workingSet);
		}
	}

	protected abstract List<IWorkingSet> getSelectedWorkingSets();

	/**
	 * Notifies the dialog that there has been a change to the sets available for
	 * use. In other words, the user has either added, deleted or renamed a set.
	 * <p>
	 * Subclasses should override, but should call
	 * <code>super.availableWorkingSetsChanged</code> to update the selection button
	 * enablements.
	 * </p>
	 */
	protected void availableWorkingSetsChanged() {
		boolean enable = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets().length > 0;
		if (!(selectAllButton == null || selectAllButton.isDisposed())) {
			selectAllButton.setEnabled(enable);
		}
		if (!(deselectAllButton == null || deselectAllButton.isDisposed())) {
			deselectAllButton.setEnabled(enable);
		}
	}

	/**
	 * Notifies the dialog that the given working set was added to the sets
	 * available for use. In other words, the user has just added the given working
	 * set.
	 *
	 * <p>
	 * This implementation doesn't do anything. Subclasses can override it to
	 * auto-select the added working set.
	 * </p>
	 *
	 * @param addedSet the added working set.
	 */
	protected void workingSetAdded(IWorkingSet addedSet) {
	}

	@Override
	public IWorkingSet[] getSelection() {
		return result;
	}

	@Override
	public void setSelection(IWorkingSet[] selection) {
		result = selection;
	}

	@Override
	public int open() {
		addedWorkingSets = new ArrayList<>();
		removedWorkingSets = new ArrayList<>();
		editedWorkingSets = new HashMap<>();
		removedMRUWorkingSets = new ArrayList<>();
		return super.open();
	}

	/**
	 * Return the list of working sets that were added during the life of this
	 * dialog.
	 *
	 * @return the working sets
	 */
	protected final List<IWorkingSet> getAddedWorkingSets() {
		return addedWorkingSets;
	}

	/**
	 * Return the map of working sets that were edited during the life of this
	 * dialog.
	 *
	 * @return the working sets
	 */
	protected final Map<IWorkingSet, IWorkingSet> getEditedWorkingSets() {
		return editedWorkingSets;
	}

	/**
	 * Return the list of working sets that were removed from the MRU list during
	 * the life of this dialog.
	 *
	 * @return the working sets
	 */
	protected final List<IWorkingSet> getRemovedMRUWorkingSets() {
		return removedMRUWorkingSets;
	}

	/**
	 * Return the list of working sets that were removed during the life of this
	 * dialog.
	 *
	 * @return the working sets
	 */
	protected final List<IWorkingSet> getRemovedWorkingSets() {
		return removedWorkingSets;
	}

	/**
	 * Updates the modify buttons' enabled state based on the current seleciton.
	 */
	protected void updateButtonAvailability() {
		List<IWorkingSet> selection = getSelectedWorkingSets();
		boolean hasSelection = !selection.isEmpty();
		boolean hasSingleSelection = hasSelection;
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();

		newButton.setEnabled(registry.hasNewPageWorkingSetDescriptor());

		if (canEdit)
			removeButton.setEnabled(hasSelection);

		IWorkingSet selectedWorkingSet = null;
		if (hasSelection) {
			hasSingleSelection = selection.size() == 1;
			if (hasSingleSelection) {
				selectedWorkingSet = selection.get(0);
			}
		}
		if (canEdit) {
			detailsButton.setEnabled(hasSingleSelection && selectedWorkingSet.isEditable());
		}

		getOkButton().setEnabled(true);
	}

	/**
	 * Removes the selected working sets from the workbench.
	 */
	protected void removeSelectedWorkingSets() {
		List<IWorkingSet> selection = getSelectedWorkingSets();
		removeSelectedWorkingSets(selection);
	}

	/**
	 * Remove the working sets contained in the provided selection from the working
	 * set manager.
	 *
	 * @param selection the sets
	 */
	protected void removeSelectedWorkingSets(List<IWorkingSet> selection) {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		for (IWorkingSet workingSet : selection) {
			if (getAddedWorkingSets().contains(workingSet)) {
				getAddedWorkingSets().remove(workingSet);
			} else {
				IWorkingSet[] recentWorkingSets = manager.getRecentWorkingSets();
				for (IWorkingSet recentWorkingSet : recentWorkingSets) {
					if (workingSet.equals(recentWorkingSet)) {
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
