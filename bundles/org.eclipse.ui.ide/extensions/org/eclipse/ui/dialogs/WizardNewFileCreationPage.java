/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.CreateLinkedResourceGroup;
import org.eclipse.ui.internal.ide.misc.ResourceAndContainerGroup;

/**
 * Standard main page for a wizard that creates a file resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Subclasses may override
 * <ul>
 *   <li><code>getInitialContents</code></li>
 *   <li><code>getNewFileLabel</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 */
public class WizardNewFileCreationPage extends WizardPage implements Listener {
    private static final int SIZING_CONTAINER_GROUP_HEIGHT = 250;

    // the current resource selection
    private IStructuredSelection currentSelection;

    // cache of newly-created file
    private IFile newFile;

    private IPath linkTargetPath;

    // widgets
    private ResourceAndContainerGroup resourceGroup;

    private Button advancedButton;

    private CreateLinkedResourceGroup linkedResourceGroup;

    private Composite linkedResourceParent;

    private Composite linkedResourceComposite;

    // initial value stores
    private String initialFileName;

    private IPath initialContainerFullPath;

    /**
     * Height of the "advanced" linked resource group. Set when the
     * advanced group is first made visible. 
     */
    private int linkedResourceGroupHeight = -1;

    /**
     * First time the advanced group is validated.
     */
    private boolean firstLinkCheck = true;

    /**
     * Creates a new file creation wizard page. If the initial resource selection 
     * contains exactly one container resource then it will be used as the default
     * container resource.
     *
     * @param pageName the name of the page
     * @param selection the current resource selection
     */
    public WizardNewFileCreationPage(String pageName,
            IStructuredSelection selection) {
        super(pageName);
        setPageComplete(false);
        this.currentSelection = selection;
    }

