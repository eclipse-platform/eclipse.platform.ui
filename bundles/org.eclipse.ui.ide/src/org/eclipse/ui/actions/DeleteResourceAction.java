/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Standard action for deleting the currently selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DeleteResourceAction extends SelectionListenerAction {

	static class DeleteProjectDialog extends MessageDialog {

		private List projects;
		private boolean deleteContent = false;
		private Button radio1;
		private Button radio2;
		
		DeleteProjectDialog(Shell parentShell, List projects) {
			super(
				parentShell, 
				getTitle(projects), 
				null,	// accept the default window icon
				getMessage(projects),
				MessageDialog.QUESTION, 
				new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
				0); 	// yes is the default
			this.projects = projects;
		}
		
		static String getTitle(List projects) {
			if (projects.size() == 1)
				return WorkbenchMessages.getString("DeleteResourceAction.titleProject1");  //$NON-NLS-1$
			else
				return WorkbenchMessages.getString("DeleteResourceAction.titleProjectN");  //$NON-NLS-1$
		}
		
		static String getMessage(List projects) {
			if (projects.size() == 1) {
				IProject project = (IProject) projects.get(0);
				return WorkbenchMessages.format("DeleteResourceAction.confirmProject1", new Object[] { project.getName() });  //$NON-NLS-1$
			}
			else {
				return WorkbenchMessages.format("DeleteResourceAction.confirmProjectN", new Object[] { new Integer(projects.size()) });  //$NON-NLS-1$
			}
		}

		/* (non-Javadoc)
		 * Method declared on Window.
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			WorkbenchHelp.setHelp(newShell, IHelpContextIds.DELETE_PROJECT_DIALOG);
		}
	
		protected Control createCustomArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			radio1 = new Button(composite, SWT.RADIO);
			radio1.addSelectionListener(selectionListener);
			String text1;
			if (projects.size() == 1) {
				IProject project = (IProject) projects.get(0);
				if(project == null || project.getLocation() == null)
					text1 = WorkbenchMessages.getString("DeleteResourceAction.deleteContentsN");  //$NON-NLS-1$
				else
					text1 = WorkbenchMessages.format("DeleteResourceAction.deleteContents1", new Object[] { project.getLocation().toOSString() });  //$NON-NLS-1$
			} else {
				text1 = WorkbenchMessages.getString("DeleteResourceAction.deleteContentsN");  //$NON-NLS-1$
			}
			radio1.setText(text1);
			radio1.setFont(parent.getFont());

			radio2 = new Button(composite, SWT.RADIO);
			radio2.addSelectionListener(selectionListener);
			String text2 = WorkbenchMessages.getString("DeleteResourceAction.doNotDeleteContents");  //$NON-NLS-1$
			radio2.setText(text2);
			radio2.setFont(parent.getFont());
			
			// set initial state
			radio1.setSelection(deleteContent);
			radio2.setSelection(!deleteContent);
			
			return composite;
		}
		
		private SelectionListener selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.widget;
				if (button.getSelection()) {
					deleteContent = (button == radio1);
				}
			}
		};
		
		public boolean getDeleteContent() {
			return deleteContent;
		}
	}
	
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".DeleteResourceAction";//$NON-NLS-1$
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

	/**
	 * Whether or not we are deleting content for projects.
	 */
	private boolean deleteContent = false;
	
	/**
	 * Whether or not to automatically delete out of sync resources
	 */
	private boolean forceOutOfSyncDelete = false;
/**
 * Creates a new delete resource action.
 *
 * @param shell the shell for any dialogs
 */
public DeleteResourceAction(Shell shell) {
	super(WorkbenchMessages.getString("DeleteResourceAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("DeleteResourceAction.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.DELETE_RESOURCE_ACTION);
	setId(ID);
	Assert.isNotNull(shell);
	this.shell = shell;
}
/**
 * Returns whether delete can be performed on the current selection.
 *
 * @return <code>true</code> if the resources can be deleted, and 
 *  <code>false</code> if the selection contains non-resources or phantom
 *  resources
 */
boolean canDelete() {
	// allow only projects or only non-projects to be selected; 
	// note that the selection may contain multiple types of resource
	if (!(containsOnlyProjects() || containsOnlyNonProjects())) {
		return false;
	}
	
	List resources = getSelectedResources();
	if (resources.size() == 0) return false;	
	// Return true if everything in the selection exists.
	for (Iterator e = resources.iterator(); e.hasNext();) {
		IResource next = (IResource)e.next();
		if (next.isPhantom()) {
			return false;
		}
	}
	return true;
}
/**
 * Returns whether the selection contains linked resources.
 *
 * @return <code>true</code> if the resources contain linked   
 *  resources, and <code>false</code> otherwise
 */
boolean containsLinkedResource() {
	Iterator iterator = getSelectedResources().iterator();
	while (iterator.hasNext()) {
		IResource resource = (IResource) iterator.next();
		if (resource.isLinked()) return true;
	}
	return false;
}
/**
 * Returns whether the selection contains only non-projects.
 *
 * @return <code>true</code> if the resources contains only non-projects, and 
 *  <code>false</code> otherwise
 */
boolean containsOnlyNonProjects() {
	if (getSelectedNonResources().size() > 0) return false;
	int types = getSelectedResourceTypes();
	// check for empty selection
	if (types == 0) return false;
	// note that the selection may contain multiple types of resource
	return (types & IResource.PROJECT) == 0;
}
/**
 * Returns whether the selection contains only projects.
 *
 * @return <code>true</code> if the resources contains only projects, and 
 *  <code>false</code> otherwise
 */
boolean containsOnlyProjects() {
	if (getSelectedNonResources().size() > 0) return false;
	int types = getSelectedResourceTypes();
	// note that the selection may contain multiple types of resource
	return types == IResource.PROJECT;
}

/**
 * Asks the user to confirm a delete operation.
 *
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
boolean confirmDelete() {
	if (containsOnlyProjects()) {
		return confirmDeleteProjects();
	}
	else {
		return confirmDeleteNonProjects();
	}
}
/**
 * Asks the user to confirm a delete operation,
 * where the selection contains no projects.
 *
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
boolean confirmDeleteNonProjects() {
	List resources = getSelectedResources();
	String title;
	String msg;
	if (resources.size() == 1) {
		title = WorkbenchMessages.getString("DeleteResourceAction.title1");  //$NON-NLS-1$
 		IResource resource = (IResource) resources.get(0);
 		if (resource.isLinked())
 			msg = WorkbenchMessages.format("DeleteResourceAction.confirmLinkedResource1", new Object[] { resource.getName() });  //$NON-NLS-1$
 		else
			msg = WorkbenchMessages.format("DeleteResourceAction.confirm1", new Object[] { resource.getName() });  //$NON-NLS-1$
	}
	else {
		title = WorkbenchMessages.getString("DeleteResourceAction.titleN");  //$NON-NLS-1$
		if (containsLinkedResource())
			msg = WorkbenchMessages.format("DeleteResourceAction.confirmLinkedResourceN", new Object[] { new Integer(resources.size()) });  //$NON-NLS-1$
		else
			msg = WorkbenchMessages.format("DeleteResourceAction.confirmN", new Object[] { new Integer(resources.size()) });  //$NON-NLS-1$
	}
	return MessageDialog.openQuestion(shell, title, msg);
}
/**
 * Asks the user to confirm a delete operation,
 * where the selection contains only projects.
 * Also remembers whether project content should be deleted.
 *
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
boolean confirmDeleteProjects() {
	List resources = getSelectedResources();
	DeleteProjectDialog dialog = new DeleteProjectDialog(shell, resources);
	int code = dialog.open();
	deleteContent = dialog.getDeleteContent();
	return code == 0;  // YES
}
/**
 * Deletes the given resources.
 */
void delete(IResource[] resourcesToDelete, IProgressMonitor monitor) throws CoreException {
	forceOutOfSyncDelete = false;
	monitor.beginTask("", resourcesToDelete.length); //$NON-NLS-1$
	for (int i = 0; i < resourcesToDelete.length; ++i) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		delete(resourcesToDelete[i], new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
	}
	monitor.done();
}
/**
 * Deletes the given resource.
 */
void delete(IResource resourceToDelete, IProgressMonitor monitor) throws CoreException {
	boolean force = false; // don't force deletion of out-of-sync resources
	try {
		if (resourceToDelete.getType() == IResource.PROJECT) {
			// if it's a project, ask whether content should be deleted too
			IProject project = (IProject) resourceToDelete;
			project.delete(deleteContent, force, monitor);
		}
		else {
			// if it's not a project, just delete it
			resourceToDelete.delete(IResource.KEEP_HISTORY, monitor);
		}
	}
	catch (CoreException exception) {
		if (resourceToDelete.getType() == IResource.FILE) {
			IStatus[] children = exception.getStatus().getChildren();
					
			if (children.length == 1 && 
				children[0].getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
				if (forceOutOfSyncDelete) {
					resourceToDelete.delete(IResource.KEEP_HISTORY | IResource.FORCE, monitor);
				}
				else {
					int result = queryDeleteOutOfSync(resourceToDelete);
			
					if (result == IDialogConstants.YES_ID) {
						resourceToDelete.delete(IResource.KEEP_HISTORY | IResource.FORCE, monitor);
					}
					else
					if (result == IDialogConstants.YES_TO_ALL_ID) {
						forceOutOfSyncDelete = true;
						resourceToDelete.delete(IResource.KEEP_HISTORY | IResource.FORCE, monitor);
					}
					else
					if (result == IDialogConstants.CANCEL_ID) {
						throw new OperationCanceledException();							
					}
				}
			}
			else {
				throw exception;
			}
		}
		else {
			throw exception;
		}
	}
}
/**
 *	Return an array of the currently selected resources.
 *
 * @return the list of selected resources
 */
IResource[] getSelectedResourcesArray() {
	List selection = getSelectedResources();
	IResource[] resources = new IResource[selection.size()];
	int i = 0;
	for (Iterator e = selection.iterator(); e.hasNext();)
		resources[i++] = (IResource) e.next();
	return resources;
}
/**
 * Returns a bit-mask containing the types of resources in the selection.
 */
int getSelectedResourceTypes() {
	int types = 0;
	for (Iterator i = getSelectedResources().iterator(); i.hasNext();) {
		IResource r = (IResource) i.next();
		types |= r.getType();
	}
	return types;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	if (!confirmDelete())
		return;
	final IResource[] resourcesToDelete = getResourcesToDelete();
	
	if (resourcesToDelete.length == 0)
		return;
	try {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				delete(resourcesToDelete, monitor);
			}
		};
		new ProgressMonitorDialog(shell).run(true, true, op);
	} catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		if (t instanceof CoreException) {
			CoreException exception = (CoreException) t;
			IStatus status = exception.getStatus();
			IStatus[] children = status.getChildren();
			boolean outOfSyncError = false;

			for (int i = 0; i < children.length; i++) {
				if (children[i].getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
					outOfSyncError = true;
					break;
				}
			}
			if (outOfSyncError) {
				ErrorDialog.openError(
					shell, 
					WorkbenchMessages.getString("DeleteResourceAction.errorTitle"), 	//$NON-NLS-1$
					WorkbenchMessages.getString("DeleteResourceAction.outOfSyncError"),	//$NON-NLS-1$
					status);
			} 
			else {
				ErrorDialog.openError(
					shell, 
					WorkbenchMessages.getString("DeleteResourceAction.errorTitle"), // no special message //$NON-NLS-1$
					null, status);
			}
		} 
		else {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log(MessageFormat.format("Exception in {0}.run: {1}", new Object[] {getClass().getName(), t}));//$NON-NLS-1$
			MessageDialog.openError(
				shell,
				WorkbenchMessages.getString("DeleteResourceAction.messageTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("DeleteResourceAction.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
		}
	} catch (InterruptedException e) {
		// just return
	}
}
/**
 * Returns the resources to delete based on the selection and their read-only status.
 * 
 * @return the resources to delete
 */
IResource[] getResourcesToDelete() {
	IResource[] selectedResources = getSelectedResourcesArray();

	if (containsOnlyProjects()  && !deleteContent) {
		// We can just return the selection
		return selectedResources;
	} 

	ReadOnlyStateChecker checker =
		new ReadOnlyStateChecker(
			this.shell,
			WorkbenchMessages.getString("DeleteResourceAction.title1"), //$NON-NLS-1$
			WorkbenchMessages.getString("DeleteResourceAction.readOnlyQuestion")); //$NON-NLS-1$
	
	return checker.checkReadOnlyResources(selectedResources);
}
/**
 * The <code>DeleteResourceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method disables the action
 * if the selection contains phantom resources or non-resources
 */
protected boolean updateSelection(IStructuredSelection selection) {
	return super.updateSelection(selection) && canDelete();
}
	
/**
 * Ask the user whether the given resource should be deleted
 * despite being out of sync with the file system.
 * @param resource the out of sync resource
 * @return One of the IDialogConstants constants indicating which
 * 	of the Yes, Yes to All, No, Cancel options has been selected by 
 * 	the user.
 */
private int queryDeleteOutOfSync(IResource resource) {
	final MessageDialog dialog =
		new MessageDialog(
			shell,
			WorkbenchMessages.getString("DeleteResourceAction.messageTitle"),	//$NON-NLS-1$		
			null,
			WorkbenchMessages.format("DeleteResourceAction.outOfSyncQuestion", new Object[] {resource.getName()}),	//$NON-NLS-1$
			MessageDialog.QUESTION,
			new String[] {
				IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL,
				IDialogConstants.CANCEL_LABEL },
			0);
	shell.getDisplay().syncExec(new Runnable() {
		public void run() {
			dialog.open();
		}
	});
	int result = dialog.getReturnCode();
	if (result == 0)
		return IDialogConstants.YES_ID;
	if (result == 1)
		return IDialogConstants.YES_TO_ALL_ID;
	if (result == 2)
		return IDialogConstants.NO_ID;
	return IDialogConstants.CANCEL_ID;
}
}
