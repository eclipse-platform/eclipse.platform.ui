/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - bug 201661
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 483425, 483429, 483435
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.SimpleWorkingSetSelectionDialog;

/**
 * Instances of this class provide a reusable composite with controls that allow
 * the selection of working sets. This class is most useful in
 * {@link IWizardPage} instances that wish to create resources and pre-install
 * them into particular working sets.
 *
 * @since 3.4
 */
public class WorkingSetConfigurationBlock {

	/**
	 * Filters the given working sets such that the following is true: for each
	 * IWorkingSet s in result: s.getId() is element of workingSetIds
	 *
	 * @param workingSets   the array to filter
	 * @param workingSetIds the acceptable working set ids
	 * @return the filtered elements
	 */
	public static IWorkingSet[] filter(IWorkingSet[] workingSets, String[] workingSetIds) {

		// create a copy so we can sort the array without mucking it up for clients.
		String[] workingSetIdsCopy = new String[workingSetIds.length];
		System.arraycopy(workingSetIds, 0, workingSetIdsCopy, 0, workingSetIds.length);
		Arrays.sort(workingSetIdsCopy);

		List<IWorkingSet> result = new ArrayList<>();

		for (IWorkingSet workingSet : workingSets) {
			if (Arrays.binarySearch(workingSetIdsCopy, workingSet.getId()) >= 0) {
				result.add(workingSet);
			}
		}

		return result.toArray(new IWorkingSet[result.size()]);
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
	private Button enableButton;
	private Button newButton;

	private IWorkingSet[] selectedWorkingSets;
	private List<String> selectionHistory;
	private final IDialogSettings dialogSettings;
	private final String[] workingSetTypeIds;

	private final String selectLabel;

	private final String comboLabel;

	private final String enableButtonLabel;

	private final String newButtonLabel;

	/**
	 * Create a new instance of this working set block using default labels.
	 *
	 * @param settings      to store/load the selection history
	 * @param workingSetIds working set ids from which the user can choose
	 * @since 3.108
	 */
	public WorkingSetConfigurationBlock(IDialogSettings settings, String... workingSetIds) {
		this(settings, null, null, null, null, workingSetIds);
	}

	/**
	 * Create a new instance of this working set block using default labels.
	 *
	 * <p>
	 * Note: Consider using the vararg version of this contructor.
	 * </p>
	 *
	 * @param workingSetIds working set ids from which the user can choose
	 * @param settings      to store/load the selection history
	 */
	public WorkingSetConfigurationBlock(String[] workingSetIds, IDialogSettings settings) {
		this(settings, workingSetIds);
	}

	/**
	 * Create a new instance of this working set block using custom labels.
	 *
	 * <p>
	 * Note: Consider using the vararg version of this contructor.
	 * </p>
	 *
	 * @param workingSetIds     working set ids from which the user can choose
	 * @param settings          to store/load the selection history
	 * @param enableButtonLabel the label to use for the checkable enablement
	 *                          button. May be <code>null</code> to use the default
	 *                          value.
	 * @param comboLabel        the label to use for the recent working set combo.
	 *                          May be <code>null</code> to use the default value.
	 * @param selectLabel       the label to use for the select button. May be
	 *                          <code>null</code> to use the default value.
	 */
	public WorkingSetConfigurationBlock(String[] workingSetIds, IDialogSettings settings, String enableButtonLabel,
			String comboLabel, String selectLabel) {
		this(settings, enableButtonLabel, null, comboLabel, selectLabel, workingSetIds);
	}

	/**
	 * Create a new instance of this working set block using custom labels.
	 *
	 * @param settings          to store/load the selection history
	 * @param enableButtonLabel the label to use for the checkable enablement
	 *                          button. May be <code>null</code> to use the default
	 *                          value.
	 * @param newButtonLabel    the label to use for the new button. May be
	 *                          <code>null</code> to use the default value.
	 * @param comboLabel        the label to use for the recent working set combo.
	 *                          May be <code>null</code> to use the default value.
	 * @param selectLabel       the label to use for the select button. May be
	 *                          <code>null</code> to use the default value.
	 * @param workingSetIds     working set ids from which the user can choose
	 * @since 3.108
	 */
	public WorkingSetConfigurationBlock(IDialogSettings settings, String enableButtonLabel, String newButtonLabel,
			String comboLabel, String selectLabel, String... workingSetIds) {
		Assert.isNotNull(workingSetIds);
		Assert.isNotNull(settings);

		workingSetTypeIds = workingSetIds;
		Arrays.sort(workingSetIds); // we'll be performing some searches with these later - presort them
		selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
		dialogSettings = settings;
		selectionHistory = loadSelectionHistory(settings, workingSetIds);

		this.enableButtonLabel = enableButtonLabel == null ? WorkbenchMessages.WorkingSetGroup_EnableWorkingSet_button
				: enableButtonLabel;
		this.newButtonLabel = newButtonLabel == null
				? WorkbenchMessages.WorkingSetConfigurationBlock_NewWorkingSet_button
				: newButtonLabel;
		this.comboLabel = comboLabel == null ? WorkbenchMessages.WorkingSetConfigurationBlock_WorkingSetText_name
				: comboLabel;
		this.selectLabel = selectLabel == null ? WorkbenchMessages.WorkingSetConfigurationBlock_SelectWorkingSet_button
				: selectLabel;
	}

	/**
	 * Set the current selection in the workbench.
	 *
	 * @param selection the selection to present in the UI or <b>null</b>
	 * @deprecated use {@link #setWorkingSets(IWorkingSet[])} and
	 *             {@link #findApplicableWorkingSets(IStructuredSelection)} instead.
	 */
	@Deprecated
	public void setSelection(IStructuredSelection selection) {
		selectedWorkingSets = findApplicableWorkingSets(selection);

		if (workingSetCombo != null) {
			updateSelectedWorkingSets();
		}
	}

	/**
	 * Set the current selection of working sets. This array will be filtered to
	 * contain only working sets that are applicable to this instance.
	 *
	 * @param workingSets the working sets
	 */
	public void setWorkingSets(IWorkingSet... workingSets) {
		selectedWorkingSets = filterWorkingSets(Arrays.asList(workingSets));
		if (workingSetCombo != null) {
			updateSelectedWorkingSets();
		}
	}

	/**
	 * Retrieves a working set from the given <code>selection</code> or an empty
	 * array if no working set could be retrieved. This selection is filtered based
	 * on the criteria used to construct this instance.
	 *
	 * @param selection the selection to retrieve the working set from
	 * @return the selected working set or an empty array
	 */
	public IWorkingSet[] findApplicableWorkingSets(IStructuredSelection selection) {
		if (selection == null) {
			return EMPTY_WORKING_SET_ARRAY;
		}

		return filterWorkingSets(selection.toList());
	}

	/**
	 * Prune a list of working sets such that they all match the criteria set out by
	 * this block.
	 *
	 * @param elements the elements to filter
	 * @return the filtered elements
	 */
	private IWorkingSet[] filterWorkingSets(Collection<?> elements) {
		List<IWorkingSet> result = new ArrayList<>();
		for (Object element : elements) {
			if (element instanceof IWorkingSet && verifyWorkingSet((IWorkingSet) element)) {
				result.add((IWorkingSet) element);
			}
		}
		return result.toArray(new IWorkingSet[result.size()]);
	}

	/**
	 * Verifies that the given working set is suitable for selection in this block.
	 *
	 * @param workingSetCandidate the candidate to test
	 * @return whether it is suitable
	 */
	private boolean verifyWorkingSet(IWorkingSet workingSetCandidate) {
		return !workingSetCandidate.isAggregateWorkingSet()
				&& Arrays.binarySearch(workingSetTypeIds, workingSetCandidate.getId()) >= 0;
	}

	/**
	 * Return the currently selected working sets. If the controls representing this
	 * block are disabled this array will be empty regardless of the currently
	 * selected values.
	 *
	 * @return the selected working sets
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (enableButton.getSelection()) {
			return selectedWorkingSets;
		}
		return EMPTY_WORKING_SET_ARRAY;
	}

	/**
	 * Add this block to the <code>parent</code>
	 *
	 * @param parent the parent to add the block to
	 */
	public void createContent(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		composite.setLayout(new GridLayout(3, false));

		enableButton = new Button(composite, SWT.CHECK);
		enableButton.setText(enableButtonLabel);
		GridData enableData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		enableData.horizontalSpan = 2;
		enableButton.setLayoutData(enableData);
		enableButton.setSelection(selectedWorkingSets.length > 0);

		newButton = new Button(composite, SWT.PUSH);
		newButton.setText(this.newButtonLabel);
		setButtonLayoutData(newButton);
		newButton.addSelectionListener(widgetSelectedAdapter(e -> createNewWorkingSet(newButton.getShell())));

		workingSetLabel = new Label(composite, SWT.NONE);
		workingSetLabel.setText(comboLabel);

		workingSetCombo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
		GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textData.horizontalIndent = 0;
		workingSetCombo.setLayoutData(textData);

		selectButton = new Button(composite, SWT.PUSH);
		selectButton.setText(selectLabel);
		setButtonLayoutData(selectButton);
		selectButton.addSelectionListener(widgetSelectedAdapter(e -> {
			SimpleWorkingSetSelectionDialog dialog = new SimpleWorkingSetSelectionDialog(parent.getShell(),
					workingSetTypeIds, selectedWorkingSets, false);
			dialog.setMessage(WorkbenchMessages.WorkingSetGroup_WorkingSetSelection_message);

			if (dialog.open() == Window.OK) {
				IWorkingSet[] result = dialog.getSelection();
				if (result != null && result.length > 0) {
					selectedWorkingSets = result;
					PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(result[0]);
				} else {
					selectedWorkingSets = EMPTY_WORKING_SET_ARRAY;
				}
				updateWorkingSetSelection();
			}
		}));

		enableButton.addSelectionListener(widgetSelectedAdapter(e -> updateEnableState(enableButton.getSelection())));
		updateEnableState(enableButton.getSelection());

		workingSetCombo.addSelectionListener(widgetSelectedAdapter(e -> updateSelectedWorkingSets()));

		workingSetCombo.setItems(getHistoryEntries());
		if (selectedWorkingSets.length == 0 && selectionHistory.size() > 0) {
			workingSetCombo.select(historyIndex(selectionHistory.get(0)));
			updateSelectedWorkingSets();
		} else {
			updateWorkingSetSelection();
		}
	}

	private void createNewWorkingSet(Shell shell) {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		IWorkingSetNewWizard wizard = manager.createWorkingSetNewWizard(workingSetTypeIds);
		// the wizard can never be null since we have at least a resource
		// working set creation page
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.WORKING_SET_NEW_WIZARD);
		if (dialog.open() == Window.OK) {
			IWorkingSet workingSet = wizard.getSelection();
			if (workingSet != null) {
				manager.addWorkingSet(workingSet);
				selectedWorkingSets = new IWorkingSet[] { workingSet };
				PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(workingSet);
			}
			enableButton.setSelection(true);
			updateEnableState(true);
			updateWorkingSetSelection();
		}
	}

