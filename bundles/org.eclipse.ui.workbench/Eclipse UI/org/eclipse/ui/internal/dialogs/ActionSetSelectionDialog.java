package org.eclipse.ui.internal.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *      IBM Corporation - initial API and implementation 
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *         font should be activated and used by other components.
 ******************************************************************************/ 
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.*;

/**
 * Dialog to display the available action sets, and
 * solicits a list of selections from the user.
 */
public class ActionSetSelectionDialog extends Dialog {
	// input data.
	private Perspective perspective;
	private ActionSetDialogInput input;
	
	// widgets.
	private CheckboxTreeViewer actionSetViewer;
	private Label actionLabel;
	private TableViewer actionViewer;
	
	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 300;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
/**
 * Creates an action set selection dialog.
 */
public ActionSetSelectionDialog(
		Shell parentShell,
		Perspective persp)
{
	super(parentShell);
	perspective = persp;
	input = new ActionSetDialogInput();
}
/**
 * Visually checks the previously-specified elements.
 */
private void checkInitialSelections() {
	IActionSetDescriptor [] actionSets = perspective.getActionSets();
	if (actionSets != null) {
		for (int i = 0; i < actionSets.length; i++)
			actionSetViewer.setChecked(actionSets[i],true);
	}

	ArrayList actions = perspective.getShowViewActionIds();
	if (actions != null) {
		for (int nX = 0; nX < actions.size(); nX ++) {
			String id = (String)actions.get(nX);
			actionSetViewer.setChecked(input.getViewActionSet(id), true);
		}
	}

	actions = perspective.getPerspectiveActionIds();
	if (actions != null) {
		for (int nX = 0; nX < actions.size(); nX ++) {
			String id = (String)actions.get(nX);
			actionSetViewer.setChecked(input.getPerspectiveActionSet(id), true);
		}
	}

	actions = perspective.getNewWizardActionIds();
	if (actions != null) {
		for (int nX = 0; nX < actions.size(); nX ++) {
			String id = (String)actions.get(nX);
			actionSetViewer.setChecked(input.getWizardActionSet(id), true);
		}
	}

	Object[] categories = input.getCategories();
	for (int i = 0; i < categories.length; i++) {
		ActionSetCategory cat = (ActionSetCategory)categories[i];
		ArrayList sets = cat.getActionSets();
		if (sets != null && sets.size() > 0) {
			boolean baseChildState = actionSetViewer.getChecked(sets.get(0));
			updateCategoryState(cat, baseChildState);
		}
	}
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("ActionSetSelection.customize")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, IHelpContextIds.ACTION_SET_SELECTION_DIALOG);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	Composite composite = (Composite)super.createDialogArea(parent);
	GridLayout layout = (GridLayout)composite.getLayout();
	layout.numColumns = 2;
	layout.makeColumnsEqualWidth = true;
	
	GridData data;
	Font font = parent.getFont();

	// description
	Label descLabel = new Label(composite, SWT.WRAP);
	descLabel.setText(WorkbenchMessages.format("ActionSetSelection.selectLabel", new Object[] {perspective.getDesc().getLabel()})); //$NON-NLS-1$
	descLabel.setFont(font);
	data = new GridData(GridData.FILL_HORIZONTAL);
	data.horizontalSpan = 2;
	descLabel.setLayoutData(data);
	
	// Setup the action set list selection...
	// ...first a composite group
	Composite actionSetGroup = new Composite(composite, SWT.NONE);
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	actionSetGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	actionSetGroup.setLayoutData(data);
	actionSetGroup.setFont(font);
	
	// ...second the label
	Label selectionLabel = new Label(actionSetGroup,SWT.NONE);
	selectionLabel.setText(WorkbenchMessages.getString("ActionSetSelection.available")); //$NON-NLS-1$
	selectionLabel.setFont(font);

