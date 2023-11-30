/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.bidi.StructuredTextTypeHandlerFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;


/**
 *	Page 1 of the base resource export-to-file-system Wizard
 */
public class WizardFileSystemResourceExportPage1 extends
		WizardExportResourcesPage implements Listener {

	// widgets
	private Combo destinationNameField;

	private Button destinationBrowseButton;

	protected Button overwriteExistingFilesCheckbox;

	protected Button createDirectoryStructureButton;

	protected Button createSelectionOnlyButton;

	protected Button resolveLinkedResourcesCheckbox;

	// dialog store id constants
	private static final String STORE_DESTINATION_NAMES_ID = "WizardFileSystemResourceExportPage1.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

	private static final String STORE_OVERWRITE_EXISTING_FILES_ID = "WizardFileSystemResourceExportPage1.STORE_OVERWRITE_EXISTING_FILES_ID"; //$NON-NLS-1$

	private static final String STORE_CREATE_STRUCTURE_ID = "WizardFileSystemResourceExportPage1.STORE_CREATE_STRUCTURE_ID"; //$NON-NLS-1$

	private static final String STORE_RESOLVE_LINKED_RESOURCES_ID = "WizardFileSystemResourceExportPage1.STORE_RESOLVE_LINKED_RESOURCES_ID"; //$NON-NLS-1$

	//messages
	private static final String SELECT_DESTINATION_MESSAGE = DataTransferMessages.FileExport_selectDestinationMessage;

	private static final String SELECT_DESTINATION_TITLE = DataTransferMessages.FileExport_selectDestinationTitle;


	/**
	 *	Create an instance of this class
	 */
	protected WizardFileSystemResourceExportPage1(String name,
			IStructuredSelection selection) {
		super(name, selection);
	}

	/**
	 * Create an instance of this class.
	 *
	 * @param selection the selection
	 */
	public WizardFileSystemResourceExportPage1(IStructuredSelection selection) {
		this("fileSystemExportPage1", selection); //$NON-NLS-1$
		setTitle(DataTransferMessages.DataTransfer_fileSystemTitle);
		setDescription(DataTransferMessages.FileExport_exportLocalFileSystem);
	}

	/**
	 *	Add the passed value to self's destination widget's history
	 *
	 *	@param value java.lang.String
	 */
	protected void addDestinationItem(String value) {
		destinationNameField.add(value);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		giveFocusToDestination();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IDataTransferHelpContextIds.FILE_SYSTEM_EXPORT_WIZARD_PAGE);
	}

	/**
	 *	Create the export destination specification widgets
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 */
	@Override
	protected void createDestinationGroup(Composite parent) {

		Font font = parent.getFont();
		// destination specification group
		Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destinationSelectionGroup.setLayout(layout);
		destinationSelectionGroup.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		destinationSelectionGroup.setFont(font);

		Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
		destinationLabel.setText(getDestinationLabel());
		destinationLabel.setFont(font);

		// destination name entry field
		destinationNameField = new Combo(destinationSelectionGroup, SWT.SINGLE
				| SWT.BORDER);
		destinationNameField.addListener(SWT.Modify, this);
		destinationNameField.addListener(SWT.Selection, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		destinationNameField.setLayoutData(data);
		destinationNameField.setFont(font);
		BidiUtils.applyBidiProcessing(destinationNameField, StructuredTextTypeHandlerFactory.FILE);

		// destination browse button
		destinationBrowseButton = new Button(destinationSelectionGroup,
				SWT.PUSH);
		destinationBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setFont(font);
		setButtonLayoutData(destinationBrowseButton);

		new Label(parent, SWT.NONE); // vertical spacer
	}

	/**
	 * Create the buttons in the options group.
	 */

	@Override
	protected void createOptionsGroupButtons(Group optionsGroup) {

		Font font = optionsGroup.getFont();
		createOverwriteExisting(optionsGroup, font);

		createDirectoryStructureOptions(optionsGroup, font);

		createResolveLinkedResources(optionsGroup, font);
	}

	/**
	 * Create the buttons for the group that determine if the entire or
	 * selected directory structure should be created.
	 */
	protected void createDirectoryStructureOptions(Composite optionsGroup, Font font) {
		// create directory structure radios
		createDirectoryStructureButton = new Button(optionsGroup, SWT.RADIO
				| SWT.LEFT);
		createDirectoryStructureButton.setText(DataTransferMessages.FileExport_createDirectoryStructure);
		createDirectoryStructureButton.setSelection(false);
		createDirectoryStructureButton.setFont(font);

		// create directory structure radios
		createSelectionOnlyButton = new Button(optionsGroup, SWT.RADIO
				| SWT.LEFT);
		createSelectionOnlyButton.setText(DataTransferMessages.FileExport_createSelectedDirectories);
		createSelectionOnlyButton.setSelection(true);
		createSelectionOnlyButton.setFont(font);
	}

	/**
	 * Create the button for checking if we should ask if we are going to
	 * overwrite existing files.
	 */
	protected void createOverwriteExisting(Group optionsGroup, Font font) {
		// overwrite... checkbox
		overwriteExistingFilesCheckbox = new Button(optionsGroup, SWT.CHECK
				| SWT.LEFT);
		overwriteExistingFilesCheckbox.setText(DataTransferMessages.ExportFile_overwriteExisting);
		overwriteExistingFilesCheckbox.setFont(font);
	}

	/**
	 * Create the button for checking if we should export linked files.
	 */
	protected void createResolveLinkedResources(Composite parent, Font font) {
		// resolve links... checkbox
		resolveLinkedResourcesCheckbox = new Button(parent, SWT.CHECK | SWT.LEFT);
		resolveLinkedResourcesCheckbox.setText(DataTransferMessages.ExportFile_resolveLinkedResources);
		resolveLinkedResourcesCheckbox.setFont(font);
		resolveLinkedResourcesCheckbox.setSelection(getShowLinkedResources());
		resolveLinkedResourcesCheckbox.addListener(SWT.Selection, this);
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
			if (!queryYesNoQuestion(DataTransferMessages.DataTransfer_createTargetDirectory)) {
				return false;
			}

			if (!directory.mkdirs()) {
				displayErrorDialog(DataTransferMessages.DataTransfer_directoryCreationError);
				giveFocusToDestination();
				return false;
			}
		}

		return true;
	}

	/**
	 * If the target for export does not exist then attempt to create it. Answer a
	 * boolean indicating whether the target exists (i.e. if it either pre-existed
	 * or this method was able to create it)
	 *
	 * @return boolean
	 */
	protected boolean ensureTargetIsValid(File targetDirectory) {
		if (targetDirectory.exists() && !targetDirectory.isDirectory()) {
			displayErrorDialog(DataTransferMessages.FileExport_directoryExists);
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
		op.setCreateLeadupStructure(createDirectoryStructureButton
				.getSelection());
		op.setOverwriteFiles(overwriteExistingFilesCheckbox.getSelection());
		op.setResolveLinks(resolveLinkedResourcesCheckbox.getSelection());

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
	 *	The Finish button was pressed.  Try to do the required work now and answer
	 *	a boolean indicating success.  If false is returned then the wizard will
	 *	not close.
	 *
	 *	@return boolean
	 */
	public boolean finish() {
		List resourcesToExport = getWhiteCheckedResources();
		if (!ensureTargetIsValid(new File(getDestinationValue()))) {
			return false;
		}


		//Save dirty editors if possible but do not stop if not all are saved
		saveDirtyEditors();
		// about to invoke the operation so save our state
		saveWidgetValues();

		return executeExportOperation(new FileSystemExportOperation(null,
				resourcesToExport, getDestinationValue(), this));
	}

	/**
	 *	Answer the string to display in self as the destination type
	 *
	 *	@return java.lang.String
	 */
	protected String getDestinationLabel() {
		return DataTransferMessages.FileExport_toDirectory;
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
		DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(),
				SWT.SAVE | SWT.SHEET);
		dialog.setMessage(SELECT_DESTINATION_MESSAGE);
		dialog.setText(SELECT_DESTINATION_TITLE);
		dialog.setFilterPath(getDestinationValue());
		String selectedDirectoryName = dialog.open();

		if (selectedDirectoryName != null) {
			setErrorMessage(null);
			setDestinationValue(selectedDirectoryName);
		}
	}

	/**
	 * Updates the content providers to show/hide linked resources
	 */
	protected void handleResolveLinkedResourcesCheckboxSelected() {
		updateContentProviders(resolveLinkedResourcesCheckbox.getSelection());
	}

	/**
	 * Handle all events and enablements for widgets in this page
	 * @param e Event
	 */
	@Override
	public void handleEvent(Event e) {
		Widget source = e.widget;

		if (source == destinationBrowseButton) {
			handleDestinationBrowseButtonPressed();
		} else if (source == resolveLinkedResourcesCheckbox) {
			handleResolveLinkedResourcesCheckboxSelected();
		}

		updatePageCompletion();
	}

	/**
	 *	Hook method for saving widget values for restoration by the next instance
	 *	of this class.
	 */
	@Override
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

			// options
			settings.put(STORE_OVERWRITE_EXISTING_FILES_ID,
					overwriteExistingFilesCheckbox.getSelection());

			settings.put(STORE_CREATE_STRUCTURE_ID,
					createDirectoryStructureButton.getSelection());

			settings.put(STORE_RESOLVE_LINKED_RESOURCES_ID, resolveLinkedResourcesCheckbox.getSelection());
		}
	}

	/**
	 *	Hook method for restoring widget values to the values that they held
	 *	last time this wizard was used to completion.
	 */
	@Override
	protected void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String[] directoryNames = settings
					.getArray(STORE_DESTINATION_NAMES_ID);
			if (directoryNames == null) {
				return; // ie.- no settings stored
			}

			// destination
			setDestinationValue(directoryNames[0]);
			for (String directoryName : directoryNames) {
				addDestinationItem(directoryName);
			}

			// options
			overwriteExistingFilesCheckbox.setSelection(settings
					.getBoolean(STORE_OVERWRITE_EXISTING_FILES_ID));

			boolean createDirectories = settings
					.getBoolean(STORE_CREATE_STRUCTURE_ID);
			createDirectoryStructureButton.setSelection(createDirectories);
			createSelectionOnlyButton.setSelection(!createDirectories);
			boolean showLinked = settings.getBoolean(STORE_RESOLVE_LINKED_RESOURCES_ID);
			if (resolveLinkedResourcesCheckbox.getSelection() != showLinked) {
				resolveLinkedResourcesCheckbox.setSelection(showLinked);
			}
		}
	}

	/**
	 *	Set the contents of the receivers destination specification widget to
	 *	the passed value
	 */
	protected void setDestinationValue(String value) {
		destinationNameField.setText(value);
	}

	/**
	 *	Answer a boolean indicating whether the receivers destination specification
	 *	widgets currently all contain valid values.
	 */
	@Override
	protected boolean validateDestinationGroup() {
		String destinationValue = getDestinationValue();
		if (destinationValue.isEmpty()) {
			setMessage(destinationEmptyMessage());
			return false;
		}

		String conflictingContainer = getConflictingContainerNameFor(destinationValue);
		if (conflictingContainer == null) {
			// no error message, but warning may exists
			String threatenedContainer = getOverlappingProjectName(destinationValue);
			if(threatenedContainer == null)
				setMessage(null);
			else
				setMessage(
					NLS.bind(DataTransferMessages.FileExport_damageWarning, threatenedContainer),
					WARNING);

		} else {
			setErrorMessage(NLS.bind(DataTransferMessages.FileExport_conflictingContainer, conflictingContainer));
			giveFocusToDestination();
			return false;
		}

		return true;
	}

	@Override
	protected boolean validateSourceGroup() {
		// there must be some resources selected for Export
		boolean isValid = true;
		List resourcesToExport = getWhiteCheckedResources();
		if (resourcesToExport.isEmpty()){
			setErrorMessage(DataTransferMessages.FileExport_noneSelected);
			isValid =  false;
		} else {
			setErrorMessage(null);
		}
		return super.validateSourceGroup() && isValid;
	}

	/**
	 * Get the message used to denote an empty destination.
	 */
	protected String destinationEmptyMessage() {
		return DataTransferMessages.FileExport_destinationEmpty;
	}

	/**
	 * Returns the name of a container with a location that encompasses targetDirectory.
	 * Returns null if there is no conflict.
	 *
	 * @param targetDirectory the path of the directory to check.
	 * @return the conflicting container name or <code>null</code>
	 */
	protected String getConflictingContainerNameFor(String targetDirectory) {

		IPath rootPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath testPath = IPath.fromOSString(targetDirectory);
		// cannot export into workspace root
		if(testPath.equals(rootPath))
			return rootPath.lastSegment();

		//Are they the same?
		if(testPath.matchingFirstSegments(rootPath) == rootPath.segmentCount()){
			String firstSegment = testPath.removeFirstSegments(rootPath.segmentCount()).segment(0);
			if(!Character.isLetterOrDigit(firstSegment.charAt(0)))
				return firstSegment;
		}

		return null;

	}

	/**
	 * Returns the name of a {@link IProject} with a location that includes
	 * targetDirectory. Returns null if there is no such {@link IProject}.
	 *
	 * @param targetDirectory
	 *            the path of the directory to check.
	 * @return the overlapping project name or <code>null</code>
	 */
	private String getOverlappingProjectName(String targetDirectory){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath testPath = IPath.fromOSString(targetDirectory);
		IContainer[] containers = root.findContainersForLocation(testPath);
		if(containers.length > 0){
			return containers[0].getProject().getName();
		}
		return null;
	}


}
