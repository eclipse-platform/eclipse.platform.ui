package org.eclipse.ui.dialogs;

/**
 * Copyright (c) 2000, 2003 IBM Corp. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * 	Nick Edgar: Initial Implementation
 * 	Simon Arsenault: Fix for PR 2248, 2473
 *  Randy Giffen: Help Support
 *  Karice MacIntyre: Print Support
 *  Leon J. Breedt: Added multiple folder creation support
 *  Tod Creasey: Integration of patches      
 */

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.StringTokenizer;

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
 * Standard main page for a wizard that creates a folder resource.
 * <p>
 * This page may be used by clients as-is; it may be also be subclassed to suit.
 * </p>
 * <p>
 * Subclasses may extend
 * <ul>
 *   <li><code>handleEvent</code></li>
 * </ul>
 * </p>
 */
public class WizardNewFolderMainPage extends WizardPage implements Listener {
	private static final int SIZING_CONTAINER_GROUP_HEIGHT = 250;
	private IStructuredSelection currentSelection;
	private IContainer currentParent;

	private IFolder newFolder;
	// link target location
	private IPath linkTargetPath;
	
	// widgets
	private ResourceAndContainerGroup resourceGroup;
	private Button advancedButton;
	private CreateLinkedResourceGroup linkedResourceGroup;
/**
 * Creates a new folder creation wizard page. If the initial resource selection 
 * contains exactly one container resource then it will be used as the default
 * container resource.
 *
 * @param pageName the name of the page
 * @param selection the current resource selection
 */
public WizardNewFolderMainPage(String pageName, IStructuredSelection selection) {
	super("newFolderPage1");//$NON-NLS-1$
	setTitle(pageName);
	setDescription(WorkbenchMessages.getString("WizardNewFolderMainPage.description")); //$NON-NLS-1$
	this.currentSelection = selection;
}
/**
 * Creates the widget for advanced options.
 *  
 * @param parent the parent composite
 */
protected void createAdvancedControls(Composite parent) {
	advancedButton = new Button(parent, SWT.PUSH);
	advancedButton.setFont(parent.getFont());
	advancedButton.setText(WorkbenchMessages.getString("WizardNewFolderMainPage.advancedButtonCollapsed"));
	GridData data = setButtonLayoutData(advancedButton);
	data.horizontalAlignment = GridData.BEGINNING;
	advancedButton.setLayoutData(data);
	advancedButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			handleAdvancedButtonSelect();
		}
	});
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	initializeDialogUnits(parent);
	// top level group
	Composite composite = new Composite(parent,SWT.NONE);
	composite.setFont(parent.getFont());
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

	WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_FOLDER_WIZARD_PAGE);

	resourceGroup = new ResourceAndContainerGroup(composite,this,WorkbenchMessages.getString("WizardNewFolderMainPage.folderName"), WorkbenchMessages.getString("WizardNewFolderMainPage.folderLabel"), false, SIZING_CONTAINER_GROUP_HEIGHT); //$NON-NLS-2$ //$NON-NLS-1$
	resourceGroup.setAllowExistingResources(false);
	initializePage();
	createAdvancedControls(composite);	
	validatePage();
	// Show description on opening
	setErrorMessage(null);
	setMessage(null);
	setControl(composite);
}
/**
 * Creates a folder resource given the folder handle.
 *
 * @param folderHandle the folder handle to create a folder resource for
 * @param monitor the progress monitor to show visual progress with
 * @exception CoreException if the operation fails
 * @exception OperationCanceledException if the operation is canceled
 */
