package org.eclipse.ui.wizards.datatransfer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.wizards.datatransfer.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

/**
 *	Page 1 of the base resource import-from-file-system Wizard
 */
public class WizardFileSystemResourceImportPage1
	extends WizardResourceImportPage
	implements Listener {
	private IWorkbench workbench;
	protected List selectedTypes = new ArrayList();
	// widgets
	protected ResourceTreeAndListGroup selectionGroup;
	protected Combo sourceNameField;
	protected Button overwriteExistingResourcesCheckbox;
	protected Button createContainerStructureCheckbox;
	protected Button sourceBrowseButton;
	protected Button selectTypesButton;
	protected Button selectAllButton;
	protected Button deselectAllButton;

	private final static int SIZING_SELECTION_WIDGET_WIDTH = 400;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 150;

	// dialog store id constants
	private final static String STORE_SOURCE_NAMES_ID =
		"WizardFileSystemImportPage1.STORE_SOURCE_NAMES_ID";
	private final static String STORE_OVERWRITE_EXISTING_RESOURCES_ID =
		"WizardFileSystemImportPage1.STORE_OVERWRITE_EXISTING_RESOURCES_ID";
	private final static String STORE_CREATE_CONTAINER_STRUCTURE_ID =
		"WizardFileSystemImportPage1.STORE_CREATE_CONTAINER_STRUCTURE_ID";

	private static final String SELECT_TYPES_TITLE = "Select Types...";
	private static final String SELECT_ALL_TITLE = "Select All";
	private static final String DESELECT_ALL_TITLE = "Deselect All";
	private static final String SELECT_SOURCE_MESSAGE =
		"Select the source directory.";
	private static final String SOURCE_EMPTY_MESSAGE = "Source must not be empty.";

	protected MinimizedFileSystemElement currentRoot;
/**
 *	Creates an instance of this class
 */
protected WizardFileSystemResourceImportPage1(String name, IWorkbench aWorkbench, IStructuredSelection selection) {
	super(name,selection);
	this.workbench = aWorkbench;
}
/**
 *	Creates an instance of this class
 *
 * @param aWorkbench IWorkbench
 * @param selection IStructuredSelection
 */
public WizardFileSystemResourceImportPage1(IWorkbench aWorkbench, IStructuredSelection selection) {
	this("fileSystemImportPage1", aWorkbench, selection);
	setTitle("File system");
	setDescription("Import resources from the local file system.");
}
/**
 * Creates a new button with the given id.
 * <p>
 * The <code>Dialog</code> implementation of this framework method
 * creates a standard push button, registers for selection events
 * including button presses and registers
 * default buttons with its shell.
 * The button id is stored as the buttons client data.
 * Note that the parent's layout is assumed to be a GridLayout and 
 * the number of columns in this layout is incremented.
 * Subclasses may override.
 * </p>
 *
 * @param parent the parent composite
 * @param id the id of the button (see
 *  <code>IDialogConstants.*_ID</code> constants 
 *  for standard dialog button ids)
 * @param label the label from the button
 * @param defaultButton <code>true</code> if the button is to be the
 *   default button, and <code>false</code> otherwise
 */
protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
	// increment the number of columns in the button bar
	((GridLayout)parent.getLayout()).numColumns++;

	Button button = new Button(parent, SWT.PUSH);

	GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH + 2);
	button.setLayoutData(data);
	
	button.setData(new Integer(id));
	button.setText(label);
	
	if (defaultButton) {
		Shell shell = parent.getShell();
		if (shell != null) {
			shell.setDefaultButton(button);
		}
		button.setFocus();
	}
	button.setFont(parent.getFont());
	return button;
}
/**
 * Creates the buttons for selecting specific types or selecting all or none of the
 * elements.
 *
 * @param parent the parent control
 */
protected final void createButtonsGroup(Composite parent) {
	// top level group
	Composite buttonComposite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	buttonComposite.setLayout(layout);
	buttonComposite.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

	// types edit button
	selectTypesButton =
		createButton(
			buttonComposite,
			IDialogConstants.SELECT_TYPES_ID,
			SELECT_TYPES_TITLE,
			false);
	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleTypesEditButtonPressed();
		}
	};
	selectTypesButton.addSelectionListener(listener);

	selectAllButton =
		createButton(
			buttonComposite,
			IDialogConstants.SELECT_ALL_ID,
			SELECT_ALL_TITLE,
			false);

	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			selectionGroup.setAllSelections(true);
		}
	};
	selectAllButton.addSelectionListener(listener);

	deselectAllButton =
		createButton(
			buttonComposite,
			IDialogConstants.DESELECT_ALL_ID,
			DESELECT_ALL_TITLE,
			false);

	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			selectionGroup.setAllSelections(false);
		}
	};
	deselectAllButton.addSelectionListener(listener);

}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {

	super.createControl(parent);
	validateSourceGroup();
}
/**
 *	Create the import source selection widget
 */
