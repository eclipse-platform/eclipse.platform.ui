package org.eclipse.ui.wizards.datatransfer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;

/**
 *	Page 1 of the base resource import-from-file-system Wizard
 *  @deprecated use WizardFileSystemResourceImportPage1
 */
/*package*/ class WizardFileSystemImportPage1 extends WizardImportPage implements ISelectionChangedListener, Listener {
	private List selectedResources;
	private FileSystemElement root;
	private IWorkbench workbench;

	// widgets
	protected Combo				typesToImportField;
	protected Button			typesToImportEditButton;
	protected Combo				sourceNameField;
	protected Button			sourceBrowseButton;
	protected Button			importAllResourcesRadio;
	protected Button			importTypedResourcesRadio;
	protected Button			detailsButton;
	protected Label				detailsDescriptionLabel;
	protected Button			overwriteExistingResourcesCheckbox;
	protected Button			createContainerStructureCheckbox;
	
	// constants
	private static final int	SIZING_TEXT_FIELD_WIDTH = 250;
	private static final String TYPE_DELIMITER = DataTransferMessages.getString("DataTransfer.typeDelimiter"); //$NON-NLS-1$

	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID = "WizardFileSystemImportPage1.STORE_SOURCE_NAMES_ID";//$NON-NLS-1$
	private final static String STORE_IMPORT_ALL_RESOURCES_ID = "WizardFileSystemImportPage1.STORE_IMPORT_ALL_FILES_ID";//$NON-NLS-1$
	private final static String STORE_OVERWRITE_EXISTING_RESOURCES_ID = "WizardFileSystemImportPage1.STORE_OVERWRITE_EXISTING_RESOURCES_ID";//$NON-NLS-1$
	private final static String STORE_CREATE_CONTAINER_STRUCTURE_ID = "WizardFileSystemImportPage1.STORE_CREATE_CONTAINER_STRUCTURE_ID";//$NON-NLS-1$
	private final static String STORE_SELECTED_TYPES_ID = "WizardFileSystemImportPage1.STORE_SELECTED_TYPES_ID";//$NON-NLS-1$
/**
 *	Creates an instance of this class
 */
protected WizardFileSystemImportPage1(String name, IWorkbench aWorkbench, IStructuredSelection selection) {
	super(name,selection);
	this.workbench = aWorkbench;
}
/**
 *	Creates an instance of this class
 */
public WizardFileSystemImportPage1(IWorkbench aWorkbench, IStructuredSelection selection) {
	this("fileSystemImportPage1", aWorkbench, selection);//$NON-NLS-1$
	setTitle(DataTransferMessages.getString("DataTransfer.fileSystemTitle")); //$NON-NLS-1$
	setDescription(DataTransferMessages.getString("FileImport.importFileSystem")); //$NON-NLS-1$
}
/**
 * Adds the recursive contents of the passed file system element to this
 * page's collection of selected resources.
 */
protected void addToSelectedResources(FileSystemElement element) {
	if (element.isDirectory()) {
		Object[] children = element.getFolders().getChildren(element);
		for (int i = 0; i < children.length; ++i) {
			addToSelectedResources((FileSystemElement) children[i]);
		}
		children = element.getFiles().getChildren(element);
		for (int i = 0; i < children.length; ++i) {
			addToSelectedResources((FileSystemElement) children[i]);
		}
	} else
		selectedResources.add(element);
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

	// create containers checkbox
	createContainerStructureCheckbox = new Button(optionsGroup,SWT.CHECK);
	createContainerStructureCheckbox.setText(DataTransferMessages.getString("FileImport.createComplete")); //$NON-NLS-1$
}
/**
 *	Create the import source specification widgets
 */
protected void createSourceGroup(Composite parent) {
	Composite sourceContainerGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	sourceContainerGroup.setLayout(layout);
	sourceContainerGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	new Label(sourceContainerGroup,SWT.NONE).setText(getSourceLabel());

	// source name entry field
	sourceNameField = new Combo(sourceContainerGroup,SWT.BORDER);
	sourceNameField.addListener(SWT.Modify,this);
	sourceNameField.addListener(SWT.Selection,this);
	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	sourceNameField.setLayoutData(data);

	// source browse button
	sourceBrowseButton = new Button(sourceContainerGroup,SWT.PUSH);
	sourceBrowseButton.setText(DataTransferMessages.getString("DataTransfer.browse")); //$NON-NLS-1$
	sourceBrowseButton.addListener(SWT.Selection,this);
	sourceBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

	// source types group
	Composite sourceTypesGroup = new Composite(parent,SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 3;
	sourceTypesGroup.setLayout(layout);
	sourceTypesGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	// all types radio
	importAllResourcesRadio = new Button(sourceTypesGroup,SWT.RADIO);
	importAllResourcesRadio.setText(DataTransferMessages.getString("DataTransfer.allTypes")); //$NON-NLS-1$
	importAllResourcesRadio.addListener(SWT.Selection,this);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.horizontalSpan = 3;
	importAllResourcesRadio.setLayoutData(data);

	// import specific types radio
	importTypedResourcesRadio = new Button(sourceTypesGroup,SWT.RADIO);
	importTypedResourcesRadio.setText(DataTransferMessages.getString("FileImport.filesofType")); //$NON-NLS-1$
	importTypedResourcesRadio.addListener(SWT.Selection,this);

	// types combo
	typesToImportField = new Combo(sourceTypesGroup, SWT.NONE);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	typesToImportField.setLayoutData(data);
	typesToImportField.addListener(SWT.Modify, this);

	// types edit button
	typesToImportEditButton = new Button(sourceTypesGroup, SWT.PUSH);
	typesToImportEditButton.setText(DataTransferMessages.getString("FileImport.edit")); //$NON-NLS-1$
	typesToImportEditButton.setLayoutData(new GridData(
		GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END));
	typesToImportEditButton.addListener(SWT.Selection, this);

	// details of files to import group
	Composite fileDetailsGroup = new Composite(parent, SWT.NONE);
	layout = new GridLayout();
	layout.numColumns = 2;
	fileDetailsGroup.setLayout(layout);
	fileDetailsGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	// details button
	detailsButton = new Button(fileDetailsGroup,SWT.PUSH);
	detailsButton.setText(DataTransferMessages.getString("DataTransfer.details")); //$NON-NLS-1$
	detailsButton.addListener(SWT.Selection,this);

	// details label
	detailsDescriptionLabel = new Label(fileDetailsGroup,SWT.NONE);
	data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	detailsDescriptionLabel.setLayoutData(data);

	// initial setup
	typesToImportField.setEnabled(false);
	typesToImportEditButton.setEnabled(false);
	importAllResourcesRadio.setSelection(true);
	resetSelection();
	sourceNameField.setFocus();
}
/**
 *	Display the appropriate string representing a selection of the
 *	passed size.
 */
protected void displaySelectedCount(int selectedFileCount) {
	if (selectedFileCount == 1)
		detailsDescriptionLabel.setText(
			DataTransferMessages.getString("DataTransfer.oneSelected"));  //$NON-NLS-1$
	else
		detailsDescriptionLabel.setText(DataTransferMessages.format("FileImport.filesSelected",new Object[] {String.valueOf(selectedFileCount)})); //$NON-NLS-1$
}
/**
 *	Answer a boolean indicating whether the specified source currently exists
 *	and is valid
 */
protected boolean ensureSourceIsValid() {
	if (new File(getSourceDirectoryName()).isDirectory())
		return true;

	displayErrorDialog(DataTransferMessages.getString("FileImport.invalidSource")); //$NON-NLS-1$
	sourceNameField.setFocus();
	return false;
}
/**
 *	Execute the passed import operation.  Answer a boolean indicating success.
 */
protected boolean executeImportOperation(ImportOperation op) {
	initializeOperation(op);
	 
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
			DataTransferMessages.getString("FileImport.importProblems"), //$NON-NLS-1$
			null,		// no special message
			status);
		return false;
	}

	return true;
}
/**
 *	The Finish button was pressed.  Try to do the required work now and answer
 *	a boolean indicating success.  If false is returned then the wizard will
 *	not close.
 */
