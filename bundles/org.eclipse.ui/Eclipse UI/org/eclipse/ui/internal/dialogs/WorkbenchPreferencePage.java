package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class WorkbenchPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage, Listener {
	private IWorkbench workbench;
	private Button showWelcomeButton;
	private Button autoBuildButton;
	private Button autoSaveAllButton;
	private Button linkButton;

	//Widgets for menu based perspective operation
	private Button openInNewWindowButton;
	private Button openInNewPageButton;
	private Button replaceButton;
	private Button switchOnNewProjectButton;
	private Text openInNewWindowText;
	private Text openInNewPageText;
	private Text replaceText;

	//Widgets for perspective switching when creating new projects
	private Button openProjectInNewWindowButton;
	private Button openProjectInNewPageButton;
	private Button replaceProjectButton;

	private String currentPerspectiveSetting;
	private String newProjectPerspectiveSetting;

	//Labels
	private static final String NEW_PERSPECTIVE_TITLE = "Open a new perspective";
	private static final String NEW_PROJECT_PERSPECTIVE_TITLE =
		"Open a project's default perspective";
	private static final String OPEN_NEW_WINDOW_LABEL = "In a new window";
	private static final String OPEN_NEW_PAGE_LABEL = "In the same window";
	private static final String OPEN_REPLACE_LABEL = "Replace current";
	private static final String DO_NOT_SWITCH_PERSPECTIVES = "Do not switch";
	private static final String SHIFT_LABEL = "Shift";
	private static final String ALT_LABEL = "Alt";
/**
 * Get the values for the alt perspective setting. It will be replace unless replace is selected.
 */
private String altPerspectiveSetting() {

	if (this.currentPerspectiveSetting
		== IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE)
		return IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
	else
		return IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
}
/**
 * Creates composite control and sets the default layout data.
 *
 * @param parent  the parent of the new composite
 * @param numColumns  the number of columns for the new composite
 * @return the newly-created coposite
 */
private Composite createComposite(Composite parent, int numColumns) {
	Composite composite = new Composite(parent, SWT.NULL);

	// GridLayout
	GridLayout layout = new GridLayout();
	layout.numColumns = numColumns;
	composite.setLayout(layout);

	// GridData
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	composite.setLayoutData(data);
	return composite;
}
/**
 *	Create this page's visual contents
 *
 *	@return org.eclipse.swt.widgets.Control
 *	@param parent org.eclipse.swt.widgets.Composite
 */
protected Control createContents(Composite parent) {
	Composite composite = new Composite(parent, SWT.NULL);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

	showWelcomeButton = new Button(composite, SWT.CHECK);
	showWelcomeButton.setText("Show Welcome dialog at startup");

	autoBuildButton = new Button(composite, SWT.CHECK);
	autoBuildButton.setText("Perform build automatically on resource modification");

	autoSaveAllButton = new Button(composite, SWT.CHECK);
	autoSaveAllButton.setText(
		"Save all modified resources automatically prior to manual build");

	linkButton = new Button(composite, SWT.CHECK);
	linkButton.setText("Link Navigator selection to active editor");

	createSpace(composite);

	createPerspectiveGroup(composite);

	createSpace(composite);

	createProjectPerspectiveGroup(composite);

	// set initial values
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
	autoSaveAllButton.setSelection(
		store.getBoolean(IWorkbenchPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
	showWelcomeButton.setSelection(
		store.getBoolean(IWorkbenchPreferenceConstants.WELCOME_DIALOG));
	linkButton.setSelection(
		store.getBoolean(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));

	return composite;
}
/**
 * Create a composite that contains buttons for selecting the preference opening selections. 
 */
private void createPerspectiveGroup(Composite composite) {

	Label titleLabel = new Label(composite, SWT.NONE);
	titleLabel.setText(NEW_PERSPECTIVE_TITLE);

	Composite buttonComposite = new Composite(composite, SWT.LEFT);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	buttonComposite.setLayout(layout);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	composite.setData(data);

	//Open New Window button
	this.openInNewWindowButton =
		createRadioButton(buttonComposite, OPEN_NEW_WINDOW_LABEL);
	this.openInNewWindowButton.setSelection(
		this.currentPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));

	this.openInNewWindowButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			currentPerspectiveSetting =
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
			setTextValuesForPerspective();
		}
	});

	this.openInNewWindowText = new Text(buttonComposite, SWT.NONE);
	this.openInNewWindowText.setEditable(false);

	//Open New Page button
	this.openInNewPageButton =
		createRadioButton(buttonComposite, OPEN_NEW_PAGE_LABEL);
	this.openInNewPageButton.setSelection(
		this.currentPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));

	this.openInNewPageButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			currentPerspectiveSetting = IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
			setTextValuesForPerspective();
		}
	});

	this.openInNewPageText = new Text(buttonComposite, SWT.NONE);
	this.openInNewPageText.setEditable(false);

	//Replace button
	this.replaceButton = createRadioButton(buttonComposite, OPEN_REPLACE_LABEL);
	this.replaceButton.setSelection(
		this.currentPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));

	this.replaceButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			currentPerspectiveSetting =
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
			setTextValuesForPerspective();
		}
	});

	this.replaceText = new Text(buttonComposite, SWT.NONE);
	this.replaceText.setEditable(false);

	setTextValuesForPerspective();
}
/**
 * Create a composite that contains buttons for selecting the 
 * preference opening new project selections. 
 */