protected void createFileSelectionGroup(Composite parent) {

	//Just create with a dummy root.
	this.selectionGroup =
		new ResourceTreeAndListGroup(
			parent,
			new FileSystemElement("Dummy", null, true),
			getFolderProvider(),
			new WorkbenchLabelProvider(),
			getFileProvider(),
			new WorkbenchLabelProvider(),
			SWT.NONE,
			SIZING_SELECTION_WIDGET_WIDTH,
			SIZING_SELECTION_WIDGET_HEIGHT);

	ICheckStateListener listener = new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			setPageComplete(selectionGroup.getCheckedElementCount() > 0);
		}
	};

	WorkbenchViewerSorter sorter = new WorkbenchViewerSorter();
	this.selectionGroup.setTreeSorter(sorter);
	this.selectionGroup.setListSorter(sorter);
	this.selectionGroup.addCheckStateListener(listener);

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

	// create containers checkbox
	createContainerStructureCheckbox = new Button(optionsGroup,SWT.CHECK);
	createContainerStructureCheckbox.setText("Create complete folder structure");
}
/**
 *	Create the group for creating the root directory
 */
protected void createRootDirectoryGroup(Composite parent) {
	Composite sourceContainerGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	sourceContainerGroup.setLayout(layout);
	sourceContainerGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

	new Label(sourceContainerGroup, SWT.NONE).setText(getSourceLabel());

	// source name entry field
	sourceNameField = new Combo(sourceContainerGroup, SWT.BORDER);
	sourceNameField.addListener(SWT.Modify, this);
	sourceNameField.addListener(SWT.Selection, this);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	sourceNameField.setLayoutData(data);

	// source browse button
	sourceBrowseButton = new Button(sourceContainerGroup, SWT.PUSH);
	sourceBrowseButton.setText("Browse...");
	sourceBrowseButton.addListener(SWT.Selection, this);
	sourceBrowseButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

	sourceNameField.setFocus();
}
/**
 * Creates and returns a <code>FileSystemElement</code> if the specified
 * file system object merits one.  The criteria for this are:
 * Also create the children.
 */
protected MinimizedFileSystemElement createRootElement(
	Object fileSystemObject,
	IImportStructureProvider provider) {
	boolean isContainer = provider.isFolder(fileSystemObject);
	String elementLabel = provider.getLabel(fileSystemObject);

	MinimizedFileSystemElement result =
		new MinimizedFileSystemElement(elementLabel, null, isContainer);
	result.setFileSystemObject(fileSystemObject);

	//Get the files for the element so as to build the first level
	result.getFiles(provider);		

	return result;
}
/**
 *	Create the import source specification widgets
 */
protected void createSourceGroup(Composite parent) {

	createRootDirectoryGroup(parent);
	createFileSelectionGroup(parent);
	createButtonsGroup(parent);
}
/**
 * Enable or disable the button group.
 */
private void enableButtonGroup(boolean enable) {
	selectTypesButton.setEnabled(enable);
	selectAllButton.setEnabled(enable);
	deselectAllButton.setEnabled(enable);
}
/**
 *	Answer a boolean indicating whether the specified source currently exists
 *	and is valid
 */
protected boolean ensureSourceIsValid() {
	if (new File(getSourceDirectoryName()).isDirectory())
		return true;

	displayErrorDialog("Source directory is not valid or has not been specified.");
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
			"Import Problems",
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
 *
 * @return boolean
 */
public boolean finish() {
	if (!ensureSourceIsValid())
		return false;

   	Iterator resourcesEnum = getSelectedResourcesIterator();
	List fileSystemObjects = new ArrayList();
	while (resourcesEnum.hasNext()) {
		fileSystemObjects.add(
			((FileSystemElement) resourcesEnum.next()).getFileSystemObject());
	}

	return importResources(fileSystemObjects);
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
				return element.getFiles(FileSystemStructureProvider.INSTANCE).getChildren(
					element);
			}
			return new Object[0];
		}
	};
}
/**
 *	Answer the root FileSystemElement that represents the contents of
 *	the currently-specified source.  If this FileSystemElement is not
 *	currently defined then create and return it.
 */
