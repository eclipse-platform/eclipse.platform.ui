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
package org.eclipse.ui.wizards.datatransfer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.WizardExportPage;

/**
 *	Page 1 of the base resource export-to-file-system Wizard
 *  @deprecated use WizardFileSystemResourceExportPage1
 */
/*package*/ class WizardFileSystemExportPage1 extends WizardExportPage implements Listener {

	// widgets
	private Combo				destinationNameField;
	private Button				destinationBrowseButton;
	private Button				overwriteExistingFilesCheckbox;
	private Button				createDirectoryStructureCheckbox;
	private Button				createDirectoriesForSelectedContainersCheckbox;

	// constants
	private static final int	SIZING_TEXT_FIELD_WIDTH = 250;
	
	// dialog store id constants
	private static final String	STORE_DESTINATION_NAMES_ID = "WizardFileSystemExportPage1.STORE_DESTINATION_NAMES_ID";//$NON-NLS-1$
	private static final String	STORE_OVERWRITE_EXISTING_FILES_ID = "WizardFileSystemExportPage1.STORE_OVERWRITE_EXISTING_FILES_ID";//$NON-NLS-1$
	private static final String	STORE_CREATE_STRUCTURE_ID = "WizardFileSystemExportPage1.STORE_CREATE_STRUCTURE_ID";//$NON-NLS-1$
	private static final String	STORE_CREATE_DIRECTORIES_FOR_SPECIFIED_CONTAINER_ID = "WizardFileSystemExportPage1.STORE_CREATE_DIRECTORIES_FOR_SPECIFIED_CONTAINER_ID";//$NON-NLS-1$
/**
 *	Create an instance of this class
 */
protected WizardFileSystemExportPage1(String name, IStructuredSelection selection) {
	super(name, selection);
}
/**
 *	Create an instance of this class
 */
public WizardFileSystemExportPage1(IStructuredSelection selection) {
	this("fileSystemExportPage1", selection);//$NON-NLS-1$
	setTitle(DataTransferMessages.getString("DataTransfer.fileSystemTitle")); //$NON-NLS-1$
	setDescription(DataTransferMessages.getString("FileExport.exportLocalFileSystem")); //$NON-NLS-1$
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
	destinationBrowseButton.setText(DataTransferMessages.getString("DataTransfer.browse")); //$NON-NLS-1$
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
	overwriteExistingFilesCheckbox.setText(DataTransferMessages.getString("ExportFile.overwriteExisting")); //$NON-NLS-1$

	// create directory structure checkbox
	createDirectoryStructureCheckbox = new Button(optionsGroup,SWT.CHECK|SWT.LEFT);
	createDirectoryStructureCheckbox.setText(DataTransferMessages.getString("ExportFile.createDirectoryStructure")); //$NON-NLS-1$
	createDirectoryStructureCheckbox.addListener(SWT.Selection,this);

	// create directory for container checkbox
	createDirectoriesForSelectedContainersCheckbox = new Button(optionsGroup,SWT.CHECK|SWT.LEFT);
	createDirectoriesForSelectedContainersCheckbox.setText(DataTransferMessages.getString("ExportFile.createDirectoriesForSelected")); //$NON-NLS-1$

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
		if (!queryYesNoQuestion(DataTransferMessages.getString("DataTransfer.createTargetDirectory"))) //$NON-NLS-1$
			return false;

		if (!directory.mkdirs()) {
			displayErrorDialog(DataTransferMessages.getString("DataTransfer.directoryCreationError")); //$NON-NLS-1$
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
		displayErrorDialog(DataTransferMessages.getString("FileExport.directoryExists")); //$NON-NLS-1$
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
	op.setCreateContainerDirectories(createDirectoriesForSelectedContainersCheckbox.getSelection());
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
			DataTransferMessages.getString("DataTransfer.exportProblems"), //$NON-NLS-1$
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

	// about to invoke the operation so save our state
	saveWidgetValues();
	
	if (resourcesToExport.size() > 0)
		return executeExportOperation(
			new FileSystemExportOperation(
				getSourceResource(),
				resourcesToExport,
				getDestinationValue(),
				this));

	MessageDialog.openInformation(
		getContainer().getShell(),
		DataTransferMessages.getString("DataTransfer.information"), //$NON-NLS-1$
		DataTransferMessages.getString("FileExport.noneSelected")); //$NON-NLS-1$

	return false;
}
/**
 *	Answer the string to display in self as the destination type
 *
 *	@return java.lang.String
 */
protected String getDestinationLabel() {
	return DataTransferMessages.getString("DataTransfer.directory"); //$NON-NLS-1$
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
	DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(),SWT.SAVE);
	dialog.setMessage(DataTransferMessages.getString("DataTransfer.selectDestination")); //$NON-NLS-1$
	dialog.setFilterPath(getDestinationValue());
	String selectedDirectoryName = dialog.open();
	
	if (selectedDirectoryName != null)
		setDestinationValue(selectedDirectoryName);
}
/**
 *	Handle all events and enablements for widgets in this page
 *
 *	@param e org.eclipse.swt.widgets.Event
 */
public void handleEvent(Event e) {
	Widget source = e.widget;

	if (source == destinationBrowseButton)
		handleDestinationBrowseButtonPressed();

	super.handleEvent(e);
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

		settings.put(
			STORE_CREATE_DIRECTORIES_FOR_SPECIFIED_CONTAINER_ID,
			createDirectoriesForSelectedContainersCheckbox.getSelection());

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

		createDirectoriesForSelectedContainersCheckbox.setSelection(
			settings.getBoolean(STORE_CREATE_DIRECTORIES_FOR_SPECIFIED_CONTAINER_ID));
	} 
}
/**
 *	Set the contents of self's destination specification widget to
 *	the passed value
 *
 *	@param value java.lang.String
 */
protected void setDestinationValue(String value) {
	destinationNameField.setText(value);
}
/**
 *	Answer a boolean indicating whether self's destination specification
 *	widgets currently all contain valid values.
 *
 *	@return boolean
 */
protected boolean validateDestinationGroup() {
	return !getDestinationValue().equals("");//$NON-NLS-1$
}
}
