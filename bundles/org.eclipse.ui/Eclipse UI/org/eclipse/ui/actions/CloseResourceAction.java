package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

/**
 * Standard action for closing the currently selected project(s)
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CloseResourceAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CloseResourceAction";
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public CloseResourceAction(Shell shell) {
	super(shell, "&Close Project");
	setId(ID);
	setToolTipText("Close Project");
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.CLOSE_RESOURCE_ACTION});
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return "";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return "Problems occurred closing the selected resources.";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return "Close Problems";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
	((IProject)resource).close(monitor);
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
boolean shouldPerformResourcePruning() {
	return false;
}
/**
 * The <code>CloseResourceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * enabled only if all of the selected resources are projects.
 */
protected boolean updateSelection(IStructuredSelection s) {
	return super.updateSelection(s) && selectionIsOfType(IProject.PROJECT);
}
}
