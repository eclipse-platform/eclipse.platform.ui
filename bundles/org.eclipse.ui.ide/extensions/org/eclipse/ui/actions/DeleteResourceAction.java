/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

/**
 * Standard action for deleting the currently selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DeleteResourceAction extends SelectionListenerAction {

	static class DeleteProjectDialog extends MessageDialog {

		private IResource[] projects;
		private boolean deleteContent = false;
		private Button radio1;
		private Button radio2;
		
		DeleteProjectDialog(Shell parentShell, IResource[] projects) {
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
		
		static String getTitle(IResource[] projects) {
			if (projects.length == 1)
				return IDEWorkbenchMessages.getString("DeleteResourceAction.titleProject1");  //$NON-NLS-1$
			else
				return IDEWorkbenchMessages.getString("DeleteResourceAction.titleProjectN");  //$NON-NLS-1$
		}
		
		static String getMessage(IResource[] projects) {
			if (projects.length == 1) {
				IProject project = (IProject) projects[0];
				return IDEWorkbenchMessages.format("DeleteResourceAction.confirmProject1", new Object[] { project.getName() });  //$NON-NLS-1$
			}
			else {
				return IDEWorkbenchMessages.format("DeleteResourceAction.confirmProjectN", new Object[] { new Integer(projects.length) });  //$NON-NLS-1$
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
			if (projects.length == 1) {
				IProject project = (IProject) projects[0];
				if(project == null || project.getLocation() == null)
					text1 = IDEWorkbenchMessages.getString("DeleteResourceAction.deleteContentsN");  //$NON-NLS-1$
				else
					text1 = IDEWorkbenchMessages.format("DeleteResourceAction.deleteContents1", new Object[] { project.getLocation().toOSString() });  //$NON-NLS-1$
			} else {
				text1 = IDEWorkbenchMessages.getString("DeleteResourceAction.deleteContentsN");  //$NON-NLS-1$
			}
			radio1.setText(text1);
			radio1.setFont(parent.getFont());

			radio2 = new Button(composite, SWT.RADIO);
			radio2.addSelectionListener(selectionListener);
			String text2 = IDEWorkbenchMessages.getString("DeleteResourceAction.doNotDeleteContents");  //$NON-NLS-1$
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
	super(IDEWorkbenchMessages.getString("DeleteResourceAction.text")); //$NON-NLS-1$
	setToolTipText(IDEWorkbenchMessages.getString("DeleteResourceAction.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.DELETE_RESOURCE_ACTION);
	setId(ID);
	if (shell == null) {
		throw new IllegalArgumentException();
	}
	this.shell = shell;
}
/**
 * Returns whether delete can be performed on the current selection.
 *
 * @param resources the selected resources
 * @return <code>true</code> if the resources can be deleted, and 
 *  <code>false</code> if the selection contains non-resources or phantom
 *  resources
 */
private boolean canDelete(IResource[] resources) {
	// allow only projects or only non-projects to be selected; 
	// note that the selection may contain multiple types of resource
	if (!(containsOnlyProjects(resources) || containsOnlyNonProjects(resources))) {
		return false;
	}
	
	if (resources.length == 0) return false;	
	// Return true if everything in the selection exists.
	for (int i = 0; i < resources.length; i++) {
	    IResource resource = resources[i];
		if (resource.isPhantom()) {
			return false;
		}
	}
	return true;
}
/**
 * Returns whether the selection contains linked resources.
 *
 * @param resources the selected resources
 * @return <code>true</code> if the resources contain linked   
 *  resources, and <code>false</code> otherwise
 */
private boolean containsLinkedResource(IResource[] resources) {
    for (int i = 0; i < resources.length; i++) {
        IResource resource = resources[i];
		if (resource.isLinked()) return true;
	}
	return false;
}
/**
 * Returns whether the selection contains only non-projects.
 *
 * @param resources the selected resources
 * @return <code>true</code> if the resources contains only non-projects, and 
 *  <code>false</code> otherwise
 */