    /**
     * Creates the widget for advanced options.
     *  
     * @param parent the parent composite
     */
    protected void createAdvancedControls(Composite parent) {
        Preferences preferences = ResourcesPlugin.getPlugin()
                .getPluginPreferences();

        if (preferences.getBoolean(ResourcesPlugin.PREF_DISABLE_LINKING) == false) {
            linkedResourceParent = new Composite(parent, SWT.NONE);
            linkedResourceParent.setFont(parent.getFont());
            linkedResourceParent.setLayoutData(new GridData(
                    GridData.FILL_HORIZONTAL));
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            linkedResourceParent.setLayout(layout);

            advancedButton = new Button(linkedResourceParent, SWT.PUSH);
            advancedButton.setFont(linkedResourceParent.getFont());
            advancedButton.setText(IDEWorkbenchMessages.showAdvanced);
            GridData data = setButtonLayoutData(advancedButton);
            data.horizontalAlignment = GridData.BEGINNING;
            advancedButton.setLayoutData(data);
            advancedButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    handleAdvancedButtonSelect();
                }
            });
        }
        linkedResourceGroup = new CreateLinkedResourceGroup(IResource.FILE,
                new Listener() {
                    public void handleEvent(Event e) {
                        setPageComplete(validatePage());
                        firstLinkCheck = false;
                    }
                },
                new CreateLinkedResourceGroup.IStringValue(){
					public void setValue(String string) {
						resourceGroup.setResource(string);
					}
					public String getValue() {
						return resourceGroup.getResource();
					}
                });
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        // top level group
        Composite topLevel = new Composite(parent, SWT.NONE);
        topLevel.setLayout(new GridLayout());
        topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        topLevel.setFont(parent.getFont());
        PlatformUI.getWorkbench().getHelpSystem().setHelp(topLevel,
				IIDEHelpContextIds.NEW_FILE_WIZARD_PAGE);

        // resource and container group
        resourceGroup = new ResourceAndContainerGroup(
                topLevel,
                this,
                getNewFileLabel(),
                IDEWorkbenchMessages.WizardNewFileCreationPage_file, false, SIZING_CONTAINER_GROUP_HEIGHT);
        resourceGroup.setAllowExistingResources(false);
        initialPopulateContainerNameField();
        createAdvancedControls(topLevel);
        if (initialFileName != null)
            resourceGroup.setResource(initialFileName);
        validatePage();
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(topLevel);
    }

    /**
     * Creates a file resource given the file handle and contents.
     *
     * @param fileHandle the file handle to create a file resource with
     * @param contents the initial contents of the new file resource, or
     *   <code>null</code> if none (equivalent to an empty stream)
     * @param monitor the progress monitor to show visual progress with
     * @exception CoreException if the operation fails
     * @exception OperationCanceledException if the operation is canceled
     */
    protected void createFile(IFile fileHandle, InputStream contents,
            IProgressMonitor monitor) throws CoreException {
        if (contents == null)
            contents = new ByteArrayInputStream(new byte[0]);

        try {
            // Create a new file resource in the workspace
            if (linkTargetPath != null)
                fileHandle.createLink(linkTargetPath,
                        IResource.ALLOW_MISSING_LOCAL, monitor);
            else {
                IPath path = fileHandle.getFullPath();
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                int numSegments= path.segmentCount();
                if (numSegments > 2 && !root.getFolder(path.removeLastSegments(1)).exists()) {
                    // If the direct parent of the path doesn't exist, try to create the
                    // necessary directories.
                    for (int i= numSegments - 2; i > 0; i--) {
                        IFolder folder = root.getFolder(path.removeLastSegments(i));
                        if (!folder.exists()) {
                            folder.create(false, true, monitor);
                        }
                    }
                }
                fileHandle.create(contents, false, monitor);
            }
        } catch (CoreException e) {
            // If the file already existed locally, just refresh to get contents
            if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
                fileHandle.refreshLocal(IResource.DEPTH_ZERO, null);
            else
                throw e;
        }

        if (monitor.isCanceled())
            throw new OperationCanceledException();
    }

    /**
     * Creates a file resource handle for the file with the given workspace path.
     * This method does not create the file resource; this is the responsibility
     * of <code>createFile</code>.
     *
     * @param filePath the path of the file resource to create a handle for
     * @return the new file resource handle
     * @see #createFile
     */
    protected IFile createFileHandle(IPath filePath) {
        return IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getFile(
                filePath);
    }

    /**
     * Creates the link target path if a link target has been specified. 
     */
    protected void createLinkTarget() {
        String linkTarget = linkedResourceGroup.getLinkTarget();
        if (linkTarget != null) {
            linkTargetPath = new Path(linkTarget);
        } else {
            linkTargetPath = null;
        }
    }

    /**
     * Creates a new file resource in the selected container and with the selected
     * name. Creates any missing resource containers along the path; does nothing if
     * the container resources already exist.
     * <p>
     * In normal usage, this method is invoked after the user has pressed Finish on
     * the wizard; the enablement of the Finish button implies that all controls on 
     * on this page currently contain valid values.
     * </p>
     * <p>
     * Note that this page caches the new file once it has been successfully
     * created; subsequent invocations of this method will answer the same
     * file resource without attempting to create it again.
     * </p>
     * <p>
     * This method should be called within a workspace modify operation since
     * it creates resources.
     * </p>
     *
     * @return the created file resource, or <code>null</code> if the file
     *    was not created
     */
    public IFile createNewFile() {
        if (newFile != null)
            return newFile;

        // create the new file and cache it if successful

        final IPath containerPath = resourceGroup.getContainerFullPath();
        IPath newFilePath = containerPath.append(resourceGroup.getResource());
        final IFile newFileHandle = createFileHandle(newFilePath);
        final InputStream initialContents = getInitialContents();

        createLinkTarget();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation(createRule(newFileHandle)) {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                    monitor.beginTask(IDEWorkbenchMessages.WizardNewFileCreationPage_progress, 2000);
                    ContainerGenerator generator = new ContainerGenerator(
                            containerPath);
                    generator.generateContainer(new SubProgressMonitor(monitor,
                            1000));
                    createFile(newFileHandle, initialContents,
                            new SubProgressMonitor(monitor, 1000));
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof CoreException) {
                ErrorDialog
                        .openError(
                                getContainer().getShell(), // Was Utilities.getFocusShell()
                                IDEWorkbenchMessages.WizardNewFileCreationPage_errorTitle,
                                null, // no special message
                                ((CoreException) e.getTargetException())
                                        .getStatus());
            } else {
                // CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
                IDEWorkbenchPlugin.log(getClass(),
                        "createNewFile()", e.getTargetException()); //$NON-NLS-1$
                MessageDialog
                        .openError(
                                getContainer().getShell(),
                                IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorTitle, NLS.bind(IDEWorkbenchMessages.WizardNewFileCreationPage_internalErrorMessage, e.getTargetException().getMessage()));
            }
            return null;
        }

        newFile = newFileHandle;

        return newFile;
    }

    /**
     * Returns the scheduling rule to use when creating the resource at
     * the given container path. The rule should be the creation rule for
     * the top-most non-existing parent.
     * @param resource The resource being created
     * @return The scheduling rule for creating the given resource
     * @since 3.1
     */
    protected ISchedulingRule createRule(IResource resource) {
		IResource parent = resource.getParent();
    	while (parent != null) {
    		if (parent.exists())
    			return resource.getWorkspace().getRuleFactory().createRule(resource);
    		resource = parent;
    		parent = parent.getParent();
    	}
		return resource.getWorkspace().getRoot();
	}

	/**
     * Returns the current full path of the containing resource as entered or 
     * selected by the user, or its anticipated initial value.
     *
     * @return the container's full path, anticipated initial value, 
     *   or <code>null</code> if no path is known
     */
    public IPath getContainerFullPath() {
        return resourceGroup.getContainerFullPath();
    }

    /**
     * Returns the current file name as entered by the user, or its anticipated
     * initial value.
     *
     * @return the file name, its anticipated initial value, or <code>null</code>
     *   if no file name is known
     */
    public String getFileName() {
        if (resourceGroup == null)
            return initialFileName;

        return resourceGroup.getResource();
    }

    /**
     * Returns a stream containing the initial contents to be given to new file resource
     * instances.  <b>Subclasses</b> may wish to override.  This default implementation
     * provides no initial contents.
     *
     * @return initial contents to be given to new file resource instances
     */
    protected InputStream getInitialContents() {
        return null;
    }

    /**
     * Returns the label to display in the file name specification visual
     * component group.
     * <p>
     * Subclasses may reimplement.
     * </p>
     *
     * @return the label to display in the file name specification visual
     *     component group
     */
    protected String getNewFileLabel() {
        return IDEWorkbenchMessages.WizardNewFileCreationPage_fileLabel;
    }

    /**
     * Shows/hides the advanced option widgets. 
     */
    protected void handleAdvancedButtonSelect() {
        Shell shell = getShell();
        Point shellSize = shell.getSize();
        Composite composite = (Composite) getControl();

        if (linkedResourceComposite != null) {
            linkedResourceComposite.dispose();
            linkedResourceComposite = null;
            composite.layout();
            shell.setSize(shellSize.x, shellSize.y - linkedResourceGroupHeight);
            advancedButton.setText(IDEWorkbenchMessages.showAdvanced);
        } else {
            linkedResourceComposite = linkedResourceGroup
                    .createContents(linkedResourceParent);
            if (linkedResourceGroupHeight == -1) {
                Point groupSize = linkedResourceComposite.computeSize(
                        SWT.DEFAULT, SWT.DEFAULT, true);
                linkedResourceGroupHeight = groupSize.y;
            }
            shell.setSize(shellSize.x, shellSize.y + linkedResourceGroupHeight);
            composite.layout();
            advancedButton.setText(IDEWorkbenchMessages.hideAdvanced);
        }
    }

    /**
     * The <code>WizardNewFileCreationPage</code> implementation of this 
     * <code>Listener</code> method handles all events and enablements for controls
     * on this page. Subclasses may extend.
     */
    public void handleEvent(Event event) {
        setPageComplete(validatePage());
    }

    /**
     * Sets the initial contents of the container name entry field, based upon
     * either a previously-specified initial value or the ability to determine
     * such a value.
     */
    protected void initialPopulateContainerNameField() {
        if (initialContainerFullPath != null)
            resourceGroup.setContainerFullPath(initialContainerFullPath);
        else {
            Iterator it = currentSelection.iterator();
            if (it.hasNext()) {
                Object object = it.next();
                IResource selectedResource = null;
                if (object instanceof IResource) {
                    selectedResource = (IResource) object;
                } else if (object instanceof IAdaptable) {
                    selectedResource = (IResource) ((IAdaptable) object)
                            .getAdapter(IResource.class);
                }
                if (selectedResource != null) {
                    if (selectedResource.getType() == IResource.FILE)
                        selectedResource = selectedResource.getParent();
                    if (selectedResource.isAccessible())
                        resourceGroup.setContainerFullPath(selectedResource
                                .getFullPath());
                }
            }
        }
    }

    /**
     * Sets the value of this page's container name field, or stores
     * it for future use if this page's controls do not exist yet.
     *
     * @param path the full path to the container
     */
    public void setContainerFullPath(IPath path) {
        if (resourceGroup == null)
            initialContainerFullPath = path;
        else
            resourceGroup.setContainerFullPath(path);
    }

    /**
     * Sets the value of this page's file name field, or stores
     * it for future use if this page's controls do not exist yet.
     *
     * @param value new file name
     */
    public void setFileName(String value) {
        if (resourceGroup == null)
            initialFileName = value;
        else
            resourceGroup.setResource(value);
    }

    /**
     * Checks whether the linked resource target is valid.
     * Sets the error message accordingly and returns the status.
     *  
     * @return IStatus validation result from the CreateLinkedResourceGroup
     */
    protected IStatus validateLinkedResource() {
        IPath containerPath = resourceGroup.getContainerFullPath();
        IPath newFilePath = containerPath.append(resourceGroup.getResource());
        IFile newFileHandle = createFileHandle(newFilePath);
        IStatus status = linkedResourceGroup
                .validateLinkLocation(newFileHandle);

        if (status.getSeverity() == IStatus.ERROR) {
            if (firstLinkCheck)
                setMessage(status.getMessage());
            else
                setErrorMessage(status.getMessage());
        } else if (status.getSeverity() == IStatus.WARNING) {
            setMessage(status.getMessage(), WARNING);
            setErrorMessage(null);
        }
        return status;
    }

    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    protected boolean validatePage() {
        boolean valid = true;

        if (!resourceGroup.areAllValuesValid()) {
            // if blank name then fail silently
            if (resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_RESOURCE_EMPTY
                    || resourceGroup.getProblemType() == ResourceAndContainerGroup.PROBLEM_CONTAINER_EMPTY) {
                setMessage(resourceGroup.getProblemMessage());
                setErrorMessage(null);
            } else
                setErrorMessage(resourceGroup.getProblemMessage());
            valid = false;
        }

        IStatus linkedResourceStatus = null;
        if (valid) {
            linkedResourceStatus = validateLinkedResource();
            if (linkedResourceStatus.getSeverity() == IStatus.ERROR)
                valid = false;
        }
        // validateLinkedResource sets messages itself
        if (valid
                && (linkedResourceStatus == null || linkedResourceStatus.isOK())) {
            setMessage(null);
            setErrorMessage(null);
        }
        return valid;
    }

    /*
     * @see DialogPage.setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible)
            resourceGroup.setFocus();
    }
}
