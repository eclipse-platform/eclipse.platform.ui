package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Abstract superclass for a typical export wizard's main page.
 * <p>
 * Clients may subclass this page to inherit its common destination resource
 * selection facilities.
 * </p>
 * <p>
 * Subclasses must implement 
 * <ul>
 *   <li><code>createDestinationGroup</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may override
 * <ul>
 *   <li><code>allowNewContainerName</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 *   <li><code>internalSaveWidgetValues</code></li>
 *   <li><code>updateWidgetEnablements</code></li>
 * </ul>
 * </p>
 */
public abstract class WizardExportResourcesPage
	extends WizardDataTransferPage {
	private IStructuredSelection initialResourceSelection;
	private List selectedTypes = new ArrayList();

	// widgets
	private ResourceTreeAndListGroup resourceGroup;
	private Button typesToExportEditButton;

	// dialog store id constants
	private static final String STORE_SELECTED_TYPES_ID = "WizardFileSystemExportPage1.STORE_SELECTED_TYPES_ID."; //$NON-NLS-1$

	private final static String SELECT_TYPES_TITLE = WorkbenchMessages.getString("WizardTransferPage.selectTypes"); //$NON-NLS-1$
	private final static String SELECT_ALL_TITLE = WorkbenchMessages.getString("WizardTransferPage.selectAll"); //$NON-NLS-1$
	private final static String DESELECT_ALL_TITLE = WorkbenchMessages.getString("WizardTransferPage.deselectAll"); //$NON-NLS-1$

	/**
	 * Creates an export wizard page. If the current resource selection 
	 * is not empty then it will be used as the initial collection of resources
	 * selected for export.
	 *
	 * @param pageName the name of the page
	 * @param selection the current resource selection
	 */
	protected WizardExportResourcesPage(
		String pageName,
		IStructuredSelection selection) {
		super(pageName);
		this.initialResourceSelection = selection;
	}
	/**
	 * The <code>addToHierarchyToCheckedStore</code> implementation of this 
	 * <code>WizardDataTransferPage</code> method returns <code>false</code>. 
	 * Subclasses may override this method.
	 */
	protected boolean allowNewContainerName() {
		return false;
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
	protected Button createButton(
		Composite parent,
		int id,
		String label,
		boolean defaultButton) {
		// increment the number of columns in the button bar
		 ((GridLayout) parent.getLayout()).numColumns++;

		Button button = new Button(parent, SWT.PUSH);

		GridData buttonData = new GridData(GridData.FILL_HORIZONTAL);
		button.setLayoutData(buttonData);

		button.setData(new Integer(id));
		button.setText(label);
		button.setFont(parent.getFont());

		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
			button.setFocus();
		}
		button.setFont(parent.getFont());
		setButtonLayoutData(button);
		return button;
	}
	/**
	 * Creates the buttons for selecting specific types or selecting all or none of the
	 * elements.
	 *
	 * @param parent the parent control
	 */
	protected final void createButtonsGroup(Composite parent) {

		Font font = parent.getFont();

		// top level group
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(
			new GridData(
				GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// types edit button
		Button selectTypesButton =
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
		selectTypesButton.setFont(font);
		setButtonLayoutData(selectTypesButton);

		Button selectButton =
			createButton(
				buttonComposite,
				IDialogConstants.SELECT_ALL_ID,
				SELECT_ALL_TITLE,
				false);

		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resourceGroup.setAllSelections(true);
			}
		};
		selectButton.addSelectionListener(listener);
		selectButton.setFont(font);
		setButtonLayoutData(selectButton);

		Button deselectButton =
			createButton(
				buttonComposite,
				IDialogConstants.DESELECT_ALL_ID,
				DESELECT_ALL_TITLE,
				false);

		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resourceGroup.setAllSelections(false);
			}
		};
		deselectButton.addSelectionListener(listener);
		deselectButton.setFont(font);
		setButtonLayoutData(deselectButton);

	}
	/** (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(
			new GridData(
				GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		createResourcesGroup(composite);
		createButtonsGroup(composite);

		createDestinationGroup(composite);

		createOptionsGroup(composite);

		restoreResourceSpecificationWidgetValues(); // ie.- local
		restoreWidgetValues(); // ie.- subclass hook
		if (initialResourceSelection != null)
			setupBasedOnInitialSelections();

		updateWidgetEnablements();
		setPageComplete(determinePageCompletion());

		setControl(composite);
	}
	/**
	 * Creates the export destination specification visual components.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param parent the parent control
	 */
	protected abstract void createDestinationGroup(Composite parent);
	/**
	 * Creates the checkbox tree and list for selecting resources.
	 *
	 * @param parent the parent control
	 */
	protected final void createResourcesGroup(Composite parent) {

		//create the input element, which has the root resource
		//as its only child
		List input = new ArrayList();
		IProject[] projects =
			ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen())
				input.add(projects[i]);
		}

		int availableRows = Dialog.availableRows(parent);
		
		this.resourceGroup =
			new ResourceTreeAndListGroup(
				parent,
				input,
				getResourceProvider(IResource.FOLDER | IResource.PROJECT),
				new WorkbenchLabelProvider(),
				getResourceProvider(IResource.FILE),
				new WorkbenchLabelProvider(),
				SWT.NONE,
				availableRows > 50);

	}
	/*
	 * @see WizardDataTransferPage.getErrorDialogTitle()
	 */
	protected String getErrorDialogTitle() {
		return WorkbenchMessages.getString("WizardExportPage.errorDialogTitle");
	}
	/**
	 * Obsolete method. This was implemented to handle the case where ensureLocal()
	 * needed to be called but it doesn't use it any longer.
	 *
	 * @deprecated Only retained for backwards compatibility.
	 */
	protected boolean ensureResourcesLocal(List resources) {
		return true;
	}

	/**
	 * Returns a new subcollection containing only those resources which are not 
	 * local.
	 *
	 * @param originalList the original list of resources (element type: 
	 *   <code>IResource</code>)
	 * @return the new list of non-local resources (element type: 
	 *   <code>IResource</code>)
	 */
	protected List extractNonLocalResources(List originalList) {
		Vector result = new Vector(originalList.size());
		Iterator resourcesEnum = originalList.iterator();

		while (resourcesEnum.hasNext()) {
			IResource currentResource = (IResource) resourcesEnum.next();
			if (!currentResource.isLocal(IResource.DEPTH_ZERO))
				result.addElement(currentResource);
		}

		return result;
	}
	/**
	 * Returns a content provider for <code>IResource</code>s that returns 
	 * only children of the given resource type.
	 */
	private ITreeContentProvider getResourceProvider(final int resourceType) {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof IContainer) {
					IResource[] members = null;
					try {
						members = ((IContainer) o).members();
					} catch (CoreException e) {
						//just return an empty set of children
						return new Object[0];
					}

					//filter out the desired resource types
					ArrayList results = new ArrayList();
					for (int i = 0; i < members.length; i++) {
						//And the test bits with the resource types to see if they are what we want
						if ((members[i].getType() & resourceType) > 0) {
							results.add(members[i]);
						}
					}
					return results.toArray();
				} else {
					//input element case
					if (o instanceof ArrayList) {
						return ((ArrayList) o).toArray();
					} else {
						return new Object[0];
					}
				}
			}
		};
	}
	/**
	 * Returns this page's collection of currently-specified resources to be 
	 * exported. This is the primary resource selection facility accessor for 
	 * subclasses.
	 *
	 * @return a collection of resources currently selected 
	 * for export (element type: <code>IResource</code>)
	 */
	protected List getSelectedResources() {
		Iterator resourcesToExportIterator =
			this.getSelectedResourcesIterator();
		List resourcesToExport = new ArrayList();
		while (resourcesToExportIterator.hasNext())
			resourcesToExport.add(resourcesToExportIterator.next());
		return resourcesToExport;
	}
	/**
	 * Returns this page's collection of currently-specified resources to be 
	 * exported. This is the primary resource selection facility accessor for 
	 * subclasses.
	 *
	 * @return an iterator over the collection of resources currently selected 
	 * for export (element type: <code>IResource</code>). This will include
	 * white checked folders and individually checked files.
	 */
	protected Iterator getSelectedResourcesIterator() {
		return this.resourceGroup.getAllCheckedListItems().iterator();
	}
	/**
	 * Returns the resource extensions currently specified to be exported.
	 *
	 * @return the resource extensions currently specified to be exported (element 
	 *   type: <code>String</code>)
	 */
	protected List getTypesToExport() {

		return selectedTypes;
	}
	/**
	 * Returns this page's collection of currently-specified resources to be 
	 * exported. This returns both folders and files - for just the files use
	 * getSelectedResources.
	 *
	 * @return a collection of resources currently selected 
	 * for export (element type: <code>IResource</code>)
	 */
	protected List getWhiteCheckedResources() {

		return this.resourceGroup.getAllWhiteCheckedItems();
	}
	/**
	 * Queries the user for the types of resources to be exported and selects
	 * them in the checkbox group.
	 */
	protected void handleTypesEditButtonPressed() {
		Object[] newSelectedTypes = queryResourceTypesToExport();

		if (newSelectedTypes != null) { // ie.- did not press Cancel
			this.selectedTypes = new ArrayList(newSelectedTypes.length);
			for (int i = 0; i < newSelectedTypes.length; i++) {
				this.selectedTypes.add(newSelectedTypes[i]);
			}
			setupSelectionsBasedOnSelectedTypes();
		}

	}
	/**
	 * Returns whether the extension of the given resource name is an extension that
	 * has been specified for export by the user.
	 *
	 * @param resourceName the resource name
	 * @return <code>true</code> if the resource name is suitable for export based 
	 *   upon its extension
	 */
	protected boolean hasExportableExtension(String resourceName) {
		if (selectedTypes == null) // ie.- all extensions are acceptable
			return true;

		int separatorIndex = resourceName.lastIndexOf("."); //$NON-NLS-1$
		if (separatorIndex == -1)
			return false;

		String extension = resourceName.substring(separatorIndex + 1);

		Iterator enum = selectedTypes.iterator();
		while (enum.hasNext()) {
			if (extension.equalsIgnoreCase((String) enum.next()))
				return true;
		}

		return false;
	}
	/**
	 * Persists additional setting that are to be restored in the next instance of
	 * this page.
	 * <p> 
	 * The <code>WizardImportPage</code> implementation of this method does
	 * nothing. Subclasses may extend to persist additional settings.
	 * </p>
	 */
	protected void internalSaveWidgetValues() {
	}
	/**
	 * Queries the user for the resource types that are to be exported and returns
	 * these types as an array.
	 *
	 * @return the resource types selected for export (element type: 
	 *   <code>String</code>), or <code>null</code> if the user canceled the 
	 *   selection
	 */
	protected Object[] queryResourceTypesToExport() {

		TypeFilteringDialog dialog =
			new TypeFilteringDialog(
				getContainer().getShell(),
				getTypesToExport());

		dialog.open();

		return dialog.getResult();
	}
	/**
	 * Restores resource specification control settings that were persisted
	 * in the previous instance of this page. Subclasses wishing to restore
	 * persisted values for their controls may extend.
	 */
	protected void restoreResourceSpecificationWidgetValues() {
	}
	/**
	 * Persists resource specification control setting that are to be restored
	 * in the next instance of this page. Subclasses wishing to persist additional
	 * setting for their controls should extend hook method 
	 * <code>internalSaveWidgetValues</code>.
	 */
	protected void saveWidgetValues() {

		// allow subclasses to save values
		internalSaveWidgetValues();

	}
	/**
	 * Set the initial selections in the resource group.
	 */
	protected void setupBasedOnInitialSelections() {

		Iterator enum = this.initialResourceSelection.iterator();
		while (enum.hasNext()) {
			IResource currentResource = (IResource) enum.next();
			if (currentResource.getType() == IResource.FILE)
				this.resourceGroup.initialCheckListItem(currentResource);
			else
				this.resourceGroup.initialCheckTreeItem(currentResource);
		}
	}
	/**
	 * Update the tree to only select those elements that match the selected types
	 */
	private void setupSelectionsBasedOnSelectedTypes() {

		Runnable runnable = new Runnable() {
			public void run() {
				Map selectionMap = new Hashtable();
				//Only get the white selected ones
				Iterator resourceIterator =
					resourceGroup.getAllWhiteCheckedItems().iterator();
				while (resourceIterator.hasNext()) {
					//handle the files here - white checked containers require recursion
					IResource resource = (IResource) resourceIterator.next();
					if (resource.getType() == IResource.FILE) {
						if (hasExportableExtension(resource.getName())) {
							List resourceList = new ArrayList();
							IContainer parent = resource.getParent();
							if (selectionMap.containsKey(parent))
								resourceList = (List) selectionMap.get(parent);
							resourceList.add(resource);
							selectionMap.put(parent, resourceList);
						}
					} else
						setupSelectionsBasedOnSelectedTypes(
							selectionMap,
							(IContainer) resource);
				}
				resourceGroup.updateSelections(selectionMap);
			}
		};

		BusyIndicator.showWhile(getShell().getDisplay(), runnable);

	}
	/**
	* Set up the selection values for the resources and put them in the selectionMap.
	* If a resource is a file see if it matches one of the selected extensions. If not
	* then check the children.
	* @return a boolean if any children are selected
	*/
	private void setupSelectionsBasedOnSelectedTypes(
		Map selectionMap,
		IContainer parent) {

		List selections = new ArrayList();
		IResource[] resources;
		boolean hasFiles = false;

		try {
			resources = parent.members();
		} catch (CoreException exception) {
			//Just return if we can't get any info
			return;
		}

		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.getType() == IResource.FILE) {
				if (hasExportableExtension(resource.getName())) {
					hasFiles = true;
					selections.add(resource);
				}
			} else {
				setupSelectionsBasedOnSelectedTypes(
					selectionMap,
					(IContainer) resource);
			}
		}

		//Only add it to the list if there are files in this folder
		if (hasFiles)
			selectionMap.put(parent, selections);
	}

	/**
	 * Save any editors that the user wants to save before export.
	 * @return boolean if the save was successful.
	 */
	protected boolean saveDirtyEditors() {
		return (
			(Workbench) WorkbenchPlugin
				.getDefault()
				.getWorkbench())
				.saveAllEditors(
			true);
	}
}
