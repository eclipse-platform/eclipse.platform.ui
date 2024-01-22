/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 *   	should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * A working set selection dialog displays a list of working sets available in
 * the workbench.
 *
 * @see IWorkingSetSelectionDialog
 * @since 2.0
 */
public class WorkingSetSelectionDialog extends AbstractWorkingSetDialog {
	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private static final int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private ILabelProvider labelProvider;

	private IStructuredContentProvider contentProvider;

	private CheckboxTableViewer listViewer;

	private boolean multiSelect;

	private IWorkbenchWindow workbenchWindow;

	private Button buttonWindowSet;

	private Button buttonNoSet;

	private Button buttonSelectedSets;

	/**
	 * Creates a working set selection dialog.
	 *
	 * @param parentShell   the parent shell
	 * @param multi         decides how the results are returned with
	 *                      <code>WorkingSetSelectionDialog#getSelection()</code> or
	 *                      <code>WorkingSetSelectionDialog#getResult()</code>.
	 *                      true= working sets chosen in the dialog are returned as
	 *                      an array of working set.false= returns an array having a
	 *                      single aggregate working set of all working sets
	 *                      selected in the dialog.
	 * @param workingSetIds a list of working set ids which are valid workings sets
	 *                      to be selected, created, removed or edited, or
	 *                      <code>null</code> if all currently available working set
	 *                      types are valid
	 */
	public WorkingSetSelectionDialog(Shell parentShell, boolean multi, String[] workingSetIds) {
		super(parentShell, workingSetIds, true);
		initWorkbenchWindow();

		contentProvider = ArrayContentProvider.getInstance();
		labelProvider = new WorkingSetLabelProvider();
		multiSelect = multi;
		if (multiSelect) {
			setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title_multiSelect);
			setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message_multiSelect);
		} else {
			setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title);
			setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message);
		}

	}

	/**
	 * Determine what window this dialog is being opened on. This impacts the
	 * returned working set in the case where the user chooses the window set.
	 *
	 * @since 3.2
	 */
	private void initWorkbenchWindow() {
		Shell shellToCheck = getShell();

		workbenchWindow = Util.getWorkbenchWindowForShell(shellToCheck);
	}

	/**
	 * Overrides method from Dialog.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		restoreAddedWorkingSets();
		restoreChangedWorkingSets();
		restoreRemovedWorkingSets();
		setSelection(null);
		super.cancelPressed();
	}

	/**
	 * Overrides method from Window.
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IWorkbenchHelpContextIds.WORKING_SET_SELECTION_DIALOG);
	}

	/**
	 * Overrides method from Dialog. Create the dialog widgets.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = (Composite) super.createDialogArea(parent);

		createMessageArea(composite);

		ButtonFactory buttonFactory = WidgetFactory.button(SWT.RADIO).onSelect(e -> updateButtonAvailability());

		buttonWindowSet = buttonFactory.text(WorkbenchMessages.WindowWorkingSets).
				layoutData(new GridData(GridData.FILL_HORIZONTAL)).create(composite);

		buttonNoSet = buttonFactory.text(WorkbenchMessages.NoWorkingSet)
				.layoutData(new GridData(GridData.FILL_HORIZONTAL)).create(composite);

		buttonSelectedSets = buttonFactory.text(WorkbenchMessages.SelectedWorkingSets).create(composite);

		switch (getInitialRadioSelection()) {
		case 0:
			buttonWindowSet.setSelection(true);
			break;
		case 1:
			buttonNoSet.setSelection(true);
			break;
		case 2:
			buttonSelectedSets.setSelection(true);
			break;
		}
		buttonSelectedSets.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH + 300; // fudge? I like fudge.
		Composite viewerComposite = WidgetFactory.composite(SWT.NONE).layout(layout).layoutData(data).create(composite);

		listViewer = CheckboxTableViewer.newCheckList(viewerComposite, SWT.BORDER | SWT.MULTI);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);
		listViewer.setComparator(new WorkbenchViewerComparator());

		listViewer.addFilter(new WorkingSetFilter(getSupportedWorkingSetIds()));

		listViewer.addSelectionChangedListener(event -> handleSelectionChanged());
		listViewer.addDoubleClickListener(event -> {
			Object obj = listViewer.getStructuredSelection().getFirstElement();
			listViewer.setCheckedElements(new Object[] { obj });
			buttonWindowSet.setSelection(false);
			buttonNoSet.setSelection(false);
			buttonSelectedSets.setSelection(true);
			okPressed();
		});
		listViewer.addCheckStateListener(event -> {
			// implicitly select the third radio button
			buttonWindowSet.setSelection(false);
			buttonNoSet.setSelection(false);
			buttonSelectedSets.setSelection(true);
		});

		addModifyButtons(viewerComposite);

		addSelectionButtons(composite);

		listViewer.setInput(Arrays.asList(WorkbenchPlugin.getDefault().getWorkingSetManager().getWorkingSets()));
		List initialElementSelections = getInitialElementSelections();
		if (multiSelect) {
			listViewer.setCheckedElements(initialElementSelections.toArray());
		} else if (!initialElementSelections.isEmpty()) {
			IWorkingSet set = (IWorkingSet) initialElementSelections.get(0);
			if (set instanceof AggregateWorkingSet) {
				AggregateWorkingSet aggregate = (AggregateWorkingSet) set;
				listViewer.setCheckedElements(aggregate.getComponents());
			} else {
				listViewer.setCheckedElements(initialElementSelections.toArray());
			}
		}

		availableWorkingSetsChanged();
		Dialog.applyDialogFont(composite);

		return composite;
	}

	private int getInitialRadioSelection() {
		IWorkingSet windowSet = workbenchWindow.getActivePage().getAggregateWorkingSet();

		int selectionIndex;
		if (getSelection() != null && getSelection().length > 0) {
			if (windowSet.equals(getSelection()[0])) {
				selectionIndex = 0;
			} else {
				selectionIndex = 2;
			}
		} else {
			selectionIndex = 1;
		}

		return selectionIndex;
	}

	/**
	 * Overrides method from Dialog. Sets the initial selection, if any.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		List selections = getInitialElementSelections();
		if (!selections.isEmpty()) {
			listViewer.setSelection(new StructuredSelection(selections), true);
		}
		updateButtonAvailability();
		return control;
	}

	/**
	 * Returns the selected working sets.
	 *
	 * @return the selected working sets
	 */
	@Override
	protected List getSelectedWorkingSets() {
		return listViewer.getStructuredSelection().toList();
	}

	/**
	 * Called when the selection has changed.
	 */
	void handleSelectionChanged() {
		updateButtonAvailability();
	}

	/**
	 * Sets the selected working sets as the dialog result. Overrides method from
	 * Dialog
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (buttonWindowSet.getSelection()) {
			IWorkingSet[] windowSet = new IWorkingSet[] { workbenchWindow.getActivePage().getAggregateWorkingSet() };
			setSelection(windowSet);
			setResult(Arrays.asList(getSelection()));
		} else if (buttonNoSet.getSelection()) {
			setSelection(new IWorkingSet[0]);
			setResult(Arrays.asList(getSelection()));
		} else if (buttonSelectedSets.getSelection()) {
			Object[] untypedResult = listViewer.getCheckedElements();
			IWorkingSet[] typedResult = new IWorkingSet[untypedResult.length];
			System.arraycopy(untypedResult, 0, typedResult, 0, untypedResult.length);
			// if multiselect is allowed or there was only one selected then dont create
			// an aggregate
			if (multiSelect || typedResult.length <= 1) {
				setSelection(typedResult);
				setResult(Arrays.asList(typedResult));
			} else {
				String setId = getAggregateIdForSets(typedResult);
				IWorkingSetManager workingSetManager = workbenchWindow.getWorkbench().getWorkingSetManager();
				IWorkingSet aggregate = workingSetManager.getWorkingSet(setId);
				if (aggregate != null) {
					workingSetManager.removeWorkingSet(aggregate);
				}
				aggregate = workingSetManager.createAggregateWorkingSet(setId,
						WorkbenchMessages.WorkbenchPage_workingSet_multi_label, typedResult);
				workingSetManager.addWorkingSet(aggregate);
				setSelection(new IWorkingSet[] { aggregate });
				setResult(Collections.singletonList(aggregate));
			}
		}

		super.okPressed();
	}

	/**
	 * Create a string that represents the name of the aggregate set composed of the
	 * supplied working sets. It's very long and not printworthy.
	 *
	 * @param typedResult the sets
	 * @return the name
	 */
	private String getAggregateIdForSets(IWorkingSet[] typedResult) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Aggregate:"); //$NON-NLS-1$
		for (IWorkingSet element : typedResult) {
			buffer.append(element.getName()).append(':');
		}
		return buffer.toString();
	}

	/**
	 * Removes newly created working sets from the working set manager.
	 */
	private void restoreAddedWorkingSets() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		Iterator<IWorkingSet> iterator = getAddedWorkingSets().iterator();

		while (iterator.hasNext()) {
			manager.removeWorkingSet((iterator.next()));
		}
	}

	/**
	 * Rolls back changes to working sets.
	 */
	private void restoreChangedWorkingSets() {
		for (IWorkingSet editedWorkingSet : getEditedWorkingSets().keySet()) {
			IWorkingSet originalWorkingSet = getEditedWorkingSets().get(editedWorkingSet);

			if (editedWorkingSet.getName().equals(originalWorkingSet.getName()) == false) {
				editedWorkingSet.setName(originalWorkingSet.getName());
			}
			if (!Arrays.equals(editedWorkingSet.getElements(), originalWorkingSet.getElements())) {
				editedWorkingSet.setElements(originalWorkingSet.getElements());
			}
		}
	}

	/**
	 * Adds back removed working sets to the working set manager.
	 */
	private void restoreRemovedWorkingSets() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		Iterator<IWorkingSet> iterator = getRemovedWorkingSets().iterator();

		while (iterator.hasNext()) {
			manager.addWorkingSet((iterator.next()));
		}
		iterator = getRemovedMRUWorkingSets().iterator();
		while (iterator.hasNext()) {
			manager.addRecentWorkingSet((iterator.next()));
		}
	}

	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(IWorkingSet[])
	 */
	@Override
	public void setSelection(IWorkingSet[] workingSets) {
		super.setSelection(workingSets);
		setInitialSelections(workingSets == null ? new Object[0] : workingSets);
	}

	@Override
	protected void availableWorkingSetsChanged() {
		listViewer.setInput(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets());
		super.availableWorkingSetsChanged();
	}

	@Override
	protected void selectAllSets() {
		listViewer.setCheckedElements(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets());
		// implicitly select the third radio button
		buttonWindowSet.setSelection(false);
		buttonNoSet.setSelection(false);
		buttonSelectedSets.setSelection(true);
		updateButtonAvailability();
	}

	@Override
	protected void deselectAllSets() {
		listViewer.setCheckedElements(new Object[0]);
		// implicitly select the third radio button
		buttonWindowSet.setSelection(false);
		buttonNoSet.setSelection(false);
		buttonSelectedSets.setSelection(true);
		updateButtonAvailability();
	}
}