public boolean finish() {
	if (!ensureSourceIsValid())
		return false;

	if (selectedResources == null && importAllResourcesRadio.getSelection()) {
		// about to invoke the operation so save our state
		saveWidgetValues();

		return importAllResources();
	} else {
		// ensure that files of appropriate extension will be marked as selected
		if (selectedResources == null) {
			if (getFileSystemTree() == null)
				return false;
		}

		// about to invoke the operation so save our state
		saveWidgetValues();

		if (selectedResources.size() > 0) {
			List fileSystemObjects = new ArrayList(selectedResources.size());
			Iterator resourcesEnum = selectedResources.iterator();
			while (resourcesEnum.hasNext())
				fileSystemObjects.add(
					((FileSystemElement)resourcesEnum.next()).getFileSystemObject());
	
			return importResources(fileSystemObjects);
		}

		MessageDialog.openInformation(
			getContainer().getShell(),
			DataTransferMessages.getString("DataTransfer.information"), //$NON-NLS-1$
			DataTransferMessages.getString("FileImport.noneSelected")); //$NON-NLS-1$
		
		return false;
	}
}
/**
 *	Answer the root FileSystemElement that represents the contents of
 *	the currently-specified source.  If this FileSystemElement is not
 *	currently defined then create and return it.
 */