private void createProjectPerspectiveGroup(Composite composite) {

	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	String currentPersspetive =
		store.getString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);
	Label titleLabel = new Label(composite, SWT.NONE);
	titleLabel.setText(NEW_PROJECT_PERSPECTIVE_TITLE);

	Composite buttonComposite = new Composite(composite, SWT.LEFT);
	GridLayout layout = new GridLayout();
	buttonComposite.setLayout(layout);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	composite.setData(data);

	//Open New Window button
	this.openProjectInNewWindowButton =
		createRadioButton(buttonComposite, OPEN_NEW_WINDOW_LABEL);
	this.openProjectInNewWindowButton.setSelection(
		this.newProjectPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));

	this.openProjectInNewWindowButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			newProjectPerspectiveSetting =
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
		}
	});

	//Open New Page button
	this.openProjectInNewPageButton =
		createRadioButton(buttonComposite, OPEN_NEW_PAGE_LABEL);
	this.openProjectInNewPageButton.setSelection(
		this.newProjectPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));

	this.openProjectInNewPageButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			newProjectPerspectiveSetting =
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
		}
	});

	//Replace button
	this.replaceProjectButton =
		createRadioButton(buttonComposite, OPEN_REPLACE_LABEL);
	this.replaceProjectButton.setSelection(
		this.newProjectPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));

	this.replaceProjectButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			newProjectPerspectiveSetting =
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE;
		}
	});

	//Replace button
	this.switchOnNewProjectButton =
		createRadioButton(buttonComposite, DO_NOT_SWITCH_PERSPECTIVES);
	this.switchOnNewProjectButton.setSelection(
		this.newProjectPerspectiveSetting.equals(
			IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE));

	this.switchOnNewProjectButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			newProjectPerspectiveSetting = IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE;
		}
	});

}
/**
 * Utility method that creates a radio button instance
 * and sets the default layout data.
 *
 * @param parent  the parent for the new button
 * @param label  the label for the new button
 * @return the newly-created button
 */
private Button createRadioButton(Composite parent, String label) {
	Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
	button.setText(label);
	button.addListener(SWT.Selection, this);
	GridData data = new GridData();
	button.setLayoutData(data);
	return button;
}
/**
 * Creates a tab of one horizontal spans.
 *
 * @param parent  the parent in which the tab should be created
 */
private void createSpace(Composite parent) {
	Label vfiller = new Label(parent, SWT.LEFT);
	GridData gridData = new GridData();
	gridData = new GridData();
	gridData.horizontalAlignment = GridData.BEGINNING;
	gridData.grabExcessHorizontalSpace = false;
	gridData.verticalAlignment = GridData.CENTER;
	gridData.grabExcessVerticalSpace = false;
	vfiller.setLayoutData(gridData);
}
/**
 * Returns preference store that belongs to the our plugin.
 *
 * @return the preference store for this plugin
 */
protected IPreferenceStore doGetPreferenceStore() {
	return WorkbenchPlugin.getDefault().getPreferenceStore();
}
/**
 * Handles events generated by controls on this page.
 *
 * @param e  the event to handle
 */
public void handleEvent(Event e) {
	// get widget that generates the event
	Widget source = e.widget;
	
	// add the code that should react to
	// some widget event
}
/**
 *	@see IWorkbenchPreferencePage
 */
public void init(IWorkbench aWorkbench) {
	this.workbench = aWorkbench;
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	this.currentPerspectiveSetting =
		store.getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
	this.newProjectPerspectiveSetting =
		store.getString(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);
}
/**
 * The default button has been pressed. 
 */
