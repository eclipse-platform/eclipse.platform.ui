package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 *	Page 1 of the base resource export-to-zip Wizard
 */
/*package*/
class WizardZipFileResourceExportPage1
	extends WizardFileSystemResourceExportPage1 {

	// widgets
	protected Button compressContentsCheckbox;

	// constants
	protected static final int COMBO_HISTORY_LENGTH = 5;

	// dialog store id constants
	private final static String STORE_DESTINATION_NAMES_ID = "WizardZipFileResourceExportPage1.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$
	private final static String STORE_CREATE_STRUCTURE_ID = "WizardZipFileResourceExportPage1.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$
	private final static String STORE_COMPRESS_CONTENTS_ID = "WizardZipFileResourceExportPage1.STORE_COMPRESS_CONTENTS_ID"; //$NON-NLS-1$
	/**
	 *	Create an instance of this class. 
	 *
	 *	@param name java.lang.String
	 */
	protected WizardZipFileResourceExportPage1(
		String name,
		IStructuredSelection selection) {
		super(name, selection);
	}
	/**
	 * Create an instance of this class
	 * @param IStructuredSelection selection
	 */
	public WizardZipFileResourceExportPage1(IStructuredSelection selection) {
		this("zipFileExportPage1", selection); //$NON-NLS-1$
		setTitle(DataTransferMessages.getString("ZipExport.exportTitle")); //$NON-NLS-1$
		setDescription(DataTransferMessages.getString("ZipExport.description")); //$NON-NLS-1$
	}
	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			getControl(),
			IDataTransferHelpContextIds.ZIP_FILE_EXPORT_WIZARD_PAGE);
	}
	/**
	 *	Create the export options specification widgets.
	 *
	 */
	protected void createOptionsGroupButtons(Group optionsGroup) {

		Font font = optionsGroup.getFont();
		// compress... checkbox
		compressContentsCheckbox =
			new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		compressContentsCheckbox.setText(DataTransferMessages.getString("ZipExport.compressContents")); //$NON-NLS-1$
		compressContentsCheckbox.setFont(font);

		createDirectoryStructureOptions(optionsGroup, font);

		// initial setup
		createDirectoryStructureButton.setSelection(true);
		createSelectionOnlyButton.setSelection(false);
		compressContentsCheckbox.setSelection(true);
	}
	/**
	 * Returns a boolean indicating whether the directory portion of the
	 * passed pathname is valid and available for use.
	 */
	protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
		int separatorIndex = fullPathname.lastIndexOf(File.separator);

		if (separatorIndex == -1) // ie.- default dir, which is fine
			return true;

		return ensureTargetIsValid(
			new File(fullPathname.substring(0, separatorIndex)));
	}
	/**
	 * Returns a boolean indicating whether the passed File handle is
	 * is valid and available for use.
	 */
	protected boolean ensureTargetFileIsValid(File targetFile) {
		if (targetFile.exists() && targetFile.isDirectory()) {
			displayErrorDialog(DataTransferMessages.getString("ZipExport.mustBeFile")); //$NON-NLS-1$
			giveFocusToDestination();
			return false;
		}

		if (targetFile.exists()) {
			if (targetFile.canWrite()) {
				if (!queryYesNoQuestion(DataTransferMessages.getString("ZipExport.alreadyExists"))) //$NON-NLS-1$
					return false;
			}

			if (!targetFile.canWrite()) {
				displayErrorDialog(DataTransferMessages.getString("ZipExport.alreadyExistsError")); //$NON-NLS-1$
				giveFocusToDestination();
				return false;
			}
		}

		return true;
	}
	/**
	 * Ensures that the target output file and its containing directory are
	 * both valid and able to be used.  Answer a boolean indicating validity.
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
	 */
	protected boolean executeExportOperation(ZipFileExportOperation op) {
		op.setCreateLeadupStructure(
			createDirectoryStructureButton.getSelection());
		op.setUseCompression(compressContentsCheckbox.getSelection());

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(), DataTransferMessages.getString("DataTransfer.exportProblems"), //$NON-NLS-1$
			null, // no special message
			status);
			return false;
		}

		return true;
	}
	/**
	 * The Finish button was pressed.  Try to do the required work now and answer
	 * a boolean indicating success.  If false is returned then the wizard will
	 * not close.
	 * @returns boolean
	 */
	public boolean finish() {
		if (!ensureTargetIsValid())
			return false;

		List resourcesToExport = getWhiteCheckedResources();

		//Save dirty editors if possible but do not stop if not all are saved
		saveDirtyEditors();
		// about to invoke the operation so save our state
		saveWidgetValues();

		if (resourcesToExport.size() > 0)
			return executeExportOperation(
				new ZipFileExportOperation(
					null,
					resourcesToExport,
					getDestinationValue()));

		MessageDialog.openInformation(getContainer().getShell(), DataTransferMessages.getString("DataTransfer.information"), //$NON-NLS-1$
		DataTransferMessages.getString("FileExport.noneSelected")); //$NON-NLS-1$

		return false;
	}
	/**
	 *	Answer the string to display in the receiver as the destination type
	 */
	protected String getDestinationLabel() {
		return DataTransferMessages.getString("ZipExport.destinationLabel"); //$NON-NLS-1$
	}
	/**
	 *	Answer the contents of self's destination specification widget.  If this
	 *	value does not have the required suffix then add it first.
	 */
	protected String getDestinationValue() {
		String requiredSuffix = getOutputSuffix();
		String destinationText = super.getDestinationValue();

		if (destinationText.length() != 0) {
			// only append a suffix if a value has been specified for the destination
			if (!destinationText
				.toLowerCase()
				.endsWith(requiredSuffix.toLowerCase()))
				destinationText += requiredSuffix;
		}

		return destinationText;
	}
	/**
	 *	Answer the suffix that files exported from this wizard must have.
	 *	If this suffix is a file extension (which is typically the case)
	 *	then it must include the leading period character.
	 *
	 */
	protected String getOutputSuffix() {
		return ".zip"; //$NON-NLS-1$
	}
	/**
	 *	Open an appropriate destination browser so that the user can specify a source
	 *	to import from
	 */
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.zip" }); //$NON-NLS-1$
		dialog.setText(DataTransferMessages.getString("ZipExport.selectDestinationTitle")); //$NON-NLS-1$
		String currentSourceString = getDestinationValue();
		int lastSeparatorIndex =
			currentSourceString.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1)
			dialog.setFilterPath(
				currentSourceString.substring(0, lastSeparatorIndex));
		String selectedFileName = dialog.open();

		if (selectedFileName != null) {
			setErrorMessage(null);
			setDestinationValue(selectedFileName);
		}
	}
	/**
	 *	Hook method for saving widget values for restoration by the next instance
	 *	of this class.
	 */
	protected void internalSaveWidgetValues() {
		// update directory names history
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames =
				settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null)
				directoryNames = new String[0];

			directoryNames =
				addToHistory(directoryNames, getDestinationValue());
			settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

			settings.put(
				STORE_CREATE_STRUCTURE_ID,
				createDirectoryStructureButton.getSelection());

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
		if (settings != null) {
			String[] directoryNames =
				settings.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null || directoryNames.length == 0)
				return; // ie.- no settings stored

			// destination
			setDestinationValue(directoryNames[0]);
			for (int i = 0; i < directoryNames.length; i++)
				addDestinationItem(directoryNames[i]);

			boolean setStructure =
				settings.getBoolean(STORE_CREATE_STRUCTURE_ID);

			createDirectoryStructureButton.setSelection(setStructure);
			createSelectionOnlyButton.setSelection(!setStructure);

			compressContentsCheckbox.setSelection(
				settings.getBoolean(STORE_COMPRESS_CONTENTS_ID));
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
	 */
	protected String destinationEmptyMessage() {
		return DataTransferMessages.getString("ZipExport.destinationEmpty"); //$NON-NLS-1$
	}

}
