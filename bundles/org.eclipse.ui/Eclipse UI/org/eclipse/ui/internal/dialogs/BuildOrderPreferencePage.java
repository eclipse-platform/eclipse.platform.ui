package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import java.util.TreeSet;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;

/**
 * The BuildOrderPage is the page that is used to determine what order projects
 * will be built in by the workspace.
 */
public class BuildOrderPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
		
	private IWorkbench workbench;
		
	private Button defaultOrderButton;
	private Label buildLabel;
	private List buildList;
	private Composite buttonComposite;
	private Label noteLabel;
	
	private String[] defaultBuildOrder;
	private String[] customBuildOrder;

	private static String UP_LABEL = WorkbenchMessages.getString("BuildOrderPreference.up"); //$NON-NLS-1$
	private static String DOWN_LABEL = WorkbenchMessages.getString("BuildOrderPreference.down"); //$NON-NLS-1$
	private static String ADD_LABEL = WorkbenchMessages.getString("BuildOrderPreference.add"); //$NON-NLS-1$
	private static String REMOVE_LABEL = WorkbenchMessages.getString("BuildOrderPreference.remove"); //$NON-NLS-1$
	private static String UNSELECTED_PROJECTS = WorkbenchMessages.getString("BuildOrderPreference.selectProject"); //$NON-NLS-1$
	private static String PROJECT_SELECTION_MESSAGE = WorkbenchMessages.getString("BuildOrderPreference.selectOtherProjects"); //$NON-NLS-1$
	private static String DEFAULTS_LABEL = WorkbenchMessages.getString("BuildOrderPreference.useDefaults"); //$NON-NLS-1$
	private static String LIST_LABEL = WorkbenchMessages.getString("BuildOrderPreference.projectBuildOrder"); //$NON-NLS-1$
	private static String NOTE_LABEL = WorkbenchMessages.getString("BuildOrderPreference.note"); //$NON-NLS-1$
	
	// marks projects with unspecified build orders
	private static final String MARKER = "*"; //$NON-NLS-1$
	
	// the index of the first project with an unspecified build order
	// the rest of the list consists of projects with unspecified build orders
	private int markedItemsStartIndex = 0;
	
	// whether or not the use defaults option was selected when Apply (or OK) was last pressed
	// (or when the preference page was opened). This represents the most recent applied state.
	private boolean defaultOrderInitiallySelected;
	
/**
 * Add another project to the list at the end.
 */
private void addProject() {

	String[] currentItems = this.buildList.getItems();

	IProject[] allProjects = getWorkspace().getRoot().getProjects();
	
	ILabelProvider labelProvider = new LabelProvider() {
		public String getText(Object element) {
			return (String) element;
		}
	};

	SimpleListContentProvider contentsProvider = new SimpleListContentProvider();
	contentsProvider.setElements(sortedDifference(allProjects, currentItems));

	ListSelectionDialog dialog =
		new ListSelectionDialog(
			this.getShell(),
			this,
			contentsProvider,
			labelProvider,
			PROJECT_SELECTION_MESSAGE);

	if (dialog.open() != dialog.OK)
		return;

	Object[] result = dialog.getResult();

	int currentItemsLength = currentItems.length;
	int resultLength = result.length;
	String[] newItems = new String[currentItemsLength + resultLength];

	System.arraycopy(currentItems, 0, newItems, 0, currentItemsLength);
	System.arraycopy(
		result,
		0,
		newItems,
		currentItemsLength,
		result.length);
	this.buildList.setItems(newItems);
}
/**
 * Create the list of build paths. If the current build order is empty make the list empty
 * and disable it.
 * @param composite - the parent to create the list in
 * @param - enabled - the boolean that indcates if the list will be sensitive initially or not
 */