protected void createFolder(IFolder folderHandle, IProgressMonitor monitor) throws CoreException {
    try {
        // Create the folder resource in the workspace
        // Update: Recursive to create any folders which do not exist already
        if (!folderHandle.exists()) {
            IContainer parent= folderHandle.getParent();
            if (parent instanceof IFolder && (!((IFolder)parent).exists())) {
                createFolder((IFolder)parent, monitor);
            }
            if (linkTargetPath != null)
            	folderHandle.createLink(linkTargetPath, IResource.ALLOW_MISSING_LOCAL, monitor);
            else
        		folderHandle.create(false, true, monitor);
        }
    }
    catch (CoreException e) {
        // If the folder already existed locally, just refresh to get contents
        if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
            folderHandle.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 500));
        else
            throw e;
    }

    if (monitor.isCanceled())
        throw new OperationCanceledException();
}

/**
 * Creates a folder resource handle for the folder with the given workspace path.
 * This method does not create the folder resource; this is the responsibility
 * of <code>createFolder</code>.
 *
 * @param folderPath the path of the folder resource to create a handle for
 * @return the new folder resource handle
 * @see #createFolder
 */
protected IFolder createFolderHandle(IPath folderPath) {
	return WorkbenchPlugin.getPluginWorkspace().getRoot().getFolder(folderPath);
}
/**
 * Creates the link target path if a link target has been specified. 
 */
protected void createLinkTarget() {
	if (linkedResourceGroup != null) {
		String linkTarget = linkedResourceGroup.getLinkTarget();
		if (linkTarget != null) {
			linkTargetPath = new Path(linkTarget);
		}
		else {
			linkTargetPath = null;
		}
	}
	else {
		linkTargetPath = null;
	}
}
/**
 * Creates a new folder resource in the selected container and with the selected
 * name. Creates any missing resource containers along the path; does nothing if
 * the container resources already exist.
 * <p>
 * In normal usage, this method is invoked after the user has pressed Finish on
 * the wizard; the enablement of the Finish button implies that all controls on
 * this page currently contain valid values.
 * </p>
 * <p>
 * Note that this page caches the new folder once it has been successfully
 * created; subsequent invocations of this method will answer the same
 * folder resource without attempting to create it again.
 * </p>
 * <p>
 * This method should be called within a workspace modify operation since
 * it creates resources.
 * </p>
 *
 * @return the created folder resource, or <code>null</code> if the folder
 *    was not created
 */
public IFolder createNewFolder() {
	if (newFolder != null)
		return newFolder;

	// create the new folder and cache it if successful
	final IPath containerPath = resourceGroup.getContainerFullPath();
	IPath newFolderPath = containerPath.append(resourceGroup.getResource());
	final IFolder newFolderHandle = createFolderHandle(newFolderPath);

	createLinkTarget();
	WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		public void execute(IProgressMonitor monitor) throws CoreException {
			try {
				monitor.beginTask(WorkbenchMessages.getString("WizardNewFolderCreationPage.progress"), 2000); //$NON-NLS-1$
				ContainerGenerator generator = new ContainerGenerator(containerPath);
				generator.generateContainer(new SubProgressMonitor(monitor, 1000));
				createFolder(newFolderHandle, new SubProgressMonitor(monitor, 1000));
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
				WorkbenchMessages.getString("WizardNewFolderCreationPage.errorTitle"),  //$NON-NLS-1$
				null,	// no special message
				((CoreException) e.getTargetException()).getStatus());
		}
		else {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			
			WorkbenchPlugin.log(MessageFormat.format("Exception in {0}.getNewFolder(): {1}", new Object[] {getClass().getName(),e.getTargetException()}));//$NON-NLS-1$
			MessageDialog.openError(getContainer().getShell(), WorkbenchMessages.getString("WizardNewFolderCreationPage.internalErrorTitle"), WorkbenchMessages.format("WizardNewFolder.internalError", new Object[] {e.getTargetException().getMessage()})); //$NON-NLS-2$ //$NON-NLS-1$
		}
		return null;	// ie.- one of the steps resulted in a core exception
	}

	newFolder = newFolderHandle;

	return newFolder;
}
/**
 * Shows/hides the advanced option widgets. 
 */