protected FileSystemElement getFileSystemTree() {
	if (root != null)
		return root;
		
	File sourceDirectory = getSourceDirectory();
	if (sourceDirectory == null)
		return null;

	return selectFiles(sourceDirectory, FileSystemStructureProvider.INSTANCE);
}
/**
 *	Answer self's import source root element
 */
protected FileSystemElement getRoot() {
	return root;
}
/**
 *	Answer self's current collection of selected resources
 */
protected List getSelectedResources() {
	return selectedResources;
}
/**
 * Returns a File object representing the currently-named source directory iff
 * it exists as a valid directory, or <code>null</code> otherwise.
 */
protected File getSourceDirectory() {
	File sourceDirectory = new File(getSourceDirectoryName());
	if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
		displayErrorDialog(DataTransferMessages.getString("FileImport.invalidSource")); //$NON-NLS-1$
		sourceNameField.setFocus();
		return null;
	}

	return sourceDirectory;
}
/**
 *	Answer the directory name specified as being the import source.
 *	Note that if it ends with a separator then the separator is first
 *	removed so that java treats it as a proper directory
 */
private String getSourceDirectoryName() {
	IPath result = new Path(sourceNameField.getText().trim());

	if (result.getDevice() != null && result.segmentCount() == 0)	// something like "c:"
		result = result.addTrailingSeparator();
	else
		result = result.removeTrailingSeparator();

	return result.toOSString();
}
/**
 *	Answer the string to display as the label for the source specification field
 */
protected String getSourceLabel() {
	return DataTransferMessages.getString("FileImport.sourceTitle"); //$NON-NLS-1$
}
/**
 * Returns a collection of the currently-specified resource types,
 * or <code>null</code> to indicate that all types should be imported.
 */
protected List getTypesToImport() {
	if (importAllResourcesRadio.getSelection())
		return null;
					
	List result = new ArrayList();
	StringTokenizer tokenizer = new StringTokenizer(typesToImportField.getText(),TYPE_DELIMITER);
	
	while (tokenizer.hasMoreTokens()) {
		String currentExtension = tokenizer.nextToken().trim();
		if (!currentExtension.equals(""))//$NON-NLS-1$
			result.add(currentExtension);
	}
		
	return result;
}
/**
 * Returns an array of the currently-specified resource types,
 * or <code>null</code> to indicate that all types should be imported.
 */
protected String[] getTypesToImportArray() {
	List typesToImport = getTypesToImport();
	if (typesToImport == null)
		return null;
		
	String result[] = new String[typesToImport.size()];
	typesToImport.toArray(result);

	return result;
}
/**
 *	Open a selection dialog and note the selections
 */
protected void handleDetailsButtonPressed() {
	FileSystemElement rootElement = getFileSystemTree();
	
	if (rootElement != null) {
		List newSelections = queryResourcesToImport(rootElement);
		
		if (newSelections != null) {
			selectedResources = newSelections;
			displaySelectedCount(selectedResources.size());
		}
	}
}
/**
 *	Handle all events and enablements for widgets in this dialog
 */
public void handleEvent(Event e) {
	Widget source = e.widget;

	if (source == sourceNameField)
		resetSelection();
	else if (source == sourceBrowseButton)
		handleSourceBrowseButtonPressed();
	else if (source == importAllResourcesRadio)
		resetSelection();
	else if (source == importTypedResourcesRadio) {
		resetSelection();
		typesToImportField.setFocus();
	} else if (source == detailsButton)
		handleDetailsButtonPressed();
	else if (source == typesToImportField)
		resetSelection();
	else if (source == typesToImportEditButton)
		handleTypesEditButtonPressed();

	super.handleEvent(e);
}
/**
 *	Open an appropriate source browser so that the user can specify a source
 *	to import from
 */
protected void handleSourceBrowseButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(sourceNameField.getShell(),SWT.SAVE);
	dialog.setMessage(DataTransferMessages.getString("FileImport.selectSource")); //$NON-NLS-1$
	dialog.setFilterPath(getSourceDirectoryName());
	
	String selectedDirectory = dialog.open();
	if (selectedDirectory != null) {
		if (!selectedDirectory.equals(getSourceDirectoryName())) {
			resetSelection();
			sourceNameField.setText(selectedDirectory);
		}
	}
}
/**
 *	Open a registered type selection dialog and note the selections
 *	in self's types-to-export field
 */
protected void handleTypesEditButtonPressed() {
	IFileEditorMapping editorMappings[] =
		PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();

	List selectedTypes = getTypesToImport();
	List initialSelections = new ArrayList();
	for (int i = 0; i < editorMappings.length; i++) {
		IFileEditorMapping mapping = editorMappings[i];
		if (selectedTypes.contains(mapping.getExtension()))
			initialSelections.add(mapping);
	}

	ListSelectionDialog dialog =
		new ListSelectionDialog(
			getContainer().getShell(),
			editorMappings,
			FileEditorMappingContentProvider.INSTANCE,
			FileEditorMappingLabelProvider.INSTANCE,
			DataTransferMessages.getString("FileImport.selectTypes")); //$NON-NLS-1$

	dialog.setInitialSelections(initialSelections.toArray());
	dialog.setTitle(DataTransferMessages.getString("FileImport.typeSelectionTitle")); //$NON-NLS-1$
	dialog.open();

	Object[] newSelectedTypes = dialog.getResult();
	if (newSelectedTypes != null) {					// ie.- did not press Cancel
		List result = new ArrayList(newSelectedTypes.length);
		for (int i = 0; i < newSelectedTypes.length; i++)
			result.add(((IFileEditorMapping)newSelectedTypes[i]).getExtension());
		setTypesToImport(result);
	}
}
/**
 *	Recursively import all resources starting at the user-specified source location.
 *	Answer a boolean indicating success.
 */
protected boolean importAllResources() {
	return executeImportOperation(
		new ImportOperation(
			getContainerFullPath(),
			getSourceDirectory(),
			FileSystemStructureProvider.INSTANCE,
			this));
}
/**
 *  Import the resources with extensions as specified by the user
 */
protected boolean importResources(List fileSystemObjects) {
	return executeImportOperation(
		new ImportOperation(
			getContainerFullPath(),
			getSourceDirectory(),
			FileSystemStructureProvider.INSTANCE,
			this,
			fileSystemObjects));
}
/**
 * Initializes the specified operation appropriately.
 */
protected void initializeOperation(ImportOperation op) {
	op.setCreateContainerStructure(createContainerStructureCheckbox.getSelection());
	op.setOverwriteResources(overwriteExistingResourcesCheckbox.getSelection());
}
/**
 * Opens a resource selection dialog with the passed root as input, and returns
 * a collection with the resources that were subsequently specified for import
 * by the user, or <code>null</code> if the dialog was canceled.
 */
protected List queryResourcesToImport(FileSystemElement rootElement) {
	FileSelectionDialog dialog = new FileSelectionDialog(getContainer().getShell(), rootElement, DataTransferMessages.getString("FileImport.selectResources")); //$NON-NLS-1$
	dialog.setInitialSelections(selectedResources.toArray());
	dialog.setExpandAllOnOpen(true);
	dialog.open();
	if (dialog.getResult() == null)
		return null;
	return Arrays.asList(dialog.getResult());

}
/**
 *	Reset the selected resources collection and update the ui appropriately
 */