	// ...third the checkbox list
	actionSetViewer = new CheckboxTreeViewer(actionSetGroup, SWT.BORDER);
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
	data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
	actionSetViewer.getTree().setLayoutData(data);
	actionSetViewer.getTree().setFont(font);
	actionSetViewer.setLabelProvider(new ActionSetLabelProvider());
	actionSetViewer.setContentProvider(new ActionSetContentProvider());
	actionSetViewer.setInput(input);
	actionSetViewer.setSorter(new ActionSetSorter());
	actionSetViewer.addSelectionChangedListener(
		new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection)event.getSelection();
				IActionSetDescriptor actionSet = null;
				if (sel.getFirstElement() instanceof IActionSetDescriptor)
					actionSet = (IActionSetDescriptor)sel.getFirstElement();
				if (actionSet != actionViewer.getInput()) {
					actionViewer.setInput(actionSet);
				}
			}
		});
	actionSetViewer.addCheckStateListener(new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleActionSetChecked(event);
		}
	});

	// Setup the action list for the action set selected...
	// ...first a composite group
	Composite actionGroup = new Composite(composite, SWT.NONE);
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	actionGroup.setLayout(layout);
	data = new GridData(GridData.FILL_BOTH);
	actionGroup.setLayoutData(data);
	actionGroup.setFont(font);
	
	// ...second the label
	actionLabel = new Label(actionGroup, SWT.NONE);
	actionLabel.setText(WorkbenchMessages.getString("ActionSetSelection.details")); //$NON-NLS-1$
	actionLabel.setFont(font);

	// ...third the list of actions
	Table actionTable = new Table(actionGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	actionTable.setBackground(WorkbenchColors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	data = new GridData(GridData.FILL_BOTH);
	data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
	data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
	actionTable.setLayoutData(data);
	actionTable.setFont(font);

	actionViewer = new TableViewer(actionTable);
	actionViewer.setLabelProvider(new WorkbenchLabelProvider());
	actionViewer.setContentProvider(new WorkbenchContentProvider());
	actionViewer.setSorter(new WorkbenchViewerSorter());
	
	// initialize page
	checkInitialSelections();

	return composite;
}
/**
 * Checked event handler for the action set tree.
 */
private void handleActionSetChecked(CheckStateChangedEvent event) {
	// On action set category check/uncheck. Category can be
	// in three states:
	//		1) all children unchecked -> category unchecked
	//		2) some children checked  -> category checked & grayed
	//		3) all children checked   -> category checked 
	if (event.getElement() instanceof ActionSetCategory) {
		// On check, check all its children also
		if (event.getChecked()) {
			actionSetViewer.setSubtreeChecked(event.getElement(), true);
			return;
		}
		// On uncheck & gray, remain check but ungray
		// and check all its children
		if (actionSetViewer.getGrayed(event.getElement())) {
			actionSetViewer.setChecked(event.getElement(), true);
			actionSetViewer.setGrayed(event.getElement(), false);
			actionSetViewer.setSubtreeChecked(event.getElement(), true);
			return;
		}
		// On uncheck & not gray, uncheck all its children
		actionSetViewer.setSubtreeChecked(event.getElement(), false);
		return;
	}

	// On action set check/uncheck
	if (event.getElement() instanceof IActionSetDescriptor) {
		IActionSetDescriptor desc = (IActionSetDescriptor)event.getElement();
		ActionSetCategory cat = input.findCategory(desc.getCategory());
		updateCategoryState(cat, event.getChecked());
		return;
	}
}
/**
 * The <code>ActionSetSelectionDialog</code> implementation of this 
 * <code>Dialog</code> method builds a list of the selected elements for later
 * retrieval by the client and closes this dialog.
 */
protected void okPressed() {
	// Prepare result arrays.
	ArrayList actionSets = new ArrayList();
	ArrayList viewActions = new ArrayList();
	ArrayList perspActions = new ArrayList();
	ArrayList wizardActions = new ArrayList();

	// Fill result arrays.
	Object[] selected = actionSetViewer.getCheckedElements();
	for (int nX = 0; nX < selected.length; nX ++) {
		Object obj = selected[nX];
		if (obj instanceof FakeViewActionSet) {
			viewActions.add(((FakeViewActionSet)obj).getView().getID());
		} else if (obj instanceof FakePerspectiveActionSet) {
			perspActions.add(((FakePerspectiveActionSet)obj).getPerspective().getId());
		} else if (obj instanceof FakeWizardActionSet) {
			wizardActions.add(((FakeWizardActionSet)obj).getWizard().getID());
		} else if (obj instanceof ActionSetDescriptor) {
			actionSets.add(obj);
		}
	}

	perspective.setShowViewActionIds(viewActions);
	perspective.setPerspectiveActionIds(perspActions);
	perspective.setNewWizardActionIds(wizardActions);

	IActionSetDescriptor [] actionSetArray = new IActionSetDescriptor[actionSets.size()];
	actionSetArray = (IActionSetDescriptor [])actionSets.toArray(actionSetArray);
	perspective.setActionSets(actionSetArray);
	
	super.okPressed();
}
/**
 * Update the check and gray state of a category
 * Category can be in three states:
 * 	1) all children uncheck -> category uncheck
 * 	2) some children check  -> category check & gray
 * 	3) all children check   -> category check
 */
private void updateCategoryState(ActionSetCategory cat, boolean baseChildState) {
	// Check if all the action sets of the category are all
	// in the same state at the action set of the event
	boolean allSameState = true;
	Iterator enum = cat.getActionSets().iterator();
	while (enum.hasNext()) {
		if (actionSetViewer.getChecked(enum.next()) != baseChildState) {
			allSameState = false;
			break;
		}
	}

	// On all the same state, ungray the category and
	// set the category's state to be the same
	if (allSameState) {
		actionSetViewer.setGrayed(cat, false);
		actionSetViewer.setChecked(cat, baseChildState);
		return;
	}

	// On all different state, gray the category and
	// check the category
	actionSetViewer.setGrayed(cat, true);
	actionSetViewer.setChecked(cat, true);
	return;
}
}
