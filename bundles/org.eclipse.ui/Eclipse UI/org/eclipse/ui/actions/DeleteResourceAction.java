package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.text.MessageFormat;

/**
 * Standard action for deleting the currently selected resources.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DeleteResourceAction extends SelectionListenerAction {

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
	private boolean deleteContent;
/**
 * Creates a new delete resource action.
 *
 * @param shell the shell for any dialogs
 */
public DeleteResourceAction(Shell shell) {
	super(WorkbenchMessages.getString("DeleteResourceAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("DeleteResourceAction.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.DELETE_RESOURCE_ACTION});
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
	if (getSelectedNonResources().size() > 0) return false;
	int types = getSelectedResourceTypes();
	// allow only projects or only non-projects to be selected; 
	// note that the selection may contain multiple types of resource
	if (!(types == IResource.PROJECT || (types & IResource.PROJECT) == 0)) {
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
 * Asks the user to confirm a delete operation.
 *
 * @return <code>true</code> if the user says to go ahead, and <code>false</code>
 *  if the deletion should be abandoned
 */
boolean confirmDelete() {
	if ((getSelectedResourceTypes() & IResource.PROJECT) != 0) {
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
	String title = WorkbenchMessages.getString("DeleteResourceAction.title");  //$NON-NLS-1$
	String msg;
	if (resources.size() == 1) {
		IResource resource = (IResource) resources.get(0);
		msg = WorkbenchMessages.format("DeleteResourceAction.confirm1", new Object[] { resource.getName() });  //$NON-NLS-1$
	}
	else {
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
	String title = WorkbenchMessages.getString("DeleteResourceAction.titleProject"); //$NON-NLS-1$
	List resources = getSelectedResources();
	String msg;
	if (resources.size() == 1) {
		IProject project = (IProject) resources.get(0);
		msg = WorkbenchMessages.format("DeleteResourceAction.confirmProject1", new Object[] { project.getName(), project.getLocation().toOSString() });  //$NON-NLS-1$
	}
	else {
		msg = WorkbenchMessages.format("DeleteResourceAction.confirmProjectN", new Object[] { new Integer(resources.size()) });  //$NON-NLS-1$
	}
	MessageDialog dialog = new MessageDialog(
		shell,
		title, 
		null,	// accept the default window icon
		msg, 
		MessageDialog.QUESTION, 
		new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL},
		0); 	// yes is the default
	int code = dialog.open();
	switch (code) {
		case 0: // YES
			deleteContent = true;
			return true;
		case 1: // NO
			deleteContent = false;
			return true;
		default: // CANCEL and close dialog
			return false;
	}
}
/**
 * Deletes the given resources.
 */
void delete(IResource[] resourcesToDelete, IProgressMonitor monitor) throws CoreException {
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
	if (resourceToDelete.getType() == IResource.PROJECT) {
		// if it's a project, ask whether content should be deleted too
		IProject project = (IProject) resourceToDelete;
		project.delete(deleteContent, force, monitor);
	}
	else {
		// if it's not a project, just delete it
		resourceToDelete.delete(force, monitor);
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

	ReadOnlyStateChecker checker =
		new ReadOnlyStateChecker(
			this.shell,
			WorkbenchMessages.getString("DeleteResourceAction.checkDelete"), //$NON-NLS-1$
			WorkbenchMessages.getString("DeleteResourceAction.readOnlyQuestion")); //$NON-NLS-1$

	final IResource[] resourcesToDelete =
		checker.checkReadOnlyResources(getSelectedResourcesArray());

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
			ErrorDialog.openError(shell, WorkbenchMessages.getString("DeleteResourceAction.errorTitle"), null, // no special message //$NON-NLS-1$
			 ((CoreException) t).getStatus());
		} else {
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
 * The <code>DeleteResourceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method disables the action
 * if the selection contains phantom resources or non-resources
 */
protected boolean updateSelection(IStructuredSelection selection) {
	return super.updateSelection(selection) && canDelete();
}
}
