package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import java.util.Iterator;

/**
 * Standard action for opening the currently selected project(s).
 * <p>
 * Note that there is a different action for opening an editor on file resources:
 * <code>OpenFileAction</code>.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenResourceAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenResourceAction";
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public OpenResourceAction(Shell shell) {
	super(shell, "&Open Project");
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.OPEN_RESOURCE_ACTION});
	setToolTipText("Open Project");
	setId(ID);
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
	return "Problems occurred opening the selected resources.";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return "Open Problems";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
	((IProject)resource).open(monitor);
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
boolean shouldPerformResourcePruning() {
	return false;
}
/**
 * The <code>OpenResourceAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * enabled only if one of the selections is a closed project.
 */
protected boolean updateSelection(IStructuredSelection s) {
	// don't call super since we want to enable if closed project is selected.
	
	if (!selectionIsOfType(IResource.PROJECT))
		return false;

	Iterator resources = s.iterator();
	while (resources.hasNext()) {
		IProject currentResource = (IProject)resources.next();
		if (!currentResource.isOpen()) {
			return true;
		}
	}
	return false;
}
}
