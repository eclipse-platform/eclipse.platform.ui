package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	public static final String ID = PlatformUI.PLUGIN_ID + ".DeleteResourceAction";

	public static final String CHECK_DELETION_TITLE = "Check Deletion";

	public static final String CHECK_DELETION_MESSAGE = " is read only. Do you still wish to delete it?";
	
	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;
/**
 * Creates a new delete resource action.
 *
 * @param shell the shell for any dialogs
 */
public DeleteResourceAction(Shell shell) {
	super("&Delete");
	setToolTipText("Delete the resource");
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
	return
		MessageDialog.openConfirm(shell,
		"Question", 
		"Delete the selected resources?");
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
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	if (!confirmDelete())
		return;

	ReadOnlyStateChecker checker =
		new ReadOnlyStateChecker(
			this.shell,
			CHECK_DELETION_TITLE,
			CHECK_DELETION_MESSAGE);

	final IResource[] resourcesToDelete =
		checker.checkReadOnlyResources(getSelectedResourcesArray());

	try {
		new ProgressMonitorDialog(
			shell).run(true, true, new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws CoreException {
				WorkbenchPlugin.getPluginWorkspace().delete(resourcesToDelete, false, monitor);
			}
		});
	} catch (InvocationTargetException e) {
		Throwable t = e.getTargetException();
		if (t instanceof CoreException) {
			ErrorDialog.openError(shell, "Delete Problems", null, // no special message
			 ((CoreException) t).getStatus());
		} else {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log("Exception in " + getClass().getName() + ".run: " + t);
			MessageDialog.openError(
				shell,
				"Problems deleting",
				"Internal error: " + t.getMessage());
		}
	} catch (InterruptedException e) {
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