protected MinimizedFileSystemElement getFileSystemTree() {

	File sourceDirectory = getSourceDirectory();
	if (sourceDirectory == null)
		return null;

	return selectFiles(sourceDirectory, FileSystemStructureProvider.INSTANCE);
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
				return element.getFolders(FileSystemStructureProvider.INSTANCE).getChildren(
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
 *	Answer the reciever current collection of selected resources in Iterator form.
 */
protected Iterator getSelectedResourcesIterator() {
	return this.selectionGroup.getAllCheckedListItems();
}
/**
 * Returns a File object representing the currently-named source directory iff
 * it exists as a valid directory, or <code>null</code> otherwise.
 */
protected File getSourceDirectory() {
	File sourceDirectory = new File(getSourceDirectoryName());
	if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
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
	return "Directory:";
}
/**
 * Returns a collection of the currently-specified resource types for
 * use by the type selection dialog.
 */
protected List getTypesToImport() {

	return selectedTypes;
}
/**
 *	Handle all events and enablements for widgets in this dialog
 *
 * @param event Event
 */
public void handleEvent(Event event) {
	if (event.widget == sourceBrowseButton)
		handleSourceBrowseButtonPressed();

	super.handleEvent(event);
}
/**
 *	Open an appropriate source browser so that the user can specify a source
 *	to import from
 */
protected void handleSourceBrowseButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(sourceNameField.getShell(),SWT.SAVE);
	dialog.setMessage(SELECT_SOURCE_MESSAGE);
	dialog.setFilterPath(getSourceDirectoryName());
	
	String selectedDirectory = dialog.open();
	if (selectedDirectory != null) {
		if (!selectedDirectory.equals(getSourceDirectoryName())) {
			setErrorMessage(null);
			setSourceDirectory(selectedDirectory);
		}
	}
}
/**
 *	Open a registered type selection dialog and note the selections
 *	in the receivers types-to-export field
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
			"Select the types to import.");

	dialog.setInitialSelections(initialSelections.toArray());
	dialog.setTitle("Type Selection");
	dialog.open();

	Object[] newSelectedTypes = dialog.getResult();
	if (newSelectedTypes != null) { // ie.- did not press Cancel
		this.selectedTypes = new ArrayList(newSelectedTypes.length);
		for (int i = 0; i < newSelectedTypes.length; i++)
			this.selectedTypes.add(
				((IFileEditorMapping) newSelectedTypes[i]).getExtension());
	}

	setupSelectionsBasedOnSelectedTypes();
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
 * Returns whether the extension provided is an extension that
 * has been specified for export by the user.
 *
 * @param extension the resource name
 * @return <code>true</code> if the resource name is suitable for export based 
 *   upon its extension
 */
protected boolean isExportableExtension(String extension) {
	if (selectedTypes == null)	// ie.- all extensions are acceptable
		return true;

	Iterator enum = selectedTypes.iterator();
	while (enum.hasNext()) {
		if (extension.equalsIgnoreCase((String)enum.next()))
			return true;
	}
	
	return false;
}
/**
 *	Repopulate the view based on the currently entered directory.
 */
protected void resetSelection() {

	this.currentRoot = getFileSystemTree();
	this.selectionGroup.setRoot(this.currentRoot);
	

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
		
				// set filenames history
		this.sourceNameField.setText(sourceNames[0]);
		for (int i = 0; i < sourceNames.length; i++)
			sourceNameField.add(sourceNames[i]);

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
	if (settings != null) {
		// update source names history
		String[] sourceNames = settings.getArray(STORE_SOURCE_NAMES_ID);
		if (sourceNames == null)
			sourceNames = new String[0];

		sourceNames = addToHistory(sourceNames, getSourceDirectoryName());
		settings.put(STORE_SOURCE_NAMES_ID, sourceNames);

		// radio buttons and checkboxes	
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
protected MinimizedFileSystemElement selectFiles(
	final Object rootFileSystemObject,
	final IImportStructureProvider structureProvider) {

	final MinimizedFileSystemElement[] results = new MinimizedFileSystemElement[1];

	BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
		public void run() {
			//Create the root element from the supplied file system object
			results[0] = createRootElement(rootFileSystemObject, structureProvider);
		}
	});

	return results[0];
}
/**
 * Set the source directory of the import
 */
private void setSourceDirectory(String path) {

	this.sourceNameField.setText(path);

	if (path.length() > 0)
		resetSelection();
}
/**
 * Update the tree to only select those elements that match the selected types
 */
private void setupSelectionsBasedOnSelectedTypes() {

	BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
		public void run() {

			Map selectionMap = new Hashtable();

			Iterator filesList = selectionGroup.getAllCheckedListItems();

			while (filesList.hasNext()) {
				MinimizedFileSystemElement file = (MinimizedFileSystemElement) filesList.next();
				if (isExportableExtension(file.getFileNameExtension())) {
					List elements = new ArrayList();
					FileSystemElement parent = file.getParent();
					if (selectionMap.containsKey(parent))
						elements = (List) selectionMap.get(parent);
					elements.add(file);
					selectionMap.put(parent, elements);
				}
			}
			selectionGroup.updateSelections(selectionMap);

		}
	});

}
/* (non-Javadoc)
 * Method declared on IDialogPage. Set the selection up when it becomes visible.
 */
public void setVisible(boolean visible) {
	super.setVisible(visible);
	resetSelection();
}
/**
 * Determine if the page is complete and update the page appropriately. 
 */
protected void updatePageCompletion() {

	//Added locally as it is referred to in an inner class 
	super.updatePageCompletion();
}
/**
 *	Answer a boolean indicating whether self's source specification
 *	widgets currently all contain valid values.
 */
protected boolean validateSourceGroup() {
	String sourceValue = getSourceDirectoryName();
	if (sourceValue.length() == 0) {
		setMessage(SOURCE_EMPTY_MESSAGE);
		enableButtonGroup(false);
		return false;
	} else {
		enableButtonGroup(true);
		return true;
	}
}
}