	private void updateEnableState(boolean enabled) {
		workingSetLabel.setEnabled(enabled);
		workingSetCombo.setEnabled(enabled && (selectedWorkingSets.length > 0 || getHistoryEntries().length > 0));
		selectButton.setEnabled(enabled);
	}

	private void updateWorkingSetSelection() {
		if (selectedWorkingSets.length > 0) {
			workingSetCombo.setEnabled(true);
			StringBuilder buf = new StringBuilder();

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
		String[] history = selectionHistory.toArray(new String[selectionHistory.size()]);
		Arrays.sort(history, (o1, o2) -> Collator.getInstance().compare(o1, o2));
		return history;
	}

	private void historyInsert(String entry) {
		selectionHistory.remove(entry);
		selectionHistory.add(0, entry);
		storeSelectionHistory(dialogSettings);
	}

	private int historyIndex(String entry) {
		for (int i = 0; i < workingSetCombo.getItemCount(); i++) {
			if (workingSetCombo.getItem(i).equals(entry)) {
				return i;
			}
		}

		return -1;
	}

	// copied from org.eclipse.jdt.internal.ui.text.JavaCommentScanner
	private String[] split(String value, String delimiters) {
		StringTokenizer tokenizer = new StringTokenizer(value, delimiters);
		int size = tokenizer.countTokens();
		String[] tokens = new String[size];
		int i = 0;
		while (i < size) {
			tokens[i++] = tokenizer.nextToken();
		}
		return tokens;
	}

	private void updateSelectedWorkingSets() {
		String item = workingSetCombo.getItem(workingSetCombo.getSelectionIndex());
		String[] workingSetNames = split(item, ", "); //$NON-NLS-1$

		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		selectedWorkingSets = new IWorkingSet[workingSetNames.length];
		for (int i = 0; i < workingSetNames.length; i++) {
			IWorkingSet set = workingSetManager.getWorkingSet(workingSetNames[i]);
			Assert.isNotNull(set);
			selectedWorkingSets[i] = set;
		}
	}

	private void storeSelectionHistory(IDialogSettings settings) {
		String[] history;
		if (selectionHistory.size() > MAX_HISTORY_SIZE) {
			List<String> subList = selectionHistory.subList(0, MAX_HISTORY_SIZE);
			history = subList.toArray(new String[subList.size()]);
		} else {
			history = selectionHistory.toArray(new String[selectionHistory.size()]);
		}
		settings.put(WORKINGSET_SELECTION_HISTORY, history);
	}

	private List<String> loadSelectionHistory(IDialogSettings settings, String... workingSetIds) {
		String[] strings = settings.getArray(WORKINGSET_SELECTION_HISTORY);
		if (strings == null || strings.length == 0) {
			return new ArrayList<>();
		}

		List<String> result = new ArrayList<>();

		Set<String> workingSetIdsSet = new HashSet<>(Arrays.asList(workingSetIds));

		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		for (String string : strings) {
			String[] workingSetNames = split(string, ", "); //$NON-NLS-1$
			boolean valid = true;
			for (int j = 0; j < workingSetNames.length && valid; j++) {
				IWorkingSet workingSet = workingSetManager.getWorkingSet(workingSetNames[j]);
				if (workingSet == null) {
					valid = false;
				} else if (!workingSetIdsSet.contains(workingSet.getId())) {
					valid = false;
				}
			}
			if (valid) {
				result.add(string);
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