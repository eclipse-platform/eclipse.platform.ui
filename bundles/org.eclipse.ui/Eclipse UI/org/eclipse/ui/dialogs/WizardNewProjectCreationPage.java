package org.eclipse.ui.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.help.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Standard main page for a wizard that is creates a project resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class WizardNewProjectCreationPage extends WizardPage {

	private boolean useDefaults = true;

	// initial value stores
	private String initialProjectFieldValue;
	private IPath initialLocationFieldValue;
	
	// widgets
	private Text projectNameField;
	private Text locationPathField;
	private Button browseButton;

	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setLocationForSelection();
			setPageComplete(validatePage());
		}
	};

	private Listener locationModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private static final int SIZING_INDENTATION_WIDTH = 10;
/**
 * Creates a new project creation wizard page.
 *
 * @param pageName the name of this page
 */
public WizardNewProjectCreationPage(String pageName) {
	super(pageName);
	setPageComplete(false);
	this.initialLocationFieldValue = Platform.getLocation();
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	Composite composite = new Composite(parent, SWT.NULL);

	WorkbenchHelp.setHelp(composite, new DialogPageContextComputer(this, IHelpContextIds.NEW_PROJECT_WIZARD_PAGE));
	
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	
	createProjectNameGroup(composite);
	createProjectLocationGroup(composite);
	projectNameField.setFocus();
	
	setControl(composite);
}
/**
 * Creates the project location specification controls.
 *
 * @param parent the parent composite
 */
private final void createProjectLocationGroup(Composite parent) {

	// project specification group
	Composite projectGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	projectGroup.setLayout(layout);
	projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	final Button useDefaultsButton = new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
	useDefaultsButton.setText("Use default location");
	useDefaultsButton.setSelection(this.useDefaults);

	GridData buttonData = new GridData();
	buttonData.horizontalSpan = 3;
	useDefaultsButton.setLayoutData(buttonData);

	createUserSpecifiedProjectLocationGroup(projectGroup,!this.useDefaults);

	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			useDefaults = useDefaultsButton.getSelection();
			browseButton.setEnabled(!useDefaults);
			locationPathField.setEnabled(!useDefaults);
			setLocationForSelection();
		}
	};
	useDefaultsButton.addSelectionListener(listener);
}
/**
 * Creates the project name specification controls.
 *
 * @param parent the parent composite
 */
private final void createProjectNameGroup(Composite parent) {
	// project specification group
	Composite projectGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	projectGroup.setLayout(layout);
	projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// new project label
	Label projectLabel = new Label(projectGroup,SWT.NONE);
	projectLabel.setText("Project name:");

	// new project name entry field
	projectNameField = new Text(projectGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	projectNameField.setLayoutData(data);

	// Set the initial value first before listener
	// to avoid handling an event during the creation.
	if (initialProjectFieldValue != null)
		projectNameField.setText(initialProjectFieldValue);
	projectNameField.addListener(SWT.Modify, nameModifyListener);
}
/**
 * Creates the project location specification controls.
 *
 * @param projectGroup the parent composite
 * @param boolean - the initial enabled state of the widgets created
 */
private void createUserSpecifiedProjectLocationGroup(Composite projectGroup, boolean enabled) {

	// location label
	Label locationLabel = new Label(projectGroup,SWT.NONE);
	locationLabel.setText("Location:");

	// project location entry field
	locationPathField = new Text(projectGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	locationPathField.setLayoutData(data);
	locationPathField.setEnabled(enabled);

	// browse button
	browseButton = new Button(projectGroup, SWT.PUSH);
	browseButton.setText("Browse...");
	browseButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleLocationBrowseButtonPressed();
		}
	});

	browseButton.setEnabled(enabled);

	// Set the initial value first before listener
	// to avoid handling an event during the creation.
	if (initialLocationFieldValue != null)
		locationPathField.setText(initialLocationFieldValue.toOSString());
	locationPathField.addListener(SWT.Modify, locationModifyListener);
}
/**
 * Returns the current project location path as entered by 
 * the user, or its anticipated initial value.
 *
 * @return the project location path, its anticipated initial value, or <code>null</code>
 *   if no project location path is known
 */
public IPath getLocationPath() {
	if (useDefaults)
		return initialLocationFieldValue;
		
	return new Path(locationPathField.getText());
}
/**
 * Creates a project resource handle for the current project name field value.
 * <p>
 * This method does not create the project resource; this is the responsibility
 * of <code>IProject::create</code> invoked by the new project resource wizard.
 * </p>
 *
 * @return the new project resource handle
 */
public IProject getProjectHandle() {
	return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
}
/**
 * Returns the current project name as entered by the user, or its anticipated
 * initial value.
 *
 * @return the project name, its anticipated initial value, or <code>null</code>
 *   if no project name is known
 */
public String getProjectName() {
	if (projectNameField == null)
		return initialProjectFieldValue;
		
	return projectNameField.getText();
}
/**
 *	Open an appropriate directory browser
 */
private void handleLocationBrowseButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
	dialog.setMessage("Select the location directory.");
	dialog.setFilterPath(locationPathField.getText());
	
	String selectedDirectory = dialog.open();
	if (selectedDirectory != null)
		locationPathField.setText(selectedDirectory);
}
/**
 * Set the location to the default location if we are set to useDefaults.
 */
private void setLocationForSelection() {
	if (useDefaults) {
		IPath defaultPath = Platform.getLocation().append(projectNameField.getText());
		locationPathField.setText(defaultPath.toString());
	}
}
/**
 * Returns whether this page's controls currently all contain valid 
 * values.
 *
 * @return <code>true</code> if all controls are valid, and
 *   <code>false</code> if at least one is invalid
 */
private boolean validatePage() {
	IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();

	String projectFieldContents = projectNameField.getText();
	IStatus nameStatus =
		workspace.validateName(projectFieldContents, IResource.PROJECT);
	if (!nameStatus.isOK()) {
		setErrorMessage(nameStatus.getMessage());
		return false;
	}

	String locationFieldContents = locationPathField.getText();
	
	if (!locationFieldContents.equals("")) {
		IPath path = new Path("");
		if (!path.isValidPath(locationFieldContents)) {
			setErrorMessage("Invalid location path");
			return false;
		}
	}


	if (getProjectHandle().exists()) {
		setErrorMessage("Project already exists.");
		return false;
	}

	setErrorMessage(null);
	return true;
}
}
