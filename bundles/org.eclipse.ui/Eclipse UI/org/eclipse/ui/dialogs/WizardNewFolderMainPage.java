package org.eclipse.ui.dialogs;

/**
 * Copyright (c) 2000, 2002 IBM Corp. and others.
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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
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
	private IStructuredSelection currentSelection;
	private IContainer currentParent;

	private IFolder newFolder;
	
	// widgets
	private ResourceAndContainerGroup resourceGroup;
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
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	// top level group
	Composite composite = new Composite(parent,SWT.NONE);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

	WorkbenchHelp.setHelp(composite, IHelpContextIds.NEW_FOLDER_WIZARD_PAGE);

	resourceGroup = new ResourceAndContainerGroup(composite,this,WorkbenchMessages.getString("WizardNewFolderMainPage.folderName"), WorkbenchMessages.getString("WizardNewFolderMainPage.folderLabel"), false); //$NON-NLS-2$ //$NON-NLS-1$
	resourceGroup.setAllowExistingResources(false);
	initializePage();
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
    } else {
        nameStatus =
            workspace.validateName(folderName, IResource.FOLDER);
    }
    if (!nameStatus.isOK()) {
        setErrorMessage(nameStatus.getMessage());
        return false;
    }

	if (!resourceGroup.areAllValuesValid()) {
		// if blank name then fail silently
		if (resourceGroup.getProblemType() == resourceGroup.PROBLEM_RESOURCE_EMPTY
			|| resourceGroup.getProblemType() == resourceGroup.PROBLEM_CONTAINER_EMPTY) {
			setMessage(resourceGroup.getProblemMessage());
			setErrorMessage(null);
		} else
			setErrorMessage(resourceGroup.getProblemMessage());
		valid = false;
	}

	// Avoid draw flicker by clearing error message
	// if all is valid.
	if (valid) {
		setErrorMessage(null);
		setMessage(null);
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