protected void handleAdvancedButtonSelect() {
	Shell shell = getShell();
	
	if (linkedResourceGroup != null) {
		Composite composite = (Composite) getControl();
		linkedResourceGroup.dispose();
		linkedResourceGroup = null;
		setPageComplete(validatePage());
		advancedButton.setText(WorkbenchMessages.getString("WizardNewFolderMainPage.advancedButtonCollapsed"));
		
		Point newSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point oldSize = composite.getSize();
		int heightDelta = newSize.y - oldSize.y; 
		if (heightDelta < 0) {
			Point shellSize = shell.getSize();
			shell.setSize(shellSize.x, shellSize.y + heightDelta);
		}
	} else {
		Composite composite = (Composite) getControl();
		linkedResourceGroup = new CreateLinkedResourceGroup(
			IResource.FOLDER,
			new Listener() {
				public void handleEvent(Event e) {
					setPageComplete(validatePage());
				}
			});
		linkedResourceGroup.createContents(composite);
		advancedButton.setText(WorkbenchMessages.getString("WizardNewFolderMainPage.advancedButtonExpanded"));
		
		Point newSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point oldSize = composite.getSize();
		int widthDelta = Math.max(newSize.x - oldSize.x, 0);
		int heightDelta = Math.max(newSize.y - oldSize.y, 0); 
		if (widthDelta > 0 || heightDelta > 0) {
			Point shellSize = shell.getSize();
			shell.setSize(shellSize.x + widthDelta, shellSize.y + heightDelta);
		}
	}
}
/**
 * The <code>WizardNewFolderCreationPage</code> implementation of this 
 * <code>Listener</code> method handles all events and enablements for controls
 * on this page. Subclasses may extend.
 */
public void handleEvent(Event ev) {
	setPageComplete(validatePage());
}
/**
 * Initializes this page's controls.
 */
protected void initializePage() {
	Iterator enum = currentSelection.iterator();
	if (enum.hasNext()) {
		Object next = enum.next();
		IResource selectedResource = null;
		if (next instanceof IResource) {
			selectedResource = (IResource)next;
		} else if (next instanceof IAdaptable) {
			selectedResource = (IResource)((IAdaptable)next).getAdapter(IResource.class);
		}
		if (selectedResource != null) {
			if (selectedResource.getType() == IResource.FILE)
				selectedResource = selectedResource.getParent();
			if (selectedResource.isAccessible())
				resourceGroup.setContainerFullPath(selectedResource.getFullPath());
		}
	}

	setPageComplete(false);
}
/**
 * Checks whether the linked resource target is valid.
 * Sets the error message accordingly and returns the status.
 *  
 * @return IStatus validation result from the CreateLinkedResourceGroup
 */
protected IStatus validateLinkedResource() {
	IPath containerPath = resourceGroup.getContainerFullPath();
	IPath newFolderPath = containerPath.append(resourceGroup.getResource());
	IFolder newFolderHandle = createFolderHandle(newFolderPath);
	IStatus status = linkedResourceGroup.validateLinkLocation(newFolderHandle);

	if (status.getSeverity() == IStatus.ERROR) {
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
	
	IWorkspace workspace = WorkbenchPlugin.getPluginWorkspace();

    IStatus nameStatus = null;
    String folderName = resourceGroup.getResource();
    if (folderName.indexOf(IPath.SEPARATOR) != -1) {
        StringTokenizer tok = new StringTokenizer(folderName, String.valueOf(IPath.SEPARATOR));
        while (tok.hasMoreTokens()) {
            String pathFragment = tok.nextToken();
            nameStatus = workspace.validateName(pathFragment, IResource.FOLDER);
            if (!nameStatus.isOK()) {
                break;
            }
        }
    }
   
   //If the name status was not set validate using the name
   	if(nameStatus == null)
        nameStatus =
            workspace.validateName(folderName, IResource.FOLDER);
            
    if (!nameStatus.isOK()) {
        setErrorMessage(nameStatus.getMessage());
        return false;
    }

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
	if (valid && linkedResourceGroup != null) {
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

