/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

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
 * @deprecated use WizardExportResourcePage
 */
public abstract class WizardExportPage extends WizardDataTransferPage {
    private IStructuredSelection currentResourceSelection;

    private List selectedResources;

    private List selectedTypes;

    private boolean exportCurrentSelection = false;

    private boolean exportAllResourcesPreSet = false;

    // widgets
    private Combo typesToExportField;

    private Button typesToExportEditButton;

    private Button exportAllTypesRadio;

    private Button exportSpecifiedTypesRadio;

    private Button resourceDetailsButton;

    private Label resourceDetailsDescription;

    private Text resourceNameField;

    private Button resourceBrowseButton;

    // initial value stores
    private boolean initialExportAllTypesValue = true;

    private String initialExportFieldValue;

    private String initialTypesFieldValue;

    // constants
    private static final String CURRENT_SELECTION = "<current selection>";//$NON-NLS-1$

    private static final String TYPE_DELIMITER = ",";//$NON-NLS-1$

    // dialog store id constants
    private static final String STORE_SELECTED_TYPES_ID = "WizardFileSystemExportPage1.STORE_SELECTED_TYPES_ID.";//$NON-NLS-1$

    private static final String STORE_EXPORT_ALL_RESOURCES_ID = "WizardFileSystemExportPage1.STORE_EXPORT_ALL_RESOURCES_ID.";//$NON-NLS-1$

    /**
     * Creates an export wizard page. If the current resource selection 
     * is not empty then it will be used as the initial collection of resources
     * selected for export.
     *
     * @param pageName the name of the page
     * @param selection the current resource selection
     */
    protected WizardExportPage(String pageName, IStructuredSelection selection) {
        super(pageName);
        this.currentResourceSelection = selection;
    }

