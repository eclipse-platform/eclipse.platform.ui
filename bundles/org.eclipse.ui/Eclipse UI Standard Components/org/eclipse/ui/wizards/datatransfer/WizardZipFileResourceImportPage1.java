package org.eclipse.ui.wizards.datatransfer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.wizards.datatransfer.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;

/**
 *	Page 1 of the base resource import-from-zip Wizard.
 *
 *	Note that importing from .jar is identical to importing from .zip, so
 *	all references to .zip in this class are equally applicable to .jar
 *	references.
 */
/*package*/
class WizardZipFileResourceImportPage1
	extends WizardFileSystemResourceImportPage1
	implements Listener {
	private ZipFileStructureProvider providerCache;
	ZipFileStructureProvider currentProvider;

	// constants
	private static final int COMBO_HISTORY_LENGTH = 5;
	private static final String FILE_IMPORT_MASK = "*.jar;*.zip";

	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID =
		"WizardZipFileImportPage1.STORE_SOURCE_NAMES_ID";
	private final static String STORE_IMPORT_ALL_RESOURCES_ID =
		"WizardZipFileImportPage1.STORE_IMPORT_ALL_ENTRIES_ID";
	private final static String STORE_OVERWRITE_EXISTING_RESOURCES_ID =
		"WizardZipFileImportPage1.STORE_OVERWRITE_EXISTING_RESOURCES_ID";
	private final static String STORE_SELECTED_TYPES_ID =
		"WizardZipFileImportPage1.STORE_SELECTED_TYPES_ID";
/**
 *	Creates an instance of this class
 * @param aWorkbench IWorkbench
 * @param selection IStructuredSelection
 */
public WizardZipFileResourceImportPage1(IWorkbench aWorkbench, IStructuredSelection selection) {
	super("zipFileImportPage1", aWorkbench, selection);
	setTitle("Zip file");
	setDescription("Import the contents of a Zip file from the local file system.");
}
/**
 * Called when the user presses the Cancel button. Return a boolean
 * indicating permission to close the wizard.
 *
 * @return boolean
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
		displayErrorDialog("Could not close file " + file.getName());
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
	overwriteExistingResourcesCheckbox.setText("Overwrite existing resources without warning");
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
 *
 * @return boolean
 */
public boolean finish() {
	if (!super.finish())
		return false;

	clearProviderCache();
	return true;
}
/**
 * Returns a content provider for <code>FileSystemElement</code>s that returns 
 * only files as children.
 */
protected ITreeContentProvider getFileProvider() {
	return new WorkbenchContentProvider() {
		public Object[] getChildren(Object o) {
			if (o instanceof MinimizedFileSystemElement) {
				MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
				return element.getFiles(currentProvider).getChildren(
					element);
			}
			return new Object[0];
		}
	};
}
/**
 *	Answer the root FileSystemElement that represents the contents of the
 *	currently-specified .zip file.  If this FileSystemElement is not
 *	currently defined then create and return it.
 */
protected MinimizedFileSystemElement getFileSystemTree() {
	
	ZipFile sourceFile = getSpecifiedSourceFile();
	if (sourceFile == null)
		return null;

	ZipFileStructureProvider provider = getStructureProvider(sourceFile);
	this.currentProvider = provider;
	return selectFiles(provider.getRoot(),provider);
}
/**
 * Returns a content provider for <code>FileSystemElement</code>s that returns 
 * only folders as children.
 */
protected ITreeContentProvider getFolderProvider() {
	return new WorkbenchContentProvider() {
		public Object[] getChildren(Object o) {
			if (o instanceof MinimizedFileSystemElement) {
				MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
				return element.getFolders(currentProvider).getChildren(
					element);
			}
			return new Object[0];
		}
		public boolean hasChildren(Object o) {
			if (o instanceof MinimizedFileSystemElement) {
				MinimizedFileSystemElement element = (MinimizedFileSystemElement) o;
				if (element.isPopulated())
					return getChildren(element).length > 0;
				else {
					//If we have not populated then wait until asked
					return true;
				}
			}
			return false;
		}
	};
}
/**
 *	Answer the string to display as the label for the source specification field
 */
protected String getSourceLabel() {
	return "Zip file:";
}
/**
 *	Answer a handle to the zip file currently specified as being the source.
 *	Return null if this file does not exist or is not of valid format.
 */
protected ZipFile getSpecifiedSourceFile() {
	if(sourceNameField.getText().length() == 0)
		return null;
		
	try {
		return new ZipFile(sourceNameField.getText());
	} catch (ZipException e) {
		displayErrorDialog("Source file is not of proper format.");
	} catch (IOException e) {
		displayErrorDialog("Source file could not be read.");
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
			sourceNameField.setText(selectedFile);
			resetSelection();
		}
	}
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
		
		// set filenames history
		sourceNameField.setText(sourceNames[0]); 
		for (int i = 0; i < sourceNames.length; i++)
			sourceNameField.add(sourceNames[i]);
			
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
	
		settings.put(
			STORE_OVERWRITE_EXISTING_RESOURCES_ID,
			overwriteExistingResourcesCheckbox.getSelection());	
	}
}
}