protected void performDefaults() {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	autoBuildButton.setSelection(ResourcesPlugin.getWorkspace().isAutoBuilding());
	autoSaveAllButton.setSelection(
		store.getDefaultBoolean(IWorkbenchPreferenceConstants.SAVE_ALL_BEFORE_BUILD));
	showWelcomeButton.setSelection(
		store.getDefaultBoolean(IWorkbenchPreferenceConstants.WELCOME_DIALOG));
	linkButton.setSelection(
		store.getDefaultBoolean(
			IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR));

	//Perspective preferences
	String defaultPreference =
		store.getDefaultString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
	this.currentPerspectiveSetting = defaultPreference;
	openInNewWindowButton.setSelection(
		defaultPreference.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));
	openInNewPageButton.setSelection(
		defaultPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));
	replaceButton.setSelection(
		defaultPreference.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));

	//Project perspective preferences
	String projectPreference =
		store.getDefaultString(
			IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE);
	this.newProjectPerspectiveSetting = projectPreference;
	openProjectInNewWindowButton.setSelection(
		projectPreference.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW));
	openProjectInNewPageButton.setSelection(
		projectPreference.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE));
	replaceProjectButton.setSelection(
		projectPreference.equals(
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE));
	switchOnNewProjectButton.setSelection(
		projectPreference.equals(IWorkbenchPreferenceConstants.NO_NEW_PERSPECTIVE));

	setTextValuesForPerspective();
	super.performDefaults();
}
/**
 *	The user has pressed Ok.  Store/apply this page's values appropriately.
 */
public boolean performOk() {
	IPreferenceStore store = getPreferenceStore();

	// inform the workbench of whether it should do autobuilds or not
	boolean newAutoBuildSetting = autoBuildButton.getSelection();
	IWorkspaceDescription description =
		ResourcesPlugin.getWorkspace().getDescription();
	boolean oldAutoBuildSetting = description.isAutoBuilding();
	description.setAutoBuilding(newAutoBuildSetting);
	try {
		ResourcesPlugin.getWorkspace().setDescription(description);
	} catch (org.eclipse.core.runtime.CoreException e) {
		// handle the exception here (could not save the new description to disk)
	}
	if (oldAutoBuildSetting != newAutoBuildSetting) {
		// fire off a property change notification so interested
		// parties can know about the auto build setting change
		// since it is not kept in the preference store.
		store.firePropertyChangeEvent(
			IWorkbenchPreferenceConstants.AUTO_BUILD,
			new Boolean(oldAutoBuildSetting),
			new Boolean(newAutoBuildSetting));

		// If auto build is turned on, then do a global incremental
		// build on all the projects.
		if (newAutoBuildSetting) {
			GlobalBuildAction action =
				new GlobalBuildAction(this.workbench, IncrementalProjectBuilder.AUTO_BUILD);
			action.doBuild();
		}
	}

	// store the save all prior to build setting
	store.setValue(
		IWorkbenchPreferenceConstants.SAVE_ALL_BEFORE_BUILD,
		autoSaveAllButton.getSelection());

	// store the Show welcome dialog setting
	store.setValue(
		IWorkbenchPreferenceConstants.WELCOME_DIALOG,
		showWelcomeButton.getSelection());

	// store the link navigator to editor setting
	store.setValue(
		IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR,
		linkButton.getSelection());

	// store the open in new window settings
	store.setValue(
		IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE,
		currentPerspectiveSetting);

	// store the open in new window shift settings
	store.setValue(
		IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE,
		shiftPerspectiveSetting());

	// store the open in new window alt settings
	store.setValue(
		IWorkbenchPreferenceConstants.ALT_OPEN_NEW_PERSPECTIVE,
		altPerspectiveSetting());

	// store the open in new project settings
	store.setValue(
		IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE,
		newProjectPerspectiveSetting);

	return true;
}
/**
 * Set the values for the text based on the current setting.
 */
private void setTextValuesForPerspective() {

	if (this.currentPerspectiveSetting
		== IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE) {
		this.openInNewWindowText.setText(SHIFT_LABEL);
		this.replaceText.setText(ALT_LABEL);
		this.openInNewPageText.setText("");
	} else {

		if (this.currentPerspectiveSetting
			== IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW) {
			this.openInNewPageText.setText(SHIFT_LABEL);
			this.replaceText.setText(ALT_LABEL);
			this.openInNewWindowText.setText("");
		} else {
			if (this.currentPerspectiveSetting
				== IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE) {
				this.openInNewWindowText.setText(SHIFT_LABEL);
				this.openInNewPageText.setText(ALT_LABEL);
				this.replaceText.setText("");
			}
		}

	}

}
/**
 * Get the values for the shift perspective setting. It will be window unless window is selected.
 */
private String shiftPerspectiveSetting() {

	if (this.currentPerspectiveSetting
		== IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW)
		return IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE;
	else
		return IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW;
}
}