protected void resetSelection() {
	detailsDescriptionLabel.setText(DataTransferMessages.getString("DataTransfer.allFiles")); //$NON-NLS-1$
	selectedResources = null;
	root = null;
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
			return;		// ie.- no values stored, so stop
		
		// set all/specific types radios and related enablements
		boolean importAll = settings.getBoolean(STORE_IMPORT_ALL_RESOURCES_ID);
		importAllResourcesRadio.setSelection(importAll);
		importTypedResourcesRadio.setSelection(!importAll);

		// set filenames history
		sourceNameField.setText(sourceNames[0]);
		for (int i = 0; i < sourceNames.length; i++)
			sourceNameField.add(sourceNames[i]);

		// set selected types
		String[] selectedTypes = settings.getArray(STORE_SELECTED_TYPES_ID);
		if (selectedTypes.length > 0)
			typesToImportField.setText(selectedTypes[0]);
		for (int i = 0; i < selectedTypes.length; i++)
			typesToImportField.add(selectedTypes[i]);
			
		// radio buttons and checkboxes	
		overwriteExistingResourcesCheckbox.setSelection(
			settings.getBoolean(STORE_OVERWRITE_EXISTING_RESOURCES_ID));

		createContainerStructureCheckbox.setSelection(
			settings.getBoolean(STORE_CREATE_CONTAINER_STRUCTURE_ID));
		
	}
}
/**
 * 	Since Finish was pressed, write widget values to the dialog store so that they
 *	will persist into the next invocation of this wizard page
 */
protected void saveWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	if(settings != null) {
		// update source names history
		String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
		if (sourceNames == null)
			sourceNames = new String[0];

		sourceNames = addToHistory(sourceNames,getSourceDirectoryName());
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

		settings.put(
			STORE_CREATE_CONTAINER_STRUCTURE_ID,
			createContainerStructureCheckbox.getSelection());
	
	}
}
/**
 * Invokes a file selection operation using the specified file system and
 * structure provider.  If the user specifies files to be imported then
 * this selection is cached for later retrieval and is returned.
 */
protected FileSystemElement selectFiles(Object rootFileSystemObject,IImportStructureProvider structureProvider) {
	try {
		SelectFilesOperation op =
			new SelectFilesOperation(rootFileSystemObject,structureProvider);
		op.setDesiredExtensions(getTypesToImportArray());
		getContainer().run(true, true, op);
		root = op.getResult();
		setSelectedResources(new ArrayList());
		addToSelectedResources(root);
	} catch (InterruptedException e) {
		return null;
	} catch (InvocationTargetException e) {
		displayErrorDialog(e.getTargetException().getMessage());
		return null;
	}

	return root;
}
/**
 *	Respond to the user selecting/deselecting items in the
 *	extensions list
 */
public void selectionChanged(SelectionChangedEvent event) {
	if (importTypedResourcesRadio.getSelection())
		resetSelection();
}
/**
 *	Set self's import source root element
 */
protected void setRoot(FileSystemElement value) {
	root = value;
}
/**
 *	Set self's current collection of selected resources
 */
protected void setSelectedResources(List value) {
	selectedResources = value;
}
/**
 *	Populate self's import types field based upon the passed types collection
 */
protected void setTypesToImport(List types) {
	StringBuffer result = new StringBuffer();
	for (int i = 0; i < types.size(); ++i) {
		if (i != 0) {
			result.append(TYPE_DELIMITER);
			result.append(" ");//$NON-NLS-1$
		}
		result.append(types.get(i));
	}
	typesToImportField.setText(result.toString());
}
/**
 *	Set the enablements of self's widgets
 */
protected void updateWidgetEnablements() {
	typesToImportField.setEnabled(importTypedResourcesRadio.getSelection());
	typesToImportEditButton.setEnabled(importTypedResourcesRadio.getSelection());
}
/**
 *	Answer a boolean indicating whether self's source specification
 *	widgets currently all contain valid values.
 */
protected boolean validateSourceGroup() {
	return !getSourceDirectoryName().equals("");//$NON-NLS-1$
}
}