private void createBuildOrderList(Composite composite, boolean enabled) {

	this.buildLabel = new Label(composite, SWT.NONE);
	this.buildLabel.setText(LIST_LABEL);
	this.buildLabel.setEnabled(enabled);
	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.horizontalSpan = 2;
	this.buildLabel.setLayoutData(gridData);
	
	this.buildList = new List(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	this.buildList.setEnabled(enabled);
	GridData data = new GridData();
	//Set heightHint with a small value so the list size will be defined by 
	//the space available in the dialog instead of resizing the dialog to
	//fit all the items in the list.
	data.heightHint = buildList.getItemHeight();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.grabExcessVerticalSpace = true;
	this.buildList.setLayoutData(data);
}
/**
 * Create the widgets that are used to determine the build order.
 *
 * @param parent the parent composite
 * @return the new control
 */
protected Control createContents(Composite parent) {

	WorkbenchHelp.setHelp(parent, IHelpContextIds.BUILD_ORDER_PREFERENCE_PAGE);

	//The main composite
	Composite composite = new Composite(parent, SWT.NULL);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	composite.setLayout(layout);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	composite.setLayoutData(data);

	String[] buildOrder = getCurrentBuildOrder();
	boolean useDefault = (buildOrder.length < 1);
	
	createDefaultPathButton(composite, useDefault);
	// List always enabled so user can scroll list.
	// Only the button need to be disabled.
	createBuildOrderList(composite, true);
	createListButtons(composite, !useDefault);
	
	// a note about projects with unspecified build orders
	noteLabel = new Label(composite, SWT.NONE);
	noteLabel.setText(NOTE_LABEL);


	if (useDefault) {
		this.buildList.setItems(getDefaultProjectOrder());
		// if there are no marked items, do not show the note
		if(markedItemsStartIndex >= buildList.getItemCount())
			noteLabel.setVisible(false);
	} else {
		this.buildList.setItems(buildOrder);
		// when not using default build order, do not show the note
		noteLabel.setVisible(false);
	}
	
	return composite;

}
/**
 * Create the default path button. Set it to selected based on the current workspace
 * build path.
 * @param composite org.eclipse.swt.widgets.Composite
 * @param selected - the boolean that indicates the buttons initial state
 */
private void createDefaultPathButton(Composite composite, boolean selected) {

	defaultOrderInitiallySelected = selected;

	this.defaultOrderButton = new Button(composite, SWT.LEFT | SWT.CHECK);
	this.defaultOrderButton.setSelection(selected);
	this.defaultOrderButton.setText(DEFAULTS_LABEL);
	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			defaultsButtonSelected(defaultOrderButton.getSelection());
		}
	};
	this.defaultOrderButton.addSelectionListener(listener);

	GridData gridData = new GridData();
	gridData.horizontalAlignment = GridData.FILL;
	gridData.horizontalSpan = 2;
	this.defaultOrderButton.setLayoutData(gridData);
}
/**
 * Create the buttons used to manipulate the list. These Add, Remove and Move Up or Down
 * the list items.
 * @param composite the parent of the buttons
 * @param enableComposite - boolean that indicates if a composite should be enabled
 */
private void createListButtons(Composite composite, boolean enableComposite) {

	//Create an intermeditate composite to keep the buttons in the same column
	this.buttonComposite = new Composite(composite, SWT.RIGHT);
	GridLayout layout = new GridLayout();
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	this.buttonComposite.setLayout(layout);
	GridData gridData = new GridData();
	gridData.verticalAlignment = GridData.FILL;
	gridData.horizontalAlignment = GridData.FILL;
	this.buttonComposite.setLayoutData(gridData);

	Button upButton = new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
	upButton.setText(UP_LABEL);
	upButton.setEnabled(enableComposite);
	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			moveSelectionUp();
		}
	};
	upButton.addSelectionListener(listener);
	setButtonGridData(upButton);

	Button downButton = new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
	downButton.setText(DOWN_LABEL);
	downButton.setEnabled(enableComposite);
	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			moveSelectionDown();
		}
	};
	downButton.addSelectionListener(listener);
	setButtonGridData(downButton);

	Button addButton = new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
	addButton.setText(ADD_LABEL);
	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			addProject();
		}
	};
	addButton.addSelectionListener(listener);
	addButton.setEnabled(enableComposite);
	setButtonGridData(addButton);

	Button removeButton = new Button(this.buttonComposite, SWT.CENTER | SWT.PUSH);
	removeButton.setText(REMOVE_LABEL);
	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			removeSelection();
		}
	};
	removeButton.addSelectionListener(listener);
	removeButton.setEnabled(enableComposite);
	setButtonGridData(removeButton);

}
/**
 * The defaults button has been selected - update the other widgets as required.
 * @param selected - whether or not the defaults button got selected
 */
private void defaultsButtonSelected(boolean selected) {
	if (selected) {
		setBuildOrderWidgetsEnablement(false);
		buildList.setItems(getDefaultProjectOrder());
		// if there are marked items, make the note visible
		if(markedItemsStartIndex < buildList.getItemCount())
			noteLabel.setVisible(true);
	}
	else {
		setBuildOrderWidgetsEnablement(true);
		String[] buildOrder = getCurrentBuildOrder();
		if (buildOrder.length < 1) {
			// Get a copy of the default order and remove markers
			String[] names = getDefaultProjectOrder();
			String[] copy = new String[names.length];
			System.arraycopy(names, 0, copy, 0, names.length);
			for (int i = markedItemsStartIndex; i < copy.length; i++)
				copy[i] = names[i].substring(1);
			buildList.setItems(copy);
		} else {
			buildList.setItems(buildOrder);
		}
		// make the note invisible
		noteLabel.setVisible(false);
	}
}
/**
 * Get the project names for the current custom build
 * order stored in the workspace description.
 * 
 * @return java.lang.String[]
 */
private String[] getCurrentBuildOrder() {
	if (customBuildOrder == null) {
		customBuildOrder = getWorkspace().getDescription().getBuildOrder();
		if (customBuildOrder == null)
			customBuildOrder = new String[0];
	}
	
	return customBuildOrder;
}
/**
 * Get the project names in the default build order
 * based on the current Workspace settings.
 * 
 * @return java.lang.String[]
 */
