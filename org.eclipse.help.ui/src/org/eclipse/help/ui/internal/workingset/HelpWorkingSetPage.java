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
package org.eclipse.help.ui.internal.workingset;


import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;

/**
 * Page for help working sets.
 */
public class HelpWorkingSetPage extends WizardPage implements IWorkingSetPage {

	public final static String PAGE_ID =
		WorkbenchHelpPlugin.PLUGIN_ID + ".HelpWorkingSetPage";
	public final static String PAGE_TITLE =
		WorkbenchResources.getString("WorkingSetPageTitle");
	public final static String PAGE_DESCRIPTION =
		WorkbenchResources.getString("WorkingSetPageDescription");

	private Text workingSetName;
	private CheckboxTreeViewer tree;
	private ITreeContentProvider treeContentProvider;
	private ILabelProvider elementLabelProvider;

	private boolean firstCheck;
	private IWorkingSet workingSet;

	/**
	 * Default constructor.
	 */
	public HelpWorkingSetPage() {
		super(PAGE_ID, PAGE_TITLE, null);
		setDescription(PAGE_DESCRIPTION);
		firstCheck = true;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label = new Label(composite, SWT.WRAP);
		label.setFont(font);
		label.setText(WorkbenchResources.getString("WorkingSetName"));
		GridData gd =
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		workingSetName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		workingSetName.setLayoutData(
			new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		workingSetName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		workingSetName.setFocus();
		workingSetName.setFont(font);

		label = new Label(composite, SWT.WRAP);
		label.setFont(font);
		label.setText(WorkbenchResources.getString("WorkingSetContent"));
		gd =
			new GridData(
				GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		tree =
			new CheckboxTreeViewer(
				composite,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint = convertHeightInCharsToPixels(15);
		tree.getControl().setLayoutData(gd);
		tree.getControl().setFont(font);

		treeContentProvider = new HelpWorkingSetTreeContentProvider();
		tree.setContentProvider(treeContentProvider);

		elementLabelProvider = new HelpWorkingSetElementLabelProvider();
		tree.setLabelProvider(elementLabelProvider);

		tree.setUseHashlookup(true);

		tree.setInput(HelpSystem.getWorkingSetManager().getRoot());

		tree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		tree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
			}
			public void treeExpanded(TreeExpansionEvent event) {
				final Object element = event.getElement();
				if (tree.getGrayed(element) == false)
					BusyIndicator
						.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						setSubtreeChecked(
							element,
							tree.getChecked(element),
							false);
					}
				});
			}
		});

		if (workingSet != null) {
			workingSetName.setText(workingSet.getName());
			// May need to reconcile working sets
			WorkbenchHelpPlugin
				.getDefault()
				.getWorkingSetSynchronizer()
				.addWorkingSet(
				workingSet);
		}
		initializeCheckedState();
		validateInput();

		// Set help for the page
		//WorkbenchHelp.setHelp(tree, "help_workingset_page");
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	public void finish() {
		String workingSetName = this.workingSetName.getText();
		ArrayList elements = new ArrayList(10);
		findCheckedElements(elements, tree.getInput());
		if (workingSet == null) {
			HelpWorkingSet ws =
				new HelpWorkingSet(
					workingSetName,
					(AdaptableHelpResource[]) elements.toArray(
						new AdaptableHelpResource[elements.size()]));
			workingSet = ws.getIWorkingSet();
		} else {
			workingSet.setName(workingSetName);
			workingSet.setElements(
				(IAdaptable[]) elements.toArray(
					new IAdaptable[elements.size()]));
		}
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	public IWorkingSet getSelection() {
		return workingSet;
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(org.eclipse.ui.IWorkingSet)
	 */
	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null");
		this.workingSet = workingSet;
		if (getContainer() != null
			&& getShell() != null
			&& workingSetName != null) {
			firstCheck = false;
			workingSetName.setText(workingSet.getName());
			initializeCheckedState();
			validateInput();
		}
	}

	private void validateInput() {
		String errorMessage = null;
		String newText = workingSetName.getText();

		if (newText.equals(newText.trim()) == false)
			errorMessage = WorkbenchResources.getString("WE030");
		if (newText.equals("")) {
			if (firstCheck) {
				setPageComplete(false);
				firstCheck = false;
				return;
			} else
				errorMessage = WorkbenchResources.getString("WE031");
		}

		firstCheck = false;

		if (errorMessage == null
			&& (workingSet == null
				|| newText.equals(workingSet.getName()) == false)) {
			IWorkingSet[] workingSets =
				PlatformUI
					.getWorkbench()
					.getWorkingSetManager()
					.getWorkingSets();
			for (int i = 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage = WorkbenchResources.getString("WE032");
				}
			}
		}
		if (errorMessage == null && tree.getCheckedElements().length == 0)
			errorMessage = WorkbenchResources.getString("WE033");

		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}

	private void initializeCheckedState() {
		if (workingSet == null)
			return;

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Object[] elements = workingSet.getElements();
				tree.setCheckedElements(elements);
				for (int i = 0; i < elements.length; i++) {
					Object element = elements[i];
					if (isExpandable(element))
						setSubtreeChecked(element, true, true);
					updateParentState(element, true);
				}
			}
		});
	}

	private boolean isExpandable(Object element) {
		return treeContentProvider.hasChildren(element);
	}

	private void updateParentState(Object child, boolean baseChildState) {
		if (child == null)
			return;

		Object parent = treeContentProvider.getParent(child);
		if (parent == null)
			return;

		boolean allSameState = true;
		Object[] children = null;
		children = treeContentProvider.getChildren(parent);

		for (int i = children.length - 1; i >= 0; i--) {
			if (tree.getChecked(children[i]) != baseChildState
				|| tree.getGrayed(children[i])) {
				allSameState = false;
				break;
			}
		}

		tree.setGrayed(parent, !allSameState);
		tree.setChecked(parent, !allSameState || baseChildState);

		updateParentState(parent, baseChildState);
	}

	private void setSubtreeChecked(
		Object parent,
		boolean state,
		boolean checkExpandedState) {

		Object[] children = treeContentProvider.getChildren(parent);
		for (int i = children.length - 1; i >= 0; i--) {
			Object element = children[i];
			if (state) {
				tree.setChecked(element, true);
				tree.setGrayed(element, false);
			} else
				tree.setGrayChecked(element, false);
			if (isExpandable(element))
				setSubtreeChecked(element, state, checkExpandedState);
		}
	}

	private void findCheckedElements(
		java.util.List checkedResources,
		Object parent) {
		Object[] children = treeContentProvider.getChildren(parent);
		for (int i = 0; i < children.length; i++) {
			if (tree.getGrayed(children[i]))
				findCheckedElements(checkedResources, children[i]);
			else if (tree.getChecked(children[i]))
				checkedResources.add(children[i]);
		}
	}

	void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Object element = event.getElement();
				boolean state = event.getChecked();
				tree.setGrayed(element, false);
				if (isExpandable(element))
					setSubtreeChecked(element, state, state);
				// only check subtree if state is set to true

				updateParentState(element, state);
				validateInput();
			}
		});
	}

}
