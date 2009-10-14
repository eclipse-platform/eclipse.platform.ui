/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;

/**
 *	Page 1 of the base resource export-to-archive Wizard.
 *
 *	@since 3.1
 */
public class WizardArchiveFileResourceExportPage1 extends
		WizardFileSystemResourceExportPage1 {

    // widgets
    protected Button compressContentsCheckbox;
    
    private Button zipFormatButton;
    private Button targzFormatButton;

    // dialog store id constants
    private final static String STORE_DESTINATION_NAMES_ID = "WizardZipFileResourceExportPage1.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private final static String STORE_CREATE_STRUCTURE_ID = "WizardZipFileResourceExportPage1.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$

    private final static String STORE_COMPRESS_CONTENTS_ID = "WizardZipFileResourceExportPage1.STORE_COMPRESS_CONTENTS_ID"; //$NON-NLS-1$

    /**
     *	Create an instance of this class. 
     *
     *	@param name java.lang.String
     */
    protected WizardArchiveFileResourceExportPage1(String name,
            IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Create an instance of this class
     * @param selection the selection
     */
    public WizardArchiveFileResourceExportPage1(IStructuredSelection selection) {
        this("zipFileExportPage1", selection); //$NON-NLS-1$
        setTitle(DataTransferMessages.ArchiveExport_exportTitle);
        setDescription(DataTransferMessages.ArchiveExport_description);
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
                IDataTransferHelpContextIds.ZIP_FILE_EXPORT_WIZARD_PAGE);
    }

    /**
     *	Create the export options specification widgets.
     *
     */
    protected void createOptionsGroupButtons(Group optionsGroup) {
    	Font font = optionsGroup.getFont();
    	optionsGroup.setLayout(new GridLayout(2, true));
    	
    	Composite left = new Composite(optionsGroup, SWT.NONE);
    	left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
    	left.setLayout(new GridLayout(1, true));

        createFileFormatOptions(left, font);
        
        // compress... checkbox
        compressContentsCheckbox = new Button(left, SWT.CHECK
                | SWT.LEFT);
        compressContentsCheckbox.setText(DataTransferMessages.ZipExport_compressContents);
        compressContentsCheckbox.setFont(font);

        Composite right = new Composite(optionsGroup, SWT.NONE);
        right.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
        right.setLayout(new GridLayout(1, true));

        createDirectoryStructureOptions(right, font);

        // initial setup
        createDirectoryStructureButton.setSelection(true);
        createSelectionOnlyButton.setSelection(false);
        compressContentsCheckbox.setSelection(true);
    }

    /**
     * Create the buttons for the group that determine if the entire or
     * selected directory structure should be created.
     * @param optionsGroup
     * @param font
     */
    protected void createFileFormatOptions(Composite optionsGroup, Font font) {
        // create directory structure radios
        zipFormatButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        zipFormatButton.setText(DataTransferMessages.ArchiveExport_saveInZipFormat);
        zipFormatButton.setSelection(true);
        zipFormatButton.setFont(font);

        // create directory structure radios
        targzFormatButton = new Button(optionsGroup, SWT.RADIO | SWT.LEFT);
        targzFormatButton.setText(DataTransferMessages.ArchiveExport_saveInTarFormat);
        targzFormatButton.setSelection(false);
        targzFormatButton.setFont(font);
    }    
    
    /**
     * Returns a boolean indicating whether the directory portion of the
     * passed pathname is valid and available for use.
     */
    protected boolean ensureTargetDirectoryIsValid(String fullPathname) {
        int separatorIndex = fullPathname.lastIndexOf(File.separator);

        if (separatorIndex == -1) {
			return true;
		}

        return ensureTargetIsValid(new File(fullPathname.substring(0,
                separatorIndex)));
    }

    /**
     * Returns a boolean indicating whether the passed File handle is
     * is valid and available for use.
     */
    protected boolean ensureTargetFileIsValid(File targetFile) {
        if (targetFile.exists() && targetFile.isDirectory()) {
            displayErrorDialog(DataTransferMessages.ZipExport_mustBeFile);
            giveFocusToDestination();
            return false;
        }

        if (targetFile.exists()) {
            if (targetFile.canWrite()) {
                if (!queryYesNoQuestion(DataTransferMessages.ZipExport_alreadyExists)) {
					return false;
				}
            } else {
                displayErrorDialog(DataTransferMessages.ZipExport_alreadyExistsError);
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

        if (!ensureTargetDirectoryIsValid(targetPath)) {
			return false;
		}

        if (!ensureTargetFileIsValid(new File(targetPath))) {
			return false;
		}

        return true;
    }

    /**
     *  Export the passed resource and recursively export all of its child resources
     *  (iff it's a container).  Answer a boolean indicating success.
     */
    protected boolean executeExportOperation(ArchiveFileExportOperation op) {
        op.setCreateLeadupStructure(createDirectoryStructureButton
                .getSelection());
        op.setUseCompression(compressContentsCheckbox.getSelection());
        op.setUseTarFormat(targzFormatButton.getSelection());

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
            ErrorDialog.openError(getContainer().getShell(),
                    DataTransferMessages.DataTransfer_exportProblems,
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
    	List resourcesToExport = getWhiteCheckedResources();
    	
        if (!ensureTargetIsValid()) {
			return false;
		}

        //Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();

        return executeExportOperation(new ArchiveFileExportOperation(null,
                resourcesToExport, getDestinationValue()));
    }

    /**
     *	Answer the string to display in the receiver as the destination type
     */
    protected String getDestinationLabel() {
        return DataTransferMessages.ArchiveExport_destinationLabel;
    }

    /**
     *	Answer the contents of self's destination specification widget.  If this
     *	value does not have a suffix then add it first.
     */
    protected String getDestinationValue() {
        String idealSuffix = getOutputSuffix();
        String destinationText = super.getDestinationValue();

        // only append a suffix if the destination doesn't already have a . in 
        // its last path segment.  
        // Also prevent the user from selecting a directory.  Allowing this will 
        // create a ".zip" file in the directory
        if (destinationText.length() != 0
                && !destinationText.endsWith(File.separator)) {
            int dotIndex = destinationText.lastIndexOf('.');
            if (dotIndex != -1) {
                // the last path seperator index
                int pathSepIndex = destinationText.lastIndexOf(File.separator);
                if (pathSepIndex != -1 && dotIndex < pathSepIndex) {
                    destinationText += idealSuffix;
                }
            } else {
                destinationText += idealSuffix;
            }
        }

        return destinationText;
    }

    /**
     *	Answer the suffix that files exported from this wizard should have.
     *	If this suffix is a file extension (which is typically the case)
     *	then it must include the leading period character.
     *
     */
    protected String getOutputSuffix() {
    	if(zipFormatButton.getSelection()) {
        	return ".zip"; //$NON-NLS-1$
    	} else if(compressContentsCheckbox.getSelection()) {
    		return ".tar.gz"; //$NON-NLS-1$
    	} else {
    		return ".tar"; //$NON-NLS-1$
    	}
    }

    /**
     *	Open an appropriate destination browser so that the user can specify a source
     *	to import from
     */
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
        dialog.setFilterExtensions(new String[] { "*.zip;*.tar.gz;*.tar;*.tgz", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString
                .lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0,
                    lastSeparatorIndex));
		}
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
            String[] directoryNames = settings
                    .getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
				directoryNames = new String[0];
			}

            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);

            settings.put(STORE_CREATE_STRUCTURE_ID,
                    createDirectoryStructureButton.getSelection());

            settings.put(STORE_COMPRESS_CONTENTS_ID, compressContentsCheckbox
                    .getSelection());
        }
    }

    /**
     *	Hook method for restoring widget values to the values that they held
     *	last time this wizard was used to completion.
     */
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings
                    .getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null || directoryNames.length == 0) {
				return; // ie.- no settings stored
			}

            // destination
            setDestinationValue(directoryNames[0]);
            for (int i = 0; i < directoryNames.length; i++) {
				addDestinationItem(directoryNames[i]);
			}

            boolean setStructure = settings
                    .getBoolean(STORE_CREATE_STRUCTURE_ID);

            createDirectoryStructureButton.setSelection(setStructure);
            createSelectionOnlyButton.setSelection(!setStructure);

            compressContentsCheckbox.setSelection(settings
                    .getBoolean(STORE_COMPRESS_CONTENTS_ID));
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
    protected String destinationEmptyMessage() {
        return DataTransferMessages.ArchiveExport_destinationEmpty;
    }
    
    /**
     *	Answer a boolean indicating whether the receivers destination specification
     *	widgets currently all contain valid values.
     */
    protected boolean validateDestinationGroup() {
    	String destinationValue = getDestinationValue();
    	if (destinationValue.endsWith(".tar")) { //$NON-NLS-1$
    		compressContentsCheckbox.setSelection(false);
    		targzFormatButton.setSelection(true);
    		zipFormatButton.setSelection(false);
    	} else if (destinationValue.endsWith(".tar.gz") //$NON-NLS-1$
				|| destinationValue.endsWith(".tgz")) { //$NON-NLS-1$
    		compressContentsCheckbox.setSelection(true);
    		targzFormatButton.setSelection(true);
    		zipFormatButton.setSelection(false);
    	} else if (destinationValue.endsWith(".zip")) { //$NON-NLS-1$
    		zipFormatButton.setSelection(true);
    		targzFormatButton.setSelection(false);
    	}
    	
    	return super.validateDestinationGroup();
    }
}
