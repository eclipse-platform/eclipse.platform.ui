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
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.FileSystemElement;

/**
 *	Page 1 of the base resource import-from-zip Wizard.
 *
 *	Note that importing from .jar is identical to importing from .zip, so
 *	all references to .zip in this class are equally applicable to .jar
 *	references.
 * @deprecated use WizardZipFileResourceImportPage1
 */
/*package*/ class WizardZipFileImportPage1 extends WizardFileSystemImportPage1 implements ISelectionChangedListener, Listener {
	private ZipFileStructureProvider providerCache;

	// constants
	private static final String	FILE_IMPORT_MASK = "*.jar;*.zip";//$NON-NLS-1$
	
	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID = "WizardZipFileImportPage1.STORE_SOURCE_NAMES_ID";//$NON-NLS-1$
	private final static String STORE_IMPORT_ALL_RESOURCES_ID = "WizardZipFileImportPage1.STORE_IMPORT_ALL_ENTRIES_ID";//$NON-NLS-1$
	private final static String STORE_OVERWRITE_EXISTING_RESOURCES_ID = "WizardZipFileImportPage1.STORE_OVERWRITE_EXISTING_RESOURCES_ID";//$NON-NLS-1$
	private final static String STORE_SELECTED_TYPES_ID = "WizardZipFileImportPage1.STORE_SELECTED_TYPES_ID";//$NON-NLS-1$
/**
 *	Creates an instance of this class
 */
public WizardZipFileImportPage1(IWorkbench aWorkbench, IStructuredSelection selection) {
	super("zipFileImportPage1", aWorkbench, selection);//$NON-NLS-1$
	setTitle(DataTransferMessages.getString("ZipExport.exportTitle")); //$NON-NLS-1$
	setDescription(DataTransferMessages.getString("ZipImport.description")); //$NON-NLS-1$
}
/**
 * Called when the user presses the Cancel button. Return a boolean
 * indicating permission to close the wizard.
 */
public boolean cancel() {
	clearProviderCache();
	return true;
}
/**
 * Clears the cached structure provider after first finalizing
 * it properly.
 */
protected void clearProviderCache() {
	if (providerCache != null) {
		closeZipFile(providerCache.getZipFile());
		providerCache = null;
	}
}
/**
 * Attempts to close the passed zip file, and answers a boolean indicating success.
 */
protected boolean closeZipFile(ZipFile file) {
	try {
		file.close();
	} catch (IOException e) {
		displayErrorDialog(DataTransferMessages.format("ZipImport.couldNotClose", new Object [] {file.getName()})); //$NON-NLS-1$
		return false;
	}

	return true;
}
/**
 *	Create the import options specification widgets.
 */
protected void createOptionsGroup(Composite parent) {
	// options group
	Composite optionsGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	optionsGroup.setLayout(layout);
	optionsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	// overwrite... checkbox
	overwriteExistingResourcesCheckbox = new Button(optionsGroup,SWT.CHECK);
	overwriteExistingResourcesCheckbox.setText(DataTransferMessages.getString("FileImport.overwriteExisting")); //$NON-NLS-1$
}
/**
 *	Display the appropriate string representing a selection of the
 *	passed size.
 */
protected void displaySelectedCount(int selectedEntryCount) {
	if (selectedEntryCount == 1)
		detailsDescriptionLabel.setText(DataTransferMessages.getString("ImportPage.oneSelected")); //$NON-NLS-1$
	else
		detailsDescriptionLabel.setText(DataTransferMessages.format("ZipImport.entriesSelected",new Object[] {String.valueOf(selectedEntryCount)})); //$NON-NLS-1$
}
/**
 *	Answer a boolean indicating whether the specified source currently exists
 *	and is valid (ie.- proper format)
 */
protected boolean ensureSourceIsValid() {
	ZipFile specifiedFile = getSpecifiedSourceFile();

	if (specifiedFile == null)
		return false;

	return closeZipFile(specifiedFile);
}
/**
 * The Finish button was pressed.  Try to do the required work now and answer
 * a boolean indicating success.  If <code>false</code> is returned then the
 * wizard will not close.
 */
public boolean finish() {
	if (!super.finish())
		return false;

	clearProviderCache();
	return true;
}
/**
 *	Answer the root FileSystemElement that represents the contents of the
 *	currently-specified .zip file.  If this FileSystemElement is not
 *	currently defined then create and return it.
 */
protected FileSystemElement getFileSystemTree() {
	if (getRoot() != null)
		return getRoot();

	ZipFile sourceFile = getSpecifiedSourceFile();
	if (sourceFile == null)
		return null;

	ZipFileStructureProvider provider = getStructureProvider(sourceFile);
	return selectFiles(provider.getRoot(),provider);
}
/**
 *	Answer the string to display as the label for the source specification field
 */
protected String getSourceLabel() {
	return DataTransferMessages.getString("ZipExport.destinationLabel"); //$NON-NLS-1$
}
/**
 *	Answer a handle to the zip file currently specified as being the source.
 *	Return null if this file does not exist or is not of valid format.
 */
protected ZipFile getSpecifiedSourceFile() {
	try {
		return new ZipFile(sourceNameField.getText());
	} catch (ZipException e) {
		displayErrorDialog(DataTransferMessages.getString("ZipImport.badFormat")); //$NON-NLS-1$
	} catch (IOException e) {
		displayErrorDialog(DataTransferMessages.getString("ZipImport.couldNotRead")); //$NON-NLS-1$
	}
	
	sourceNameField.setFocus();
	return null;
}
/**
 * Returns a structure provider for the specified zip file.
 */
protected ZipFileStructureProvider getStructureProvider(ZipFile targetZip) {
	if (providerCache == null)
		providerCache = new ZipFileStructureProvider(targetZip);
	else if (!providerCache.getZipFile().getName().equals(targetZip.getName())) {
		clearProviderCache();               // ie.- new value, so finalize&remove old value
		providerCache = new ZipFileStructureProvider(targetZip);
	} else if (!providerCache.getZipFile().equals(targetZip))
		closeZipFile(targetZip);            // ie.- duplicate handle to same .zip
		
	return providerCache;
}
/**
 *	Open a FileDialog so that the user can specify the source
 *	file to import from
 */
protected void handleSourceBrowseButtonPressed() {
	String selectedFile = queryZipFileToImport();

	if (selectedFile != null) {
		if (!selectedFile.equals(sourceNameField.getText())) {
			resetSelection();
			sourceNameField.setText(selectedFile);
		}
	}
}
/**
 *	Recursively import all resources starting at the user-specified source location.
 *	Answer a boolean indicating success.
 */
protected boolean importAllResources() {
	ZipFileStructureProvider structureProvider = getStructureProvider(getSpecifiedSourceFile());
	
	return executeImportOperation(
		new ImportOperation(
			getContainerFullPath(),
			structureProvider.getRoot(),
			structureProvider,
			this));
}
/**
 *  Import the resources with extensions as specified by the user
 */
protected boolean importResources(List fileSystemObjects) {
	ZipFileStructureProvider structureProvider = getStructureProvider(getSpecifiedSourceFile());
	
	return executeImportOperation(
		new ImportOperation(
			getContainerFullPath(),
			structureProvider.getRoot(),
			structureProvider,
			this,
			fileSystemObjects));
}
/**
 * Initializes the specified operation appropriately.
 */
protected void initializeOperation(ImportOperation op) {
	op.setOverwriteResources(overwriteExistingResourcesCheckbox.getSelection());
}
/**
 * Opens a file selection dialog and returns a string representing the
 * selected file, or <code>null</code> if the dialog was canceled.
 */
protected String queryZipFileToImport() {
	FileDialog dialog = new FileDialog(sourceNameField.getShell(),SWT.OPEN);
	dialog.setFilterExtensions(new String[] {FILE_IMPORT_MASK});
	
	String currentSourceString = sourceNameField.getText();
	int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
	if (lastSeparatorIndex != -1)
		dialog.setFilterPath(currentSourceString.substring(0,lastSeparatorIndex));
		
	return dialog.open();
}
/**
 *	Use the dialog store to restore widget values to the values that they held
 *	last time this wizard was used to completion
 */
protected void restoreWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	if(settings != null) {
		String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
		if (sourceNames == null)
			return;		// ie.- no settings stored
		
		// set all/specific types radios and related enablements
		if (settings.getBoolean(STORE_IMPORT_ALL_RESOURCES_ID)) {
			importAllResourcesRadio.setSelection(true);
			importTypedResourcesRadio.setSelection(false);
		} else {
			importTypedResourcesRadio.setSelection(true);
			importAllResourcesRadio.setSelection(false);
		}

		// set filenames history
		sourceNameField.setText(sourceNames[0]); 
		for (int i = 0; i < sourceNames.length; i++)
			sourceNameField.add(sourceNames[i]);

		// set selected types
		String[] selectedTypes = settings.getArray(STORE_SELECTED_TYPES_ID);
		if (selectedTypes.length > 0)
			typesToImportField.setText((String)selectedTypes[0]);
		for (int i = 0; i < selectedTypes.length; i++)
			typesToImportField.add((String)selectedTypes[i]);
			
		// radio buttons and checkboxes	
		overwriteExistingResourcesCheckbox.setSelection(
			settings.getBoolean(STORE_OVERWRITE_EXISTING_RESOURCES_ID));
	}
}
/**
 * 	Since Finish was pressed, write widget values to the dialog store so that they
 *	will persist into the next invocation of this wizard page.
 *
 *	Note that this method is identical to the one that appears in the superclass.
 *	This is necessary because proper overriding of instance variables is not occurring.
 */
protected void saveWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	if(settings != null) {
		// update source names history
		String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
		if (sourceNames == null)
			sourceNames = new String[0];

		sourceNames = addToHistory(sourceNames,sourceNameField.getText());
		settings.put(
			STORE_SOURCE_NAMES_ID,
			sourceNames);

		// update specific types to import history
		String[] selectedTypesNames = settings.getArray(STORE_SELECTED_TYPES_ID);
		if (selectedTypesNames == null)
			selectedTypesNames = new String[0];

		if (importTypedResourcesRadio.getSelection())
			selectedTypesNames = addToHistory(selectedTypesNames,typesToImportField.getText());
		settings.put(
			STORE_SELECTED_TYPES_ID,
			selectedTypesNames);

		// radio buttons and checkboxes	
		settings.put(
			STORE_IMPORT_ALL_RESOURCES_ID,
			importAllResourcesRadio.getSelection());
	
		settings.put(
			STORE_OVERWRITE_EXISTING_RESOURCES_ID,
			overwriteExistingResourcesCheckbox.getSelection());	
	}
}
}