private String[] getDefaultProjectOrder() {
	if (defaultBuildOrder == null) {
		IWorkspace workspace = getWorkspace();
		IProject[][] projectOrder =
			getWorkspace().computePrerequisiteOrder(workspace.getRoot().getProjects());

		IProject[] foundProjects = projectOrder[0];
		IProject[] ambiguousProjects = projectOrder[1];

		defaultBuildOrder =
			new String[foundProjects.length + ambiguousProjects.length];
		int foundSize = foundProjects.length;
		for (int i = 0; i < foundSize; i++)
			defaultBuildOrder[i] = foundProjects[i].getName();
		markedItemsStartIndex = foundSize;
		for (int i = 0; i < ambiguousProjects.length; i++)
			defaultBuildOrder[i + foundSize] = MARKER + ambiguousProjects[i].getName();
	}
	
	return defaultBuildOrder;
}
/**
 * Return the Workspace the build order is from.
 * @return org.eclipse.core.resources.IWorkspace
 */
private IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}
/**
 * Return whether or not searchElement is in testArray.
 */
private boolean includes(String[] testArray, String searchElement) {

	for (int i = 0; i < testArray.length; i++) {
		if (searchElement.equals(testArray[i]))
			return true;
	}
	return false;

}
/**
 * See IWorkbenchPreferencePage. This class does nothing with he Workbench.
 */
public void init(IWorkbench workbench) {
	this.workbench = workbench;
}
/**
 * Move the current selection in the build list down.
 */
private void moveSelectionDown() {

	//Only do this operation on a single selection
	if (this.buildList.getSelectionCount() == 1) {
		int currentIndex = this.buildList.getSelectionIndex();
		if (currentIndex < this.buildList.getItemCount() - 1) {
			String elementToMove = this.buildList.getItem(currentIndex);
			this.buildList.remove(currentIndex);
			this.buildList.add(elementToMove, currentIndex + 1);
			this.buildList.select(currentIndex + 1);
		}
	}
}
/**
 * Move the current selection in the build list up.
 */
private void moveSelectionUp() {

	int currentIndex = this.buildList.getSelectionIndex();

	//Only do this operation on a single selection
	if (currentIndex > 0 && this.buildList.getSelectionCount() == 1) {
		String elementToMove = this.buildList.getItem(currentIndex);
		this.buildList.remove(currentIndex);
		this.buildList.add(elementToMove, currentIndex - 1);
		this.buildList.select(currentIndex - 1);
	}
}
/**
 * Performs special processing when this page's Defaults button has been pressed.
 * In this case change the defaultOrderButton to have it's selection set to true.
 */
protected void performDefaults() {
	this.defaultOrderButton.setSelection(true);
	defaultsButtonSelected(true);
	super.performDefaults();
}
/** 
 * OK has been pressed. If the defualt button is pressed then reset the build order to false;
 * otherwise set it to the contents of the list.
 */
public boolean performOk() {

	String[] buildOrder = null;
	boolean useDefault = defaultOrderButton.getSelection();

	// if use defaults is turned off
	if (!useDefault)
		buildOrder = buildList.getItems();

	//Get a copy of the description from the workspace, set the build order and then
	//apply it to the workspace.
	IWorkspaceDescription description = getWorkspace().getDescription();
	description.setBuildOrder(buildOrder);
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException exception) {
		//failed - return false
		return false;
	}
	
	// Perform auto-build if use default is off (because
	// order could have changed) or if use default setting
	// was changed.
	if (!useDefault || (useDefault != defaultOrderInitiallySelected)) {
		defaultOrderInitiallySelected = useDefault;
		// If auto build is turned on, then do a global incremental
		// build on all the projects.
		if (ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			GlobalBuildAction action = new GlobalBuildAction(workbench, getShell(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
			action.doBuild();
		}	
	}

	// Clear the custom build order cache
	customBuildOrder = null;
	
	return true;
}
/**
 * Remove the current selection in the build list.
 */
private void removeSelection() {

	this.buildList.remove(this.buildList.getSelectionIndices());
}
/**
 * Set the widgets that select build order to be enabled or diabled.
 * @param value boolean
 */
private void setBuildOrderWidgetsEnablement(boolean value) {

	// Only change enablement of buttons. Leave list alone
	// because you can't scroll it when disabled.
	Control[] children = this.buttonComposite.getChildren();
	for (int i = 0; i < children.length; i++) {
		children[i].setEnabled(value);
	}
}
/**
 * Set the grid data of the supplied button to grab the whole column
 * @param button org.eclipse.swt.widgets.Button
 */
private void setButtonGridData(Button button) {

	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	button.setLayoutData(data);
}
/**
 * Return a sorted array of the names of the projects that are already in the currently 
 * displayed names.
 * @return String[]
 * @param allProjects - all of the projects in the workspace 
 * @param currentlyDisplayed - the names of the projects already being displayed
 */
private String[] sortedDifference(IProject[] allProjects, String[] currentlyDisplayed) {

	TreeSet difference = new TreeSet();

	for(int i = 0; i < allProjects.length; i ++){
		if(!includes(currentlyDisplayed,allProjects[i].getName()))
			difference.add(allProjects[i].getName());
	}

	String [] returnValue = new String[difference.size()];
	difference.toArray(returnValue);
	return returnValue;
}
}
