package org.eclipse.ui.wizards.datatransfer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

/**
 *	Page 1 of the base resource export-to-file-system Wizard
 */
/*package*/ class WizardFileSystemResourceExportPage1 extends WizardExportResourcesPage implements Listener {

	// widgets
	private Combo				destinationNameField;
	private Button				destinationBrowseButton;
	private Button				overwriteExistingFilesCheckbox;
	private Button				createDirectoryStructureCheckbox;

	// constants
	private static final int	SIZING_TEXT_FIELD_WIDTH = 250;
	private static final int	COMBO_HISTORY_LENGTH = 5;

	// dialog store id constants
	private static final String	STORE_DESTINATION_NAMES_ID = "WizardFileSystemExportPage1.STORE_DESTINATION_NAMES_ID";
	private static final String	STORE_OVERWRITE_EXISTING_FILES_ID = "WizardFileSystemExportPage1.STORE_OVERWRITE_EXISTING_FILES_ID";
	private static final String	STORE_CREATE_STRUCTURE_ID = "WizardFileSystemExportPage1.STORE_CREATE_STRUCTURE_ID";

	//messages
	private static final String DESTINATION_EMPTY_MESSAGE = "Destination must not be empty.";
	private static final String SELECT_DESTINATION_MESSAGE ="Select the destination directory.";
/**
 *	Create an instance of this class
 */
protected WizardFileSystemResourceExportPage1(String name, IStructuredSelection selection) {
	super(name, selection);
}
/**
 *	Create an instance of this class
 */
public WizardFileSystemResourceExportPage1(IStructuredSelection selection) {
	this("fileSystemExportPage1", selection);
	setTitle("File system");
	setDescription("Export resources to the local file system.");
}
/**
 *	Add the passed value to self's destination widget's history
 *
 *	@param value java.lang.String
 */
protected void addDestinationItem(String value) {
	destinationNameField.add(value);
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	super.createControl(parent);
	giveFocusToDestination();
}
/**
 *	Create the export destination specification widgets
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 */
protected void createDestinationGroup(Composite parent) {
	// destination specification group
	Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	destinationSelectionGroup.setLayout(layout);
	destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

	new Label(destinationSelectionGroup, SWT.NONE).setText(getDestinationLabel());

	// destination name entry field
	destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE | SWT.BORDER);
	destinationNameField.addListener(SWT.Modify, this);
	destinationNameField.addListener(SWT.Selection, this);
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	destinationNameField.setLayoutData(data);

	// destination browse button
	destinationBrowseButton = new Button(destinationSelectionGroup, SWT.PUSH);
	destinationBrowseButton.setText("Browse...");
	destinationBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
	destinationBrowseButton.addListener(SWT.Selection, this);

	new Label(parent, SWT.NONE);	// vertical spacer
}
/**
 *	Create the export options specification widgets.
 *
 *	@param parent org.eclipse.swt.widgets.Composite
 */
protected void createOptionsGroup(Composite parent) {
	// options group
	Composite optionsGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	optionsGroup.setLayout(layout);
	optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	// overwrite... checkbox
	overwriteExistingFilesCheckbox = new Button(optionsGroup,SWT.CHECK|SWT.LEFT);
	overwriteExistingFilesCheckbox.setText("Overwrite existing files without warning");

	// create directory structure checkbox
	createDirectoryStructureCheckbox = new Button(optionsGroup,SWT.CHECK|SWT.LEFT);
	createDirectoryStructureCheckbox.setText("Create directory structure for files");
	createDirectoryStructureCheckbox.addListener(SWT.Selection,this);

		// initial setup
	createDirectoryStructureCheckbox.setSelection(true);
}
/**
 * Attempts to ensure that the specified directory exists on the local file system.
 * Answers a boolean indicating success.
 *
 * @return boolean
 * @param directory java.io.File
 */
protected boolean ensureDirectoryExists(File directory) {
	if (!directory.exists()) {
		if (!queryYesNoQuestion("Target directory does not exist.  Would you like to create it?"))
			return false;

		if (!directory.mkdirs()) {
			displayErrorDialog("Target directory could not be created.");
			giveFocusToDestination();
			return false;
		}
	}

	return true;
}
/**
 *	If the target for export does not exist then attempt to create it.
 *	Answer a boolean indicating whether the target exists (ie.- if it
 *	either pre-existed or this method was able to create it)
 *
 *	@return boolean
 */
protected boolean ensureTargetIsValid(File targetDirectory) {
	if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
		displayErrorDialog("Target directory already exists as a file.");
		giveFocusToDestination();
		return false;
	}

	return ensureDirectoryExists(targetDirectory);
}
/**
 *  Set up and execute the passed Operation.  Answer a boolean indicating success.
 *
 *  @return boolean
 */
