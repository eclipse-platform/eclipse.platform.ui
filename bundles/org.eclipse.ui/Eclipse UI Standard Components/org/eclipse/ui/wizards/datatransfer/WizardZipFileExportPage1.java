package org.eclipse.ui.wizards.datatransfer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

/**
 *	Page 1 of the base resource export-to-zip Wizard
 */
/*package*/ class WizardZipFileExportPage1 extends WizardFileSystemExportPage1 {

	// widgets
	protected Button			overwriteExistingFileCheckbox;
	protected Button			createDirectoryStructureCheckbox;
	protected Button			compressContentsCheckbox;

	// constants
	protected static final int	COMBO_HISTORY_LENGTH = 5;
	
	// dialog store id constants
	private final static String	STORE_DESTINATION_NAMES_ID = "WizardZipFileExportPage1.STORE_DESTINATION_NAMES_ID";
	private final static String	STORE_OVERWRITE_EXISTING_FILE_ID = "WizardZipFileExportPage1.STORE_OVERWRITE_EXISTING_FILE_ID";
	private final static String	STORE_CREATE_STRUCTURE_ID = "WizardZipFileExportPage1.STORE_CREATE_STRUCTURE_ID";
	private final static String	STORE_COMPRESS_CONTENTS_ID = "WizardZipFileExportPage1.STORE_COMPRESS_CONTENTS_ID";
/**
 *	Create an instance of this class.  Note that this constructor
 *	is here primarily to keep the JarFileExportPage1 subclass happy
 *
 *	@param name java.lang.String
 */
protected WizardZipFileExportPage1(String name, IStructuredSelection selection) {
	super(name, selection);
}
/**
 *	Create an instance of this class
 */
public WizardZipFileExportPage1(IStructuredSelection selection) {
	this("zipFileExportPage1", selection);
	setTitle("Zip file");
	setDescription("Export resources to a Zip file on the local file system.");
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
	overwriteExistingFileCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
	overwriteExistingFileCheckbox.setText("Overwrite existing file without warning");

	// create directory structure checkbox
	createDirectoryStructureCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
	createDirectoryStructureCheckbox.setText("Create directory structure");

	// compress... checkbox
	compressContentsCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
	compressContentsCheckbox.setText("Compress the contents of the file");

	// initial setup
	createDirectoryStructureCheckbox.setSelection(true);
	compressContentsCheckbox.setSelection(true);
}
/**
 * Returns a boolean indicating whether the directory portion of the
 * passed pathname is valid and available for use.
 *
 * @return boolean
 */
protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
	int separatorIndex = fullPathname.lastIndexOf(File.separator);

	if (separatorIndex == -1)	// ie.- default dir, which is fine
		return true;
		
	return ensureTargetIsValid(new File(fullPathname.substring(0,separatorIndex)));
}
/**
 * Returns a boolean indicating whether the passed File handle is
 * is valid and available for use.
 *
 * @return boolean
 */
protected boolean ensureTargetFileIsValid(File targetFile) {
	if (targetFile.exists() && targetFile.isDirectory()) {
		displayErrorDialog("Export destination must be a file, not a directory.");
		giveFocusToDestination();
		return false;
	}

	if (targetFile.exists()) {
		if (!overwriteExistingFileCheckbox.getSelection() && targetFile.canWrite()) {
			if (!queryYesNoQuestion("Target file already exists.  Would you like to overwrite it?"))
				return false;
		}

		if (!targetFile.canWrite()) {
			displayErrorDialog("Export destination already exists and cannot be overwritten.");
			giveFocusToDestination();
			return false;
		}
	}

	return true;
}
/**
 * Ensures that the target output file and its containing directory are
 * both valid and able to be used.  Answer a boolean indicating these.
 *
 * @return boolean
 */
protected boolean ensureTargetIsValid() {
	String targetPath = getDestinationValue();

	if (!ensureTargetDirectoryIsValid(targetPath))
		return false;
	
	if (!ensureTargetFileIsValid(new File(targetPath)))
		return false;
		
	return true;	
}
/**
 *  Export the passed resource and recursively export all of its child resources
 *  (iff it's a container).  Answer a boolean indicating success.
 *
 *  @return boolean
 */
protected boolean executeExportOperation(ZipFileExportOperation op) {
	op.setCreateLeadupStructure(createDirectoryStructureCheckbox.getSelection());
	op.setUseCompression(compressContentsCheckbox.getSelection());
	
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
	if (!ensureTargetIsValid())
		return false;

	List resourcesToExport = getSelectedResources();
	if (!ensureResourcesLocal(resourcesToExport))
		return false;

	// about to invoke the operation so save our state
	saveWidgetValues();

	if (resourcesToExport.size() > 0)
		return executeExportOperation(
			new ZipFileExportOperation(
				null,
				resourcesToExport,
				getDestinationValue()));

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
	return "Zip file:";
}
/**
 *	Answer the contents of self's destination specification widget.  If this
 *	value does not have the required suffix then add it first.
 *
 *	@return java.lang.String
 */
protected String getDestinationValue() {
	String requiredSuffix = getOutputSuffix();
	String destinationText = super.getDestinationValue();

	if (!destinationText.toLowerCase().endsWith(requiredSuffix.toLowerCase()))
		destinationText += requiredSuffix;
		
	return destinationText;
}
/**
 *	Answer the suffix that files exported from this wizard must have.
 *	If this suffix is a file extension (which is typically the case)
 *	then it must include the leading period character.
 *
 *	@return java.lang.String
 */
protected String getOutputSuffix() {
	return ".zip";
}
/**
 *	Open an appropriate destination browser so that the user can specify a source
 *	to import from
 */
protected void handleDestinationBrowseButtonPressed() {
	FileDialog dialog = new FileDialog(getContainer().getShell(),SWT.SAVE);
	dialog.setFilterExtensions(new String[] {"*.jar;*.zip"});
	
	String currentSourceString = getDestinationValue();
	int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
	if (lastSeparatorIndex != -1)
		dialog.setFilterPath(currentSourceString.substring(0,lastSeparatorIndex));
	String selectedFileName = dialog.open();

	if (selectedFileName != null)
		setDestinationValue(selectedFileName);
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
		String[] newDirectoryNames = new String[directoryNames.length + 1];
		System.arraycopy(directoryNames,0,newDirectoryNames,1,directoryNames.length);
		newDirectoryNames[0] = getDestinationValue();
	
		settings.put(
			STORE_DESTINATION_NAMES_ID,
			directoryNames);

		// options
		settings.put(
			STORE_OVERWRITE_EXISTING_FILE_ID,
			overwriteExistingFileCheckbox.getSelection());

		settings.put(
			STORE_CREATE_STRUCTURE_ID,
			createDirectoryStructureCheckbox.getSelection());

		settings.put(
			STORE_COMPRESS_CONTENTS_ID,
			compressContentsCheckbox.getSelection());
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
		if (directoryNames == null || directoryNames.length == 0)
			return;		// ie.- no settings stored

		// destination
		setDestinationValue(directoryNames[0]); 
		for (int i = 0; i < directoryNames.length; i++)
			addDestinationItem(directoryNames[i]);

		// options
		overwriteExistingFileCheckbox.setSelection(
			settings.getBoolean(STORE_OVERWRITE_EXISTING_FILE_ID));

		createDirectoryStructureCheckbox.setSelection(
			settings.getBoolean(STORE_CREATE_STRUCTURE_ID));

		compressContentsCheckbox.setSelection(
			settings.getBoolean(STORE_COMPRESS_CONTENTS_ID));
	}
}
}
