package org.eclipse.ui.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.ResourceAndContainerGroup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

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
	super("newFolderPage1");
	setTitle(pageName);
	setDescription("Create a new folder resource.");
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

	resourceGroup = new ResourceAndContainerGroup(composite,this,"Folder name:", "folder");
	resourceGroup.setAllowExistingResources(false);
	initializePage();
	validatePage();
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
protected void createFolder(IFolder folderHandle,IProgressMonitor monitor) throws CoreException {
	folderHandle.create(false,true,monitor);

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
				monitor.beginTask("Creating", 2000);
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
				"Creation Problems", 
				null,	// no special message
				((CoreException) e.getTargetException()).getStatus());
		}
		else {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log("Exception in " + getClass().getName() + ".getNewFolder(): " + e.getTargetException());
			MessageDialog.openError(getContainer().getShell(), "Creation problems", "Internal error: " + e.getTargetException().getMessage());
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
		if (selectedResource != null && !enum.hasNext()) {	// ie.- not a multi-selection
			if (selectedResource.getType() == IResource.FILE)
				selectedResource = selectedResource.getParent();
			if (selectedResource.isAccessible())
				resourceGroup.setContainerFullPath(selectedResource.getFullPath());
		}
	}

	resourceGroup.setFocus();
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

	if (!resourceGroup.areAllValuesValid()) {
		// if blank name then fail silently
		if (resourceGroup.getProblemType() == resourceGroup.PROBLEM_RESOURCE_EMPTY
			|| resourceGroup.getProblemType() == resourceGroup.PROBLEM_CONTAINER_EMPTY)
			setMessage(resourceGroup.getProblemMessage());
		else
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
}