private boolean containsOnlyNonProjects(IResource[] resources) {
	int types = getSelectedResourceTypes(resources);
	// check for empty selection
	if (types == 0) return false;
	// note that the selection may contain multiple types of resource
	return (types & IResource.PROJECT) == 0;
}
/**
 * Returns whether the selection contains only projects.
 * 
 * @param resources the selected resources
 * @return <code>true</code> if the resources contains only projects, and 
 *  <code>false</code> otherwise
 */
private boolean containsOnlyProjects(IResource[] resources) {
	int types = getSelectedResourceTypes(resources);
	// note that the selection may contain multiple types of resource
	return types == IResource.PROJECT;
}

/**
 * Asks the user to confirm a delete operation.
 *
 * @param resources the selected resources
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
private boolean confirmDelete(IResource[] resources) {
	if (containsOnlyProjects(resources)) {
		return confirmDeleteProjects(resources);
	}
	else {
		return confirmDeleteNonProjects(resources);
	}
}
/**
 * Asks the user to confirm a delete operation,
 * where the selection contains no projects.
 *
 * @param resources the selected resources
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
private boolean confirmDeleteNonProjects(IResource[] resources) {
	String title;
	String msg;
	if (resources.length == 1) {
		title = IDEWorkbenchMessages.getString("DeleteResourceAction.title1");  //$NON-NLS-1$
 		IResource resource = resources[0];
 		if (resource.isLinked())
 			msg = IDEWorkbenchMessages.format("DeleteResourceAction.confirmLinkedResource1", new Object[] { resource.getName() });  //$NON-NLS-1$
 		else
			msg = IDEWorkbenchMessages.format("DeleteResourceAction.confirm1", new Object[] { resource.getName() });  //$NON-NLS-1$
	}
	else {
		title = IDEWorkbenchMessages.getString("DeleteResourceAction.titleN");  //$NON-NLS-1$
		if (containsLinkedResource(resources))
			msg = IDEWorkbenchMessages.format("DeleteResourceAction.confirmLinkedResourceN", new Object[] { new Integer(resources.length) });  //$NON-NLS-1$
		else
			msg = IDEWorkbenchMessages.format("DeleteResourceAction.confirmN", new Object[] { new Integer(resources.length) });  //$NON-NLS-1$
	}
	return MessageDialog.openQuestion(shell, title, msg);
}
/**
 * Asks the user to confirm a delete operation,
 * where the selection contains only projects.
 * Also remembers whether project content should be deleted.
 *
 * @param resources the selected resources
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
private boolean confirmDeleteProjects(IResource[] resources) {
	DeleteProjectDialog dialog = new DeleteProjectDialog(shell, resources);
	int code = dialog.open();
	deleteContent = dialog.getDeleteContent();
	return code == 0;  // YES
}
/**
 * Deletes the given resources.
 */
private void delete(IResource[] resourcesToDelete, IProgressMonitor monitor) throws CoreException {
    final List exceptions = new ArrayList();
	forceOutOfSyncDelete = false;
	monitor.beginTask("", resourcesToDelete.length); //$NON-NLS-1$
	for (int i = 0; i < resourcesToDelete.length; ++i) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		try {
		    delete(resourcesToDelete[i], new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
		} catch (CoreException e) {
		    exceptions.add(e);
		}
	}
	
	// Check to see if any problems occurred during processing.
	final int exceptionCount = exceptions.size();
	if (exceptionCount == 1) {
	    throw (CoreException) exceptions.get(0);
	} else if (exceptionCount > 1) {
	    final MultiStatus multi = new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, 0, IDEWorkbenchMessages.getString("DeleteResourceAction.deletionExceptionMessage"), new Exception()); //$NON-NLS-1$
	    for (int i = 0; i < exceptionCount; i++) {
	        CoreException exception = (CoreException) exceptions.get(0);
	        IStatus status = exception.getStatus();
	        multi.add(new Status(status.getSeverity(), status.getPlugin(), status.getCode(), status.getMessage(), exception));
	    }
	    throw new CoreException(multi);
	}
	
	// Signal that the job has completed successfully.
	monitor.done();
}
/**
 * Deletes the given resource.
 */