protected boolean executeExportOperation(FileSystemExportOperation op) {
	op.setCreateLeadupStructure(createDirectoryStructureCheckbox.getSelection());
	op.setOverwriteFiles(overwriteExistingFilesCheckbox.getSelection());
	
	try {
		getContainer().run(true, true, op);
	} catch (InterruptedException e) {
		return false;
	} catch (InvocationTargetException e) {
		displayErrorDialog(e.getTargetException().getMessage());
		return false;
	}

	IStatus status = op.getStatus();
	if (!status.isOK()) {
		ErrorDialog.openError(getContainer().getShell(),
			"Export Problems",
			null,   // no special message
			status);
		return false;
	}
	
	return true;
}
/**
 *	The Finish button was pressed.  Try to do the required work now and answer
 *	a boolean indicating success.  If false is returned then the wizard will
 *	not close.
 *
 *	@return boolean
 */
public boolean finish() {
	if (!ensureTargetIsValid(new File(getDestinationValue())))
		return false;

	List resourcesToExport = getSelectedResources();

	if (!ensureResourcesLocal(resourcesToExport))
		return false;

	if (resourcesToExport.size() > 0)
		return executeExportOperation(
			new FileSystemExportOperation(
				null,
				resourcesToExport,
				getDestinationValue(),
				this));

	MessageDialog.openInformation(
		getContainer().getShell(),
		"Information",
		"There are no resources currently selected for export.");

	return false;
}
/**
 *	Answer the string to display in self as the destination type
 *
 *	@return java.lang.String
 */
protected String getDestinationLabel() {
	return "Directory:";
}
/**
 *	Answer the contents of self's destination specification widget
 *
 *	@return java.lang.String
 */
protected String getDestinationValue() {
	return destinationNameField.getText().trim();
}
/**
 *	Set the current input focus to self's destination entry field
 */
protected void giveFocusToDestination() {
	destinationNameField.setFocus();
}
/**
 *	Open an appropriate destination browser so that the user can specify a source
 *	to import from
 */
protected void handleDestinationBrowseButtonPressed() {
	DirectoryDialog dialog =
		new DirectoryDialog(getContainer().getShell(), SWT.SAVE);
	dialog.setMessage(SELECT_DESTINATION_MESSAGE);
	dialog.setFilterPath(getDestinationValue());
	String selectedDirectoryName = dialog.open();

	if (selectedDirectoryName != null) {
		setErrorMessage(null);
		setDestinationValue(selectedDirectoryName);
	}
}
/**
 * Handle all events and enablements for widgets in this page
 * @param e Event
 */
public void handleEvent(Event e) {
	Widget source = e.widget;

	if (source == destinationBrowseButton)
		handleDestinationBrowseButtonPressed();

	updatePageCompletion();
}
/**
 *	Hook method for saving widget values for restoration by the next instance
 *	of this class.
 */
protected void internalSaveWidgetValues() {
	// update directory names history
	IDialogSettings settings = getDialogSettings();
	if(settings != null) {
		String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
		if (directoryNames == null)
			directoryNames = new String[0];

		directoryNames = addToHistory(directoryNames,getDestinationValue());
		settings.put(
			STORE_DESTINATION_NAMES_ID,
			directoryNames);

		// options
		settings.put(
			STORE_OVERWRITE_EXISTING_FILES_ID,
			overwriteExistingFilesCheckbox.getSelection());

		settings.put(
			STORE_CREATE_STRUCTURE_ID,
			createDirectoryStructureCheckbox.getSelection());

	}
}
/**
 *	Hook method for restoring widget values to the values that they held
 *	last time this wizard was used to completion.
 */
protected void restoreWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	if(settings != null) {
		String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
		if (directoryNames == null)
			return;		// ie.- no settings stored

		// destination
		setDestinationValue(directoryNames[0]);
		for (int i = 0; i < directoryNames.length; i++)
			addDestinationItem(directoryNames[i]);

		// options
		overwriteExistingFilesCheckbox.setSelection(
			settings.getBoolean(STORE_OVERWRITE_EXISTING_FILES_ID));

		createDirectoryStructureCheckbox.setSelection(
			settings.getBoolean(STORE_CREATE_STRUCTURE_ID));

	} 
}
/**
 *	Set the contents of the receivers destination specification widget to
 *	the passed value
 *
 */
protected void setDestinationValue(String value) {
	destinationNameField.setText(value);
}
/**
 *	Answer a boolean indicating whether the receivers destination specification
 *	widgets currently all contain valid values.
 */
protected boolean validateDestinationGroup() {
	String destinationValue = getDestinationValue();
	if (destinationValue.length() == 0) {
		setMessage(DESTINATION_EMPTY_MESSAGE);
		return false;
	} else
		return true;
}
}