    /**
     * The <code>WizardExportPage</code> implementation of this 
     * <code>WizardDataTransferPage</code> method returns <code>false</code>. 
     * Subclasses may override this method.
     */
    protected boolean allowNewContainerName() {
        return false;
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));

        createBoldLabel(composite, IDEWorkbenchMessages.WizardExportPage_whatLabel);
        createSourceGroup(composite);

        createSpacer(composite);

        createBoldLabel(composite, IDEWorkbenchMessages.WizardExportPage_whereLabel);
        createDestinationGroup(composite);

        createSpacer(composite);

        createBoldLabel(composite, IDEWorkbenchMessages.WizardExportPage_options);
        createOptionsGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook
        if (currentResourceSelection != null) {
			setupBasedOnInitialSelections();
		}

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
     * Creates the export source resource specification controls.
     *
     * @param parent the parent control
     */
    protected final void createSourceGroup(Composite parent) {
        // top level group
        Composite sourceGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        sourceGroup.setLayout(layout);
        sourceGroup.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));

        // resource label
        new Label(sourceGroup, SWT.NONE).setText(IDEWorkbenchMessages.WizardExportPage_folder);

        // resource name entry field
        resourceNameField = new Text(sourceGroup, SWT.SINGLE | SWT.BORDER);
        resourceNameField.addListener(SWT.KeyDown, this);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        resourceNameField.setLayoutData(data);

        // resource browse button
        resourceBrowseButton = new Button(sourceGroup, SWT.PUSH);
        resourceBrowseButton.setText(IDEWorkbenchMessages.WizardExportPage_browse);
        resourceBrowseButton.addListener(SWT.Selection, this);
        resourceBrowseButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

        // export all types radio	
        exportAllTypesRadio = new Button(sourceGroup, SWT.RADIO);
        exportAllTypesRadio.setText(IDEWorkbenchMessages.WizardExportPage_allTypes);
        exportAllTypesRadio.addListener(SWT.Selection, this);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 3;
        exportAllTypesRadio.setLayoutData(data);

        // export specific types radio
        exportSpecifiedTypesRadio = new Button(sourceGroup, SWT.RADIO);
        exportSpecifiedTypesRadio.setText(IDEWorkbenchMessages.WizardExportPage_specificTypes);
        exportSpecifiedTypesRadio.addListener(SWT.Selection, this);

        // types combo
        typesToExportField = new Combo(sourceGroup, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        typesToExportField.setLayoutData(data);
        typesToExportField.addListener(SWT.Modify, this);

        // types edit button
        typesToExportEditButton = new Button(sourceGroup, SWT.PUSH);
        typesToExportEditButton.setText(IDEWorkbenchMessages.WizardExportPage_edit);
        typesToExportEditButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
                        | GridData.VERTICAL_ALIGN_END));
        typesToExportEditButton.addListener(SWT.Selection, this);

        // details button
        resourceDetailsButton = new Button(sourceGroup, SWT.PUSH);
        resourceDetailsButton.setText(IDEWorkbenchMessages.WizardExportPage_details);
        resourceDetailsButton.addListener(SWT.Selection, this);

        // details label
        resourceDetailsDescription = new Label(sourceGroup, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.horizontalSpan = 2;
        resourceDetailsDescription.setLayoutData(data);

        // initial setup
        resetSelectedResources();
        exportAllTypesRadio.setSelection(initialExportAllTypesValue);
        exportSpecifiedTypesRadio.setSelection(!initialExportAllTypesValue);
        typesToExportField.setEnabled(!initialExportAllTypesValue);
        typesToExportEditButton.setEnabled(!initialExportAllTypesValue);

        if (initialExportFieldValue != null) {
			resourceNameField.setText(initialExportFieldValue);
		}
        if (initialTypesFieldValue != null) {
			typesToExportField.setText(initialTypesFieldValue);
		}
    }

    /**
     * Display an error dialog with the specified message.
     *
     * @param message the error message
     */
    protected void displayErrorDialog(String message) {
        MessageDialog.open(MessageDialog.ERROR, getContainer().getShell(), IDEWorkbenchMessages.WizardExportPage_errorDialogTitle, message, SWT.SHEET);
    }

    /**
     * Displays a description message that indicates a selection of resources
     * of the specified size.
     *
     * @param selectedResourceCount the resource selection size to display
     */
    protected void displayResourcesSelectedCount(int selectedResourceCount) {
        if (selectedResourceCount == 1) {
			resourceDetailsDescription.setText(IDEWorkbenchMessages.WizardExportPage_oneResourceSelected);
		} else {
			resourceDetailsDescription
                    .setText(NLS.bind(IDEWorkbenchMessages.WizardExportPage_resourceCountMessage, new Integer(selectedResourceCount)));
		}
    }

    /**
     * Obsolete method. This was implemented to handle the case where ensureLocal()
     * needed to be called but it doesn't use it any longer.
     *
     * @param resources the list of resources to ensure locality for
     * @return <code>true</code> for successful completion
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
            if (!currentResource.isLocal(IResource.DEPTH_ZERO)) {
				result.addElement(currentResource);
			}
        }

        return result;
    }

    /**
     * Returns the current selection value of the "Export all types" radio,
     * or its set initial value if it does not exist yet.
     *
     * @return the "Export All Types" radio's current value or anticipated initial
     *   value
     */
    public boolean getExportAllTypesValue() {
        if (exportAllTypesRadio == null) {
			return initialExportAllTypesValue;
		}

        return exportAllTypesRadio.getSelection();
    }

    /**
     * Returns the current contents of the resource name entry field,
     * or its set initial value if it does not exist yet (which could
     * be <code>null</code>).
     *
     * @return the resource name field's current value or anticipated initial value,
     *   or <code>null</code>
     */
    public String getResourceFieldValue() {
        if (resourceNameField == null) {
			return initialExportFieldValue;
		}

        return resourceNameField.getText();
    }

    /**
     * Return the path for the resource field.
     * @return org.eclipse.core.runtime.IPath
     */
    protected IPath getResourcePath() {
        return getPathFromText(this.resourceNameField);
    }

    /**
     * Returns this page's collection of currently-specified resources to be 
     * exported. This is the primary resource selection facility accessor for 
     * subclasses.
     *
     * @return the collection of resources currently selected for export (element 
     *   type: <code>IResource</code>)
     */
    protected List getSelectedResources() {
        if (selectedResources == null) {
            IResource sourceResource = getSourceResource();

            if (sourceResource != null) {
				selectAppropriateResources(sourceResource);
			}
        }

        return selectedResources;
    }

    /**
     * Returns the resource object specified in the resource name entry field,
     * or <code>null</code> if such a resource does not exist in the workbench.
     *
     * @return the resource specified in the resource name entry field, or 
     *   <code>null</code>
     */
    protected IResource getSourceResource() {
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
        //make the path absolute to allow for optional leading slash
        IPath testPath = getResourcePath();

        IStatus result = workspace.validatePath(testPath.toString(),
                IResource.ROOT | IResource.PROJECT | IResource.FOLDER
                        | IResource.FILE);

        if (result.isOK() && workspace.getRoot().exists(testPath)) {
			return workspace.getRoot().findMember(testPath);
		}

        return null;
    }

    /**
     * Returns the current contents of the types entry field, or its set
     * initial value if it does not exist yet (which could be <code>null</code>).
     *
     * @return the types entry field's current value or anticipated initial value,
     *   or <code>null</code>
     */
    public String getTypesFieldValue() {
        if (typesToExportField == null) {
			return initialTypesFieldValue;
		}

        return typesToExportField.getText();
    }

    /**
     * Returns the resource extensions currently specified to be exported.
     *
     * @return the resource extensions currently specified to be exported (element 
     *   type: <code>String</code>)
     */
    protected List getTypesToExport() {
        List result = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(typesToExportField
                .getText(), TYPE_DELIMITER);

        while (tokenizer.hasMoreTokens()) {
            String currentExtension = tokenizer.nextToken().trim();
            if (!currentExtension.equals("")) { //$NON-NLS-1$
				result.add(currentExtension);
			}
        }

        return result;
    }

    /**
     * The <code>WizardExportPage</code> implementation of this 
     * <code>Listener</code> method handles all events and enablements for controls
     * on this page. Subclasses may extend.
     */
    public void handleEvent(Event event) {
        Widget source = event.widget;

        if (source == exportAllTypesRadio || source == typesToExportField
                || source == resourceNameField) {
			resetSelectedResources();
		} else if (source == exportSpecifiedTypesRadio) {
            resetSelectedResources();
            typesToExportField.setFocus();
        } else if (source == resourceDetailsButton) {
			handleResourceDetailsButtonPressed();
		} else if (source == resourceBrowseButton) {
			handleResourceBrowseButtonPressed();
		} else if (source == typesToExportEditButton) {
			handleTypesEditButtonPressed();
		}

        setPageComplete(determinePageCompletion());
        updateWidgetEnablements();
    }

    /**
     * Opens a container selection dialog and displays the user's subsequent
     * container selection in this page's resource name field.
     */
    protected void handleResourceBrowseButtonPressed() {
        IResource currentFolder = getSourceResource();
        if (currentFolder != null && currentFolder.getType() == IResource.FILE) {
			currentFolder = currentFolder.getParent();
		}

        IPath containerPath = queryForContainer((IContainer) currentFolder,
                IDEWorkbenchMessages.WizardExportPage_selectResourcesToExport);
        if (containerPath != null) { // null means user cancelled
            String relativePath = containerPath.makeRelative().toString();
            if (!relativePath.toString().equals(resourceNameField.getText())) {
                resetSelectedResources();
                resourceNameField.setText(relativePath);
            }
        }
    }

    /**
     * Opens a resource selection dialog and records the user's subsequent
     * resource selections.
     */
    protected void handleResourceDetailsButtonPressed() {
        IAdaptable source = getSourceResource();

        if (source == null) {
			source = ResourcesPlugin.getWorkspace().getRoot();
		}

        selectAppropriateResources(source);

        if (source instanceof IFile) {
            source = ((IFile) source).getParent();
            setResourceToDisplay((IResource) source);
        }

        Object[] newlySelectedResources = queryIndividualResourcesToExport(source);

        if (newlySelectedResources != null) {
            selectedResources = Arrays.asList(newlySelectedResources);
            displayResourcesSelectedCount(selectedResources.size());
        }
    }

    /**
     * Queries the user for the types of resources to be exported and
     * displays these types in this page's "Types to export" field.
     */
    protected void handleTypesEditButtonPressed() {
        Object[] newSelectedTypes = queryResourceTypesToExport();

        if (newSelectedTypes != null) { // ie.- did not press Cancel
            List result = new ArrayList(newSelectedTypes.length);
            for (int i = 0; i < newSelectedTypes.length; i++) {
				result.add(((IFileEditorMapping) newSelectedTypes[i])
                        .getExtension());
			}
            setTypesToExport(result);
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
        if (selectedTypes == null) {
			return true;
		}

        int separatorIndex = resourceName.lastIndexOf(".");//$NON-NLS-1$
        if (separatorIndex == -1) {
			return false;
		}

        String extension = resourceName.substring(separatorIndex + 1);

        Iterator it = selectedTypes.iterator();
        while (it.hasNext()) {
            if (extension.equalsIgnoreCase((String) it.next())) {
				return true;
			}
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
     * Queries the user for the individual resources that are to be exported
     * and returns these resources as a collection.
     * 
     * @param rootResource the resource to use as the root of the selection query
     * @return the resources selected for export (element type: 
     *   <code>IResource</code>), or <code>null</code> if the user canceled the 
     *   selection
     */
    protected Object[] queryIndividualResourcesToExport(IAdaptable rootResource) {
        ResourceSelectionDialog dialog = new ResourceSelectionDialog(
                getContainer().getShell(), rootResource, IDEWorkbenchMessages.WizardExportPage_selectResourcesTitle);
        dialog.setInitialSelections(selectedResources
                .toArray(new Object[selectedResources.size()]));
        dialog.open();
        return dialog.getResult();
    }

    /**
     * Queries the user for the resource types that are to be exported and returns
     * these types as a collection.
     *
     * @return the resource types selected for export (element type: 
     *   <code>String</code>), or <code>null</code> if the user canceled the 
     *   selection
     */
    protected Object[] queryResourceTypesToExport() {
        IFileEditorMapping editorMappings[] = PlatformUI.getWorkbench()
                .getEditorRegistry().getFileEditorMappings();

        int mappingsSize = editorMappings.length;
        List selectedTypes = getTypesToExport();
        List initialSelections = new ArrayList(selectedTypes.size());

        for (int i = 0; i < mappingsSize; i++) {
            IFileEditorMapping currentMapping = editorMappings[i];
            if (selectedTypes.contains(currentMapping.getExtension())) {
				initialSelections.add(currentMapping);
			}
        }

        ListSelectionDialog dialog = new ListSelectionDialog(getContainer()
                .getShell(), editorMappings,
                ArrayContentProvider.getInstance(),
                FileEditorMappingLabelProvider.INSTANCE, IDEWorkbenchMessages.WizardExportPage_selectionDialogMessage){
        	protected int getShellStyle() {
        		return super.getShellStyle() | SWT.SHEET;
        	}
        };

        dialog.setTitle(IDEWorkbenchMessages.WizardExportPage_resourceTypeDialog);
        dialog.open();

        return dialog.getResult();
    }

    /**
     * Resets this page's selected resources collection and updates its controls
     * accordingly.
     */
    protected void resetSelectedResources() {
        resourceDetailsDescription.setText(IDEWorkbenchMessages.WizardExportPage_detailsMessage);
        selectedResources = null;

        if (exportCurrentSelection) {
            exportCurrentSelection = false;

            if (resourceNameField.getText().length() > CURRENT_SELECTION
                    .length()) {
				resourceNameField.setText(resourceNameField.getText()
                        .substring(CURRENT_SELECTION.length()));
			} else {
				resourceNameField.setText("");//$NON-NLS-1$
			}
        }
    }

    /**
     * Restores resource specification control settings that were persisted
     * in the previous instance of this page. Subclasses wishing to restore
     * persisted values for their controls may extend.
     */
    protected void restoreResourceSpecificationWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String pageName = getName();
            boolean exportAllResources = settings
                    .getBoolean(STORE_EXPORT_ALL_RESOURCES_ID + pageName);

            // restore all/typed radio values iff not already explicitly set
            if (!exportAllResourcesPreSet) {
                exportAllTypesRadio.setSelection(exportAllResources);
                exportSpecifiedTypesRadio.setSelection(!exportAllResources);
            }

            // restore selected types iff not explicitly already set
            if (initialTypesFieldValue == null) {
                String[] selectedTypes = settings
                        .getArray(STORE_SELECTED_TYPES_ID + pageName);
                if (selectedTypes != null) {
                    if (selectedTypes.length > 0) {
						typesToExportField.setText(selectedTypes[0]);
					}
                    for (int i = 0; i < selectedTypes.length; i++) {
						typesToExportField.add(selectedTypes[i]);
					}
                }
            }
        }
    }

    /**
     * Persists resource specification control setting that are to be restored
     * in the next instance of this page. Subclasses wishing to persist additional
     * setting for their controls should extend hook method 
     * <code>internalSaveWidgetValues</code>.
     */
    protected void saveWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String pageName = getName();

            // update specific types to export history
            String[] selectedTypesNames = settings
                    .getArray(STORE_SELECTED_TYPES_ID + pageName);
            if (selectedTypesNames == null) {
				selectedTypesNames = new String[0];
			}

            if (exportSpecifiedTypesRadio.getSelection()) {
                selectedTypesNames = addToHistory(selectedTypesNames,
                        typesToExportField.getText());
            }

            settings
                    .put(STORE_SELECTED_TYPES_ID + pageName, selectedTypesNames);

            // radio buttons
            settings.put(STORE_EXPORT_ALL_RESOURCES_ID + pageName,
                    exportAllTypesRadio.getSelection());
        }

        // allow subclasses to save values
        internalSaveWidgetValues();

    }

    /**
     * Records a container's recursive file descendents which have an extension
     * that has been specified for export by the user.
     *
     * @param resource the parent container
     */
    protected void selectAppropriateFolderContents(IContainer resource) {
        try {
            IResource[] members = resource.members();

            for (int i = 0; i < members.length; i++) {
                if (members[i].getType() == IResource.FILE) {
                    IFile currentFile = (IFile) members[i];
                    if (hasExportableExtension(currentFile.getFullPath()
                            .toString())) {
						selectedResources.add(currentFile);
					}
                }
                if (members[i].getType() == IResource.FOLDER) {
                    selectAppropriateFolderContents((IContainer) members[i]);
                }
            }
        } catch (CoreException e) {
            //don't show children if there are errors -- should at least log this
        }
    }

    /**
     * Records a resource's recursive descendents which are appropriate
     * for export based upon this page's current controls contents.
     *
     * @param resource the parent resource
     */
    protected void selectAppropriateResources(Object resource) {
        if (selectedResources == null) {

            if (exportSpecifiedTypesRadio.getSelection()) {
				selectedTypes = getTypesToExport();
			} else {
				selectedTypes = null; // sentinel for select all extensions
			}

            selectedResources = new ArrayList();
            if (resource instanceof IWorkspaceRoot) {
                IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
                for (int i = 0; i < projects.length; i++) {
                    selectAppropriateFolderContents(projects[i]);
                }
            } else if (resource instanceof IFile) {
                IFile file = (IFile) resource;
                if (hasExportableExtension(file.getFullPath().toString())) {
					selectedResources.add(file);
				}
            } else {
                selectAppropriateFolderContents((IContainer) resource);
            }
        }
    }

    /**
     * Sets the selection value of this page's "Export all types" radio, or stores
     * it for future use if this visual component does not exist yet.
     *
     * @param value new selection value
     */
    public void setExportAllTypesValue(boolean value) {
        if (exportAllTypesRadio == null) {
            initialExportAllTypesValue = value;
            exportAllResourcesPreSet = true;
        } else {
            exportAllTypesRadio.setSelection(value);
            exportSpecifiedTypesRadio.setSelection(!value);
        }
    }

    /**
     * Sets the value of this page's source resource field, or stores
     * it for future use if this visual component does not exist yet.
     *
     * @param value new value
     */
    public void setResourceFieldValue(String value) {
        if (resourceNameField == null) {
			initialExportFieldValue = value;
		} else {
			resourceNameField.setText(value);
		}
    }

    /**
     * Set the resource whos name we will display.
     * @param resource
     */
    protected void setResourceToDisplay(IResource resource) {
        setResourceFieldValue(resource.getFullPath().makeRelative().toString());
    }

    /**
     * Sets the value of this page's "Types to export" field, or stores
     * it for future use if this visual component does not exist yet.
     *
     * @param value new value
     */
    public void setTypesFieldValue(String value) {
        if (typesToExportField == null) {
			initialTypesFieldValue = value;
		} else {
			typesToExportField.setText(value);
		}
    }

    /**
     * Sets the value of this page's "Types to export" field based upon the
     * collection of extensions.
     *
     * @param typeStrings the collection of extensions to populate the "Types to
     *   export" field with (element type: <code>String</code>)
     */
    protected void setTypesToExport(List typeStrings) {
        StringBuffer result = new StringBuffer();
        Iterator typesEnum = typeStrings.iterator();

        while (typesEnum.hasNext()) {
            result.append(typesEnum.next());
            result.append(TYPE_DELIMITER);
            result.append(" ");//$NON-NLS-1$
        }

        typesToExportField.setText(result.toString());
    }

    /**
     * Populates the resource name field based upon the currently selected resources.
     */
    protected void setupBasedOnInitialSelections() {
        if (initialExportFieldValue != null) {
            // a source resource has been programatically specified, which overrides
            // the current workbench resource selection
            IResource specifiedSourceResource = getSourceResource();
            if (specifiedSourceResource == null) {
				currentResourceSelection = new StructuredSelection();
			} else {
				currentResourceSelection = new StructuredSelection(
                        specifiedSourceResource);
			}
        }

        if (currentResourceSelection.isEmpty()) {
			return; // no setup needed
		}

        List selections = new ArrayList();
        Iterator it = currentResourceSelection.iterator();
        while (it.hasNext()) {
            IResource currentResource = (IResource) it.next();
            // do not add inaccessible elements
            if (currentResource.isAccessible()) {
				selections.add(currentResource);
			}
        }

        if (selections.isEmpty()) {
			return; // setup not needed anymore
		}

        int selectedResourceCount = selections.size();
        if (selectedResourceCount == 1) {
            IResource resource = (IResource) selections.get(0);
            setResourceToDisplay(resource);
        } else {
            selectedResources = selections;
            exportAllTypesRadio.setSelection(true);
            exportSpecifiedTypesRadio.setSelection(false);
            resourceNameField.setText(CURRENT_SELECTION);
            exportCurrentSelection = true;
            displayResourcesSelectedCount(selectedResourceCount);
        }
    }

    /**
     * Updates the enablements of this page's controls. Subclasses may extend.
     */
    protected void updateWidgetEnablements() {
        if (exportCurrentSelection) {
			resourceDetailsButton.setEnabled(true);
		} else {
            IResource resource = getSourceResource();
            resourceDetailsButton.setEnabled(resource != null
                    && resource.isAccessible());
        }

        exportSpecifiedTypesRadio.setEnabled(!exportCurrentSelection);
        typesToExportField.setEnabled(exportSpecifiedTypesRadio.getSelection());
        typesToExportEditButton.setEnabled(exportSpecifiedTypesRadio
                .getSelection());
    }

    /* (non-Javadoc)
     * Method declared on WizardDataTransferPage.
     */
    protected final boolean validateSourceGroup() {
        if (exportCurrentSelection) {
			return true;
		}

        String sourceString = resourceNameField.getText();
        if (sourceString.equals("")) {//$NON-NLS-1$
            setErrorMessage(null);
            return false;
        }

        IResource resource = getSourceResource();

        if (resource == null) {
            setErrorMessage(IDEWorkbenchMessages.WizardExportPage_mustExistMessage);
            return false;
        }

        if (!resource.isAccessible()) {
            setErrorMessage(IDEWorkbenchMessages.WizardExportPage_mustBeAccessibleMessage);
            return false;
        }

        return true;
    }
}
