package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.TreeSet;

/**
 * The BuildOrderPage is the page that is used to determine what order projects
 * will be built in by the workspace.
 */
public class BuildOrderPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
		
	private Button defaultOrderButton;
	private List buildList;
	private Composite buttonComposite;

	private static String UP_LABEL = WorkbenchMessages.getString("BuildOrderPreference.up"); //$NON-NLS-1$
	private static String DOWN_LABEL = WorkbenchMessages.getString("BuildOrderPreference.down"); //$NON-NLS-1$
	private static String ADD_LABEL = WorkbenchMessages.getString("BuildOrderPreference.add"); //$NON-NLS-1$
	private static String REMOVE_LABEL = WorkbenchMessages.getString("BuildOrderPreference.remove"); //$NON-NLS-1$
	private static String UNSELECTED_PROJECTS = WorkbenchMessages.getString("BuildOrderPreference.selectProject"); //$NON-NLS-1$
	private static String PROJECT_SELECTION_MESSAGE = WorkbenchMessages.getString("BuildOrderPreference.selectOtherProjects"); //$NON-NLS-1$
	private static String DEFAULTS_LABEL = WorkbenchMessages.getString("BuildOrderPreference.useDefaults"); //$NON-NLS-1$
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

	String[] buildOrder = getCurrentBuildOrder();
	
	if (buildOrder == null)
		this.buildList.setEnabled(false);
	else
		this.buildList.setItems(buildOrder);

}
/**
 * Create the widgets that are used to determine the build order.
 *
 * @param parent the parent composite
 * @return the new control
 */
protected Control createContents(Composite parent) {

	WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IHelpContextIds.BUILD_ORDER_PREFERENCE_PAGE));

	//The main composite
	Composite composite = new Composite(parent, SWT.NULL);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	composite.setLayoutData(data);

	boolean useDefaults = (getCurrentBuildOrder() == null);
	createDefaultPathButton(composite,useDefaults);
	
	createBuildOrderList(composite,!useDefaults);
	createListButtons(composite,!useDefaults);


	return composite;

}
/**
 * Create the default path button. Set it to selected based on the current workspace
 * build path.
 * @param composite org.eclipse.swt.widgets.Composite
 * @param selected - the boolean that indicates the buttons initial state
 */
private void createDefaultPathButton(Composite composite, boolean selected) {

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
	if (selected)
		setBuildOrderWidgetsEnablement(false);
	else {
		setBuildOrderWidgetsEnablement(true);
		//if there are no items then add the defaults
		if (buildList.getItemCount() == 0)
			buildList.setItems(getDefaultProjectOrder());
	}
}
/**
 * Get the current build order of the workspace.
 * @return java.lang.String[]
 */
private String[] getCurrentBuildOrder() {
	String [] buildOrder =  getWorkspace().getDescription().getBuildOrder();
	if(buildOrder == null)
		return null;
	//Workaround for 1GBXLX4: ITPCORE:WINNT - getBuildOrder is never null
	if(buildOrder.length == 0)
		return null;
	else return buildOrder;
}
/**
 * Get the names of the projects in thier default build order based on the current Workspace
 * settings.
 * @return java.lang.String[]
 */
private String[] getDefaultProjectOrder() {

	IWorkspace workspace = getWorkspace();
	IProject[][] projectOrder =
		getWorkspace().computePrerequisiteOrder(workspace.getRoot().getProjects());

	IProject[] foundProjects = projectOrder[0];
	IProject[] ambiguousProjects = projectOrder[1];

	String [] projectOrderStrings =
		new String[foundProjects.length + ambiguousProjects.length];

	int foundSize = foundProjects.length;

	for (int i = 0; i < foundSize; i++) {
		projectOrderStrings[i] = foundProjects[i].getName();
	}

	for (int i = 0; i < ambiguousProjects.length; i++) {
		projectOrderStrings[i + foundSize] = ambiguousProjects[i].getName();
	}

	return projectOrderStrings;
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
public void init(IWorkbench workbench) {}
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

	String[] buildPath = null;

	if (!this.defaultOrderButton.getSelection())
		buildPath = buildList.getItems();

	//Get a copy of the description from the workspace, set the build order and then
	//apply it to the workspace.
	IWorkspaceDescription description = getWorkspace().getDescription();
	description.setBuildOrder(buildPath);
	try {
		getWorkspace().setDescription(description);
	} catch (CoreException exception) {
		//failed - return false
		return false;
	}

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

	this.buildList.setEnabled(value);
	Control[] children = this.buttonComposite.getChildren();
	for (int i = 0; i < children.length; i++) {
		children[i].setEnabled(value);
	}

}
/**
 * Set the grid data pf the supplied button to grab the whole column
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