private void delete(IResource resourceToDelete, IProgressMonitor monitor) throws CoreException {
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
 * Return an array of the currently selected resources.
 *
 * @return the selected resources
 */
private IResource[] getSelectedResourcesArray() {
	List selection = getSelectedResources();
	IResource[] resources = new IResource[selection.size()];
	selection.toArray(resources);
	return resources;
}
/**
 * Returns a bit-mask containing the types of resources in the selection.
 * 
 * @param resources the selected resources
 */
private int getSelectedResourceTypes(IResource[] resources) {
	int types = 0;
	for (int i = 0; i < resources.length; i++) {
		types |= resources[i].getType();
	}
	return types;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	IResource[] resources = getSelectedResourcesArray();
	// WARNING: do not query the selected resources more than once
	// since the selection may change during the run, 
	// e.g. due to window activation when the prompt dialog is dismissed.
	// For more details, see Bug 60606 [Navigator] (data loss) Navigator deletes/moves the wrong file
	if (!confirmDelete(resources))
		return;
	final IResource[] resourcesToDelete = getResourcesToDelete(resources);
	
	if (resourcesToDelete.length == 0)
		return;
	try {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				delete(resourcesToDelete, monitor);
			}
		};
		new ProgressMonitorJobsDialog(shell).run(true, true, op);
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
			IDEWorkbenchPlugin.log(MessageFormat.format("Exception in {0}.run: {1}", new Object[] {getClass().getName(), t}), status);//$NON-NLS-1$
			if (outOfSyncError) {
				ErrorDialog.openError(
					shell, 
					IDEWorkbenchMessages.getString("DeleteResourceAction.errorTitle"), 	//$NON-NLS-1$
					IDEWorkbenchMessages.getString("DeleteResourceAction.outOfSyncError"),	//$NON-NLS-1$
					status);
			} 
			else {
				ErrorDialog.openError(
					shell, 
					IDEWorkbenchMessages.getString("DeleteResourceAction.errorTitle"), // no special message //$NON-NLS-1$
					null, status);
			}
		} 
		else {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			IDEWorkbenchPlugin.log(MessageFormat.format("Exception in {0}.run: {1}", new Object[] {getClass().getName(), t}));//$NON-NLS-1$
			MessageDialog.openError(
				shell,
				IDEWorkbenchMessages.getString("DeleteResourceAction.messageTitle"), //$NON-NLS-1$
				IDEWorkbenchMessages.format("DeleteResourceAction.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
		}
	} catch (InterruptedException e) {
		// just return
	}
}
/**
 * Returns the resources to delete based on the selection and their read-only status.
 * 
 * @param resources the selected resources
 * @return the resources to delete
 */
private IResource[] getResourcesToDelete(IResource[] resources) {

	if (containsOnlyProjects(resources)  && !deleteContent) {
		// We can just return the selection
		return resources;
	} 

	ReadOnlyStateChecker checker =
		new ReadOnlyStateChecker(
			this.shell,
			IDEWorkbenchMessages.getString("DeleteResourceAction.title1"), //$NON-NLS-1$
			IDEWorkbenchMessages.getString("DeleteResourceAction.readOnlyQuestion")); //$NON-NLS-1$
	
	return checker.checkReadOnlyResources(resources);
}
/**
 * The <code>DeleteResourceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method disables the action
 * if the selection contains phantom resources or non-resources
 */
protected boolean updateSelection(IStructuredSelection selection) {
	return super.updateSelection(selection) && canDelete(getSelectedResourcesArray());
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
			IDEWorkbenchMessages.getString("DeleteResourceAction.messageTitle"),	//$NON-NLS-1$		
			null,
			IDEWorkbenchMessages.format("DeleteResourceAction.outOfSyncQuestion", new Object[] {resource.getName()}),	//$NON-NLS-1$
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
