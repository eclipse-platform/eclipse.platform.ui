package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
 * Creates a new delete resource action.
 *
 * @param shell the shell for any dialogs
 */
public DeleteResourceAction(Shell shell) {
	super(WorkbenchMessages.getString("DeleteAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("DeleteAction.toolTip")); //$NON-NLS-1$
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
		MessageDialog.openQuestion(shell,
		WorkbenchMessages.getString("Question"),  //$NON-NLS-1$
		WorkbenchMessages.getString("DeleteAction.confirmDelete")); //$NON-NLS-1$
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
			WorkbenchMessages.getString("DeleteResource.checkDelete"), //$NON-NLS-1$
			WorkbenchMessages.getString("DeleteResource.readOnlyQuestion")); //$NON-NLS-1$

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
			ErrorDialog.openError(shell, WorkbenchMessages.getString("DeleteAction.errorTitle"), null, // no special message //$NON-NLS-1$
			 ((CoreException) t).getStatus());
		} else {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log(MessageFormat.format("Exception in {0}.run: {1}", new Object[] {getClass().getName(), t}));//$NON-NLS-1$
			MessageDialog.openError(
				shell,
				WorkbenchMessages.getString("DeleteAction.messageTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("DeleteResourceAction.internalError", new Object[] {t.getMessage()})); //$NON-NLS-1$
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
