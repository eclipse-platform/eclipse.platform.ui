/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.SimpleWorkingSetSelectionDialog;

import com.ibm.icu.text.Collator;

/**
 * Instances of this class provide a reusable composite with controls that allow
 * the selection of working sets. This class is most useful in
 * {@link IWizardPage} instances that wish to create resources and pre-install
 * them into particular working sets.
 * 
 * <strong>Please note that this API is experimental and may change before 3.4
 * ships.</strong>
 * 
 * @since 3.4
 * 
 */
public class WorkingSetConfigurationBlock {

	/**
	 * Retrieves a working set from the given <code>selection</code> or an
	 * empty array if no working set could be retrieved.
	 * 
	 * @param selection
	 *            the selection to retrieve the working set from
	 * @return the selected working set or an empty array
	 */
	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection) {
		if (!(selection instanceof ITreeSelection))
			return EMPTY_WORKING_SET_ARRAY;

		ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty())
			return EMPTY_WORKING_SET_ARRAY;

		List elements = treeSelection.toList();
		if (elements.size() == 1) {
			Object element = elements.get(0);
			TreePath[] paths = treeSelection.getPathsFor(element);
			if (paths.length != 1)
				return EMPTY_WORKING_SET_ARRAY;

			TreePath path = paths[0];
			if (path.getSegmentCount() == 0)
				return EMPTY_WORKING_SET_ARRAY;

			Object candidate = path.getSegment(0);
			if (!(candidate instanceof IWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			return new IWorkingSet[] { (IWorkingSet) candidate };
		}

		ArrayList result = new ArrayList();
		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			Object element = iterator.next();
			if (element instanceof IWorkingSet) {
				result.add(element);
			}
		}
		return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);

	}

	/**
	 * Filters the given working sets such that the following is true: for each
	 * IWorkingSet s in result: s.getId() is element of workingSetIds
	 * 
	 * @param workingSets
	 *            the array to filter
	 * @param workingSetIds
	 *            the acceptable working set ids
	 * @return the filtered elements
	 */
	public static IWorkingSet[] filter(IWorkingSet[] workingSets,
			String[] workingSetIds) {
		ArrayList result = new ArrayList();

		for (int i = 0; i < workingSets.length; i++) {
			if (accept(workingSets[i], workingSetIds))
				result.add(workingSets[i]);
		}

		return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);
	}

	private static boolean accept(IWorkingSet set, String[] workingSetIDs) {
		for (int i = 0; i < workingSetIDs.length; i++) {
			if (workingSetIDs[i].equals(set.getId()))
				return true;
		}

		return false;
	}

	/**
	 * Empty working set array constant.
	 */
	private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

	private static final String WORKINGSET_SELECTION_HISTORY = "workingset_selection_history"; //$NON-NLS-1$
	private static final int MAX_HISTORY_SIZE = 5;

	private Label workingSetLabel;
	private Combo workingSetCombo;
	private Button selectButton;
	private IWorkingSet[] selectedWorkingSets;
	private Button enableButton;
	private ArrayList selectionHistory;
	private final IDialogSettings dialogSettings;
	private final String[] workingSetTypeIds;

	/**
	 * Create a new instance of this working set block.
	 * 
	 * @param workingSetIds
	 *            working set ids from which the user can choose
	 * @param settings
	 *            to store/load the selection history
	 */
	public WorkingSetConfigurationBlock(String[] workingSetIds,
			IDialogSettings settings) {
		Assert.isNotNull(workingSetIds);
		Assert.isNotNull(settings);

		workingSetTypeIds = workingSetIds;
		selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
		dialogSettings = settings;
		selectionHistory = loadSelectionHistory(settings, workingSetIds);
	}

	/**
	 * Set the current selection in the workbench.
	 * 
	 * @param selection
	 *            the selection to present in the UI or <b>null</b>
	 */
	public void setSelection(IStructuredSelection selection) {
		selectedWorkingSets = getSelectedWorkingSet(selection);
		if (workingSetCombo != null)
			updateSelectedWorkingSets();
	}

	/**
	 * @return the selected working sets
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (enableButton.getSelection()) {
			return selectedWorkingSets;
		}
		return EMPTY_WORKING_SET_ARRAY;
	}

	/**
	 * Add this block to the <code>parent</parent>
	 * 
	 * @param parent the parent to add the block to
	 */
	public void createContent(final Composite parent) {
		int numColumn = 3;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		composite.setLayout(new GridLayout(numColumn, false));

		enableButton = new Button(composite, SWT.CHECK);
		enableButton
				.setText(WorkbenchMessages.WorkingSetGroup_EnableWorkingSet_button);
		GridData enableData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		enableData.horizontalSpan = numColumn;
		enableButton.setLayoutData(enableData);
		enableButton.setSelection(selectedWorkingSets.length > 0);

		workingSetLabel = new Label(composite, SWT.NONE);
		workingSetLabel
				.setText(WorkbenchMessages.WorkingSetConfigurationBlock_WorkingSetText_name);

		workingSetCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.horizontalSpan = numColumn - 2;
		textData.horizontalIndent = 0;
		workingSetCombo.setLayoutData(textData);

		selectButton = new Button(composite, SWT.PUSH);
		selectButton
				.setText(WorkbenchMessages.WorkingSetConfigurationBlock_SelectWorkingSet_button);
		setButtonLayoutData(selectButton);
		selectButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				SimpleWorkingSetSelectionDialog dialog = new SimpleWorkingSetSelectionDialog(
						parent.getShell(), workingSetTypeIds,
						selectedWorkingSets, false);
				dialog
						.setMessage(WorkbenchMessages.WorkingSetGroup_WorkingSetSelection_message);

				if (dialog.open() == Window.OK) {
					IWorkingSet[] result = dialog.getSelection();
					if (result != null && result.length > 0) {
						selectedWorkingSets = result;
						PlatformUI.getWorkbench().getWorkingSetManager()
								.addRecentWorkingSet(result[0]);
					} else {
						selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
					}
					updateWorkingSetSelection();
				}
			}
		});

		enableButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnableState(enableButton.getSelection());
			}
		});
		updateEnableState(enableButton.getSelection());

		workingSetCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSelectedWorkingSets();
			}
		});

		workingSetCombo.setItems(getHistoryEntries());
		if (selectedWorkingSets.length == 0 && selectionHistory.size() > 0) {
			workingSetCombo.select(historyIndex((String) selectionHistory
					.get(0)));
			updateSelectedWorkingSets();
		} else {
			updateWorkingSetSelection();
		}
	}

	private void updateEnableState(boolean enabled) {
		workingSetLabel.setEnabled(enabled);
		workingSetCombo
				.setEnabled(enabled
						&& (selectedWorkingSets.length > 0 || getHistoryEntries().length > 0));
		selectButton.setEnabled(enabled);
	}

	private void updateWorkingSetSelection() {
		if (selectedWorkingSets.length > 0) {
			workingSetCombo.setEnabled(true);
			StringBuffer buf = new StringBuffer();

			buf.append(selectedWorkingSets[0].getLabel());
			for (int i = 1; i < selectedWorkingSets.length; i++) {
				IWorkingSet ws = selectedWorkingSets[i];
				buf.append(',').append(' ');
				buf.append(ws.getLabel());
			}

			String currentSelection = buf.toString();
			int index = historyIndex(currentSelection);
			historyInsert(currentSelection);
			if (index >= 0) {
				workingSetCombo.select(index);
			} else {
				workingSetCombo.setItems(getHistoryEntries());
				workingSetCombo.select(historyIndex(currentSelection));
			}
		} else {
			enableButton.setSelection(false);
			updateEnableState(false);
		}
	}

	private String[] getHistoryEntries() {
		String[] history = (String[]) selectionHistory
				.toArray(new String[selectionHistory.size()]);
		Arrays.sort(history, new Comparator() {
			public int compare(Object o1, Object o2) {
				return Collator.getInstance().compare(o1, o2);
			}
		});
		return history;
	}

	private void historyInsert(String entry) {
		selectionHistory.remove(entry);
		selectionHistory.add(0, entry);
		storeSelectionHistory(dialogSettings);
	}

	private int historyIndex(String entry) {
		for (int i = 0; i < workingSetCombo.getItemCount(); i++) {
			if (workingSetCombo.getItem(i).equals(entry))
				return i;
		}

		return -1;
	}

	private void updateSelectedWorkingSets() {
		String item = workingSetCombo.getItem(workingSetCombo
				.getSelectionIndex());
		String[] workingSetNames = item.split(", "); //$NON-NLS-1$

		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		selectedWorkingSets = new IWorkingSet[workingSetNames.length];
		for (int i = 0; i < workingSetNames.length; i++) {
			IWorkingSet set = workingSetManager
					.getWorkingSet(workingSetNames[i]);
			Assert.isNotNull(set);
			selectedWorkingSets[i] = set;
		}
	}

	private void storeSelectionHistory(IDialogSettings settings) {
		String[] history;
		if (selectionHistory.size() > MAX_HISTORY_SIZE) {
			List subList = selectionHistory.subList(0, MAX_HISTORY_SIZE);
			history = (String[]) subList.toArray(new String[subList.size()]);
		} else {
			history = (String[]) selectionHistory
					.toArray(new String[selectionHistory.size()]);
		}
		settings.put(WORKINGSET_SELECTION_HISTORY, history);
	}

	private ArrayList loadSelectionHistory(IDialogSettings settings,
			String[] workingSetIds) {
		String[] strings = settings.getArray(WORKINGSET_SELECTION_HISTORY);
		if (strings == null || strings.length == 0)
			return new ArrayList();

		ArrayList result = new ArrayList();

		HashSet workingSetIdsSet = new HashSet(Arrays.asList(workingSetIds));

		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
				.getWorkingSetManager();
		for (int i = 0; i < strings.length; i++) {
			String[] workingSetNames = strings[i].split(", "); //$NON-NLS-1$
			boolean valid = true;
			for (int j = 0; j < workingSetNames.length && valid; j++) {
				IWorkingSet workingSet = workingSetManager
						.getWorkingSet(workingSetNames[j]);
				if (workingSet == null) {
					valid = false;
				} else {
					if (!workingSetIdsSet.contains(workingSet.getId()))
						valid = false;
				}
			}
			if (valid) {
				result.add(strings[i]);
			}
		}

		return result;
	}
	
	/*
	 * Copy from DialogPage with changes to accomodate the lack of a Dialog context.
	 */
	private GridData setButtonLayoutData(Button button) {
		button.setFont(JFaceResources.getDialogFont());

		GC gc = new GC(button);
		gc.setFont(button.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        return data;
    }
}