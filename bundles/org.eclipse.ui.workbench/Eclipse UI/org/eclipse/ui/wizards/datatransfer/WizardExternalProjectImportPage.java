package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Standard main page for a wizard that creates a project resource from
 * whose location already contains a project.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardExternalProjectImportPage("basicNewProjectPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Create a new project resource.");
 * </pre>
 * </p>
 */
public class WizardExternalProjectImportPage extends WizardPage {

	private FileFilter projectFilter = new FileFilter() {
			//Only accept those files that are .project
	public boolean accept(File pathName) {
			return pathName.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME);
		}
	};

	//Keep track of the directory that we browsed to last time
	//the wizard was invoked.
	private static String previouslyBrowsedDirectory = "";

	// widgets
	private Text projectNameField;
	private Text locationPathField;
	private Label locationLabel;
	private Button browseButton;
	private IProjectDescription description;

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
	public WizardExternalProjectImportPage() {
		super("wizardExternalProjectPage"); //$NON-NLS-1$
		setPageComplete(false);
		setTitle(
			DataTransferMessages.getString("WizardExternalProjectImportPage.title")); //$NON-NLS-1$
		setDescription(
			DataTransferMessages.getString("WizardExternalProjectImportPage.description")); //$NON-NLS-1$

	}
	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectNameGroup(composite);
		createProjectLocationGroup(composite);
		validatePage();
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private Listener nameModifyListener = new Listener() {
		public void handleEvent(Event e) {
			setPageComplete(validatePage());
		}
	};

	/**
	 * Creates the project location specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {

		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectContentsLabel = new Label(projectGroup, SWT.NONE);
		projectContentsLabel.setText(
			DataTransferMessages.getString(
				"WizardExternalProjectImportPage.projectContentsLabel")); //$NON-NLS-1$

		createUserSpecifiedProjectLocationGroup(projectGroup);
	}
	/**
	 * Creates the project name specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectNameGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText(DataTransferMessages.getString("WizardExternalProjectImportPage.nameLabel")); //$NON-NLS-1$

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);

		projectNameField.addListener(SWT.Modify, nameModifyListener);
	}
	/**
	 * Creates the project location specification controls.
	 *
	 * @param projectGroup the parent composite
	 * @param boolean - the initial enabled state of the widgets created
	 */
	private void createUserSpecifiedProjectLocationGroup(Composite projectGroup) {

		// project location entry field
		this.locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		this.locationPathField.setLayoutData(data);

		// browse button
		this.browseButton = new Button(projectGroup, SWT.PUSH);
		this.browseButton.setText(
			DataTransferMessages.getString("WizardExternalProjectImportPage.browseLabel")); //$NON-NLS-1$
		this.browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

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

		return new Path(getProjectLocationFieldValue());
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
		return getProjectNameFieldValue();
	}
	/**
	 * Returns the value of the project name field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (projectNameField == null)
			return ""; //$NON-NLS-1$
		else
			return projectNameField.getText().trim();
	}
	/**
	 * Returns the value of the project location field
	 * with leading and trailing spaces removed.
	 * 
	 * @return the project location directory in the field
	 */
	private String getProjectLocationFieldValue() {
		return locationPathField.getText().trim();
	}
	/**
	 *	Open an appropriate directory browser
	 */
	private void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
		dialog.setMessage(
			DataTransferMessages.getString(
				"WizardExternalProjectImportPage.directoryLabel")); //$NON-NLS-1$

		String dirName = getProjectLocationFieldValue();
		if (dirName.equals(""))
			dirName = previouslyBrowsedDirectory;

		if (!dirName.equals("")) { //$NON-NLS-1$
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(new Path(dirName).toOSString());
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			previouslyBrowsedDirectory = selectedDirectory;
			locationPathField.setText(previouslyBrowsedDirectory);
			setProjectName(projectFile(previouslyBrowsedDirectory));
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
		IWorkspace workspace = getWorkspace();

		//If it is empty try to give something meaningful
		if (getProjectNameFieldValue().equals("")) //$NON-NLS-1$
			setProjectName(projectFile(locationPathField.getText()));

		String projectFieldContents = getProjectNameFieldValue();
				
		//If it is still empty show the error
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(
				DataTransferMessages.getString(
					"WizardExternalProjectImportPage.projectNameEmpty")); //$NON-NLS-1$
			return false;
		}

		IStatus nameStatus =
			workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		String locationFieldContents = getProjectLocationFieldValue();

		if (locationFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(
				DataTransferMessages.getString(
					"WizardExternalProjectImportPage.projectLocationEmpty")); //$NON-NLS-1$
			return false;
		}

		IPath path = new Path(""); //$NON-NLS-1$
		if (!path.isValidPath(locationFieldContents)) {
			setErrorMessage(
				DataTransferMessages.getString(
					"WizardExternalProjectImportPage.locationError")); //$NON-NLS-1$
			return false;
		}
		if (isPrefixOfRoot(getLocationPath())) {			
			
			//If the name does not match the last segment stop it
			if(!checkDefaultProjectValue(locationFieldContents)){
				setErrorMessage(
					DataTransferMessages.getString(
						"WizardExternalProjectImportPage.defaultLocationError")); //$NON-NLS-1$
				return false;
			}
		}
		else // Outside of the prefix so this is enabled
			locationPathField.setEditable(true);

		if (getProjectHandle().exists()) {
			setErrorMessage(
				DataTransferMessages.getString(
					"WizardExternalProjectImportPage.projectExistsMessage")); //$NON-NLS-1$
			return false;
		}

		if (projectFile(locationFieldContents) == null) {
			setErrorMessage(
				DataTransferMessages.format(
					"WizardExternalProjectImportPage.notAProject", //$NON-NLS-1$
					new String[] { locationFieldContents }));
			return false;
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	private IWorkspace getWorkspace() {
		IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();
		return workspace;
	}

	/**
	 * Check that the name of the project equals the last segment
	 * of the location path - i.e. it is the default value.
	 * If it is disable the name field and return true.
	 * If not return false
	 */
	private boolean checkDefaultProjectValue(String locationFieldContents){
		IPath locationPath = new Path(locationFieldContents);
		if(locationPath.lastSegment().equals(getProjectNameFieldValue())){
			projectNameField.setEditable(false);
			return true;
		}
		projectNameField.setEditable(true);
		return false;
	}

	/**
	 * Return whether or not the specifed location is a prefix
	 * of the root.
	 */
	private boolean isPrefixOfRoot(IPath locationPath) {
		return Platform.getLocation().isPrefixOf(locationPath);
	}

	/**
	 * Set the project name using either the name of the
	 * parent of the file or the name entry in the xml for 
	 * the file
	 */
	private void setProjectName(File projectFile) {

		//If there is no file or the user has already specified forget it
		if (projectFile == null || projectNameField.getText().length() > 0)
			return;

		IPath path = new Path(projectFile.getPath());

		IProjectDescription newDescription = null;

		try {
			newDescription = getWorkspace().loadProjectDescription(path);
		} catch (CoreException exception) {
			//no good couldn't get the name
		}

		if (newDescription == null) {
			this.description = null;
			this.projectNameField.setText("");
		}
		else{			
			this.description = newDescription;
			this.projectNameField.setText(this.description.getName());
		}
	}

	/**
	 * Return a.project file from the specified location.
	 * If there isn't one return null.
	 */
	private File projectFile(String locationFieldContents) {
		File directory = new File(locationFieldContents);
		if (directory.isFile())
			return null;

		File[] files = directory.listFiles(this.projectFilter);
		if (files != null && files.length == 1)
			return files[0];
		else
			return null;
	}

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on
	 * the wizard; the enablement of the Finish button implies that all controls
	 * on the pages currently contain valid values.
	 * </p>
	 *
	 * @return the created project resource, or <code>null</code> if the project
	 *    was not created
	 */
	IProject createExistingProject() {

		String projectName = projectNameField.getText();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		if(this.description == null){
			this.description =	workspace.newProjectDescription(projectName);
			IPath locationPath = getLocationPath();
			//If it is under the root use the default location
			if (isPrefixOfRoot(locationPath))
				this.description.setLocation(null);
			else
				this.description.setLocation(locationPath);
		}
		else
			this.description.setName(projectName);

		// create the new project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("", 2000); //$NON-NLS-1$
				project.create(description, new SubProgressMonitor(monitor, 1000));
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				project.open(new SubProgressMonitor(monitor, 1000));

			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception	
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				if (((CoreException) t).getStatus().getCode()
					== IResourceStatus.CASE_VARIANT_EXISTS) {
					MessageDialog.openError(getShell(), DataTransferMessages.getString("WizardExternalProjectImportPage.errorMessage"), //$NON-NLS-1$
					DataTransferMessages.getString("WizardExternalProjectImportPage.caseVariantExistsError") //$NON-NLS-1$,
					);
				} else {
					ErrorDialog.openError(getShell(), DataTransferMessages.getString("WizardExternalProjectImportPage.errorMessage"), //$NON-NLS-1$
					null, ((CoreException) t).getStatus());
				}
			}
			return null;
		}

		return project;
	}
	
	/*
 	 * see @DialogPage.setVisible(boolean)
 	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible)
			this.locationPathField.setFocus();
	}

}