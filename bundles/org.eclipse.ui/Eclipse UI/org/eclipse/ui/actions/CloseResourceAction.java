package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
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
	public static final String ID = PlatformUI.PLUGIN_ID + ".CloseResourceAction";//$NON-NLS-1$
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public CloseResourceAction(Shell shell) {
	super(shell, WorkbenchMessages.getString("CloseResourceAction.text")); //$NON-NLS-1$
	setId(ID);
	setToolTipText(WorkbenchMessages.getString("CloseResourceAction.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.CLOSE_RESOURCE_ACTION});
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return ""; //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return WorkbenchMessages.getString("CloseResourceAction.problemMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return WorkbenchMessages.getString("CloseResourceAction.title"); //$NON-NLS-1$
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
