package org.eclipse.ui.dialogs;

/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.CreateLinkedResourceGroup;
import org.eclipse.ui.internal.misc.ResourceAndContainerGroup;
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
	private	IStructuredSelection currentSelection;
	
	// cache of newly-created file
	private IFile newFile;
	private IPath linkTargetPath;

	// widgets
	private ResourceAndContainerGroup resourceGroup;
	private Button advancedButton;
	private CreateLinkedResourceGroup linkedResourceGroup;
		
	// initial value stores
	private String initialFileName;
	private IPath initialContainerFullPath;
	/**
	 * Whether or not the advanced widget group is currently visible.
	 */
	private boolean linkedResourceGroupVisible = false;
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
public WizardNewFileCreationPage(String pageName, IStructuredSelection selection) {
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
	Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
	
	if (preferences.getBoolean(ResourcesPlugin.PREF_DISABLE_LINKING) == false) {
		advancedButton = new Button(parent, SWT.PUSH);
		advancedButton.setFont(parent.getFont());
		advancedButton.setText(WorkbenchMessages.getString("WizardNewFileCreationPage.advancedButtonCollapsed")); //$NON-NLS-1$
		GridData data = setButtonLayoutData(advancedButton);
		data.horizontalAlignment = GridData.BEGINNING;
		advancedButton.setLayoutData(data);
		advancedButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAdvancedButtonSelect();
			}
		});
	}
	linkedResourceGroup = new CreateLinkedResourceGroup(
		IResource.FILE,
		new Listener() {
			public void handleEvent(Event e) {
				setPageComplete(validatePage());
				firstLinkCheck = false;					
			}
		});	
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	initializeDialogUnits(parent);
	// top level group
	Composite topLevel = new Composite(parent,SWT.NONE);
	topLevel.setLayout(new GridLayout());
	topLevel.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	topLevel.setFont(parent.getFont());
	WorkbenchHelp.setHelp(topLevel, IHelpContextIds.NEW_FILE_WIZARD_PAGE);

	// resource and container group
	resourceGroup = new ResourceAndContainerGroup(topLevel, this, getNewFileLabel(), WorkbenchMessages.getString("WizardNewFileCreationPage.file"), false, SIZING_CONTAINER_GROUP_HEIGHT); //$NON-NLS-1$
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
protected void createFile(IFile fileHandle, InputStream contents, IProgressMonitor monitor) throws CoreException {
	if (contents == null)
		contents = new ByteArrayInputStream(new byte[0]);

	try {
		// Create a new file resource in the workspace
		if (linkTargetPath != null) 
			fileHandle.createLink(linkTargetPath, IResource.ALLOW_MISSING_LOCAL, monitor);
		else
			fileHandle.create(contents, false, monitor);
	}
	catch (CoreException e) {
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
	return WorkbenchPlugin.getPluginWorkspace().getRoot().getFile(filePath);
}
/**
 * Creates the link target path if a link target has been specified. 
 */
protected void createLinkTarget() {
	String linkTarget = linkedResourceGroup.getLinkTarget();
	if (linkTarget != null) {
		linkTargetPath = new Path(linkTarget);
	}
	else {
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
	WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		protected void execute(IProgressMonitor monitor) throws CoreException,
			InterruptedException
		{
			try {
				monitor.beginTask(WorkbenchMessages.getString("WizardNewFileCreationPage.progress"), 2000); //$NON-NLS-1$
				ContainerGenerator generator = new ContainerGenerator(containerPath);
				generator.generateContainer(new SubProgressMonitor(monitor, 1000));
				createFile(newFileHandle,initialContents, new SubProgressMonitor(monitor, 1000));
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
			ErrorDialog.openError(
				getContainer().getShell(), // Was Utilities.getFocusShell()
				WorkbenchMessages.getString("WizardNewFileCreationPage.errorTitle"),  //$NON-NLS-1$
				null,	// no special message
				((CoreException) e.getTargetException()).getStatus());
		}
		else {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log(MessageFormat.format("Exception in {0}.getNewFile(): {1}", new Object[] {getClass().getName(), e.getTargetException()}));//$NON-NLS-1$
			MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardNewFileCreationPage.internalErrorTitle"), WorkbenchMessages.format("WizardNewFileCreationPage.internalErrorMessage", new Object[] {e.getTargetException().getMessage()})); //$NON-NLS-2$ //$NON-NLS-1$
		}
		return null;
	}

	newFile = newFileHandle;

	return newFile;
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
	return WorkbenchMessages.getString("WizardNewFileCreationPage.fileLabel"); //$NON-NLS-1$
}
/**
 * Shows/hides the advanced option widgets. 
 */
protected void handleAdvancedButtonSelect() {
	Shell shell = getShell();
	Point shellSize = shell.getSize();
	Composite composite = (Composite) getControl();

	if (linkedResourceGroupHeight == -1) {
		Composite linkedResourceComposite = linkedResourceGroup.createContents(composite);
		Point groupSize = linkedResourceComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		linkedResourceGroupHeight = groupSize.y; 
	}
	if (linkedResourceGroupVisible) {
		linkedResourceGroupVisible = false;
		shell.setSize(shellSize.x, shellSize.y - linkedResourceGroupHeight);
		advancedButton.setText(WorkbenchMessages.getString("WizardNewFileCreationPage.advancedButtonCollapsed")); //$NON-NLS-1$
	} else {
		linkedResourceGroupVisible = true;
		shell.setSize(shellSize.x, shellSize.y + linkedResourceGroupHeight);
		composite.layout();
		advancedButton.setText(WorkbenchMessages.getString("WizardNewFileCreationPage.advancedButtonExpanded")); //$NON-NLS-1$
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
		Iterator enum = currentSelection.iterator();
		if (enum.hasNext()) {
			Object object = enum.next();
			IResource selectedResource = null;
			if (object instanceof IResource) {
				selectedResource = (IResource)object;
			} else if (object instanceof IAdaptable) {
				selectedResource = (IResource)((IAdaptable)object).getAdapter(IResource.class);
			}
			if (selectedResource != null) {
				if (selectedResource.getType() == IResource.FILE)
					selectedResource = selectedResource.getParent();
				if (selectedResource.isAccessible())
					resourceGroup.setContainerFullPath(selectedResource.getFullPath());
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
	IStatus status = linkedResourceGroup.validateLinkLocation(newFileHandle);

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
	if (valid && (linkedResourceStatus == null || linkedResourceStatus.isOK())) {
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
	if(visible)
		resourceGroup.setFocus();
}
}
