package org.eclipse.ui.actions;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;

/**
 * Standard action for refreshing the workspace from the local file system for
 * the selected resources and all of their descendents.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class RefreshAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".RefreshAction";
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public RefreshAction(Shell shell) {
	super(shell, "Re&fresh From Local");
	setToolTipText("Refresh the resource from the local copy");
	setId(ID);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.REFRESH_ACTION});
}
/**
 * Checks whether the given project's location has been deleted.
 * If so, prompts the user with whether to delete the project or not.
 */
void checkLocationDeleted(IProject project) throws CoreException {
	if (!project.exists())
		return;
	File location = project.getLocation().toFile();
	if (!location.exists()) {
		String message = 
			"Project \"" + project.getName() + "\"'s location in the file system (" + location.getAbsolutePath() + ") has been deleted.\n"
		  + "Delete project from workspace?";
		final MessageDialog dialog = new MessageDialog(
			getShell(),
			"Project location has been deleted", // dialog title
			null, // use default window icon
			message,
			MessageDialog.QUESTION,
			new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
			0); // yes is the default

		// Must prompt user in UI thread (we're in the operation thread here).
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});

		// Do the deletion back in the operation thread
		if (dialog.getReturnCode() == 0) { // yes was chosen
			project.delete(true, true, null);
		}
	}
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getOperationMessage() {
	return "Refreshing:";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return "Problems occurred refreshing the selected resources.";
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return "Refresh Problems";
}
/**
 * Returns a list containing the workspace root if the selection would otherwise be empty.
 */
protected List getSelectedResources() {
	List resources = super.getSelectedResources();
	if (resources.isEmpty()) {
		resources = new ArrayList();
		resources.add(ResourcesPlugin.getWorkspace().getRoot());
	}
	return resources;
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
	resource.refreshLocal(IResource.DEPTH_INFINITE,monitor);
	// Check if project's location has been deleted, 
	// as per 1G83UCE: ITPUI:WINNT - Refresh from local doesn't detect new or deleted projects
	if (resource.getType() == IResource.PROJECT) {
		checkLocationDeleted((IProject) resource);
	}
}
/**
 * The <code>RefreshAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * disabled if any of the selections are not resources.
 */
protected boolean updateSelection(IStructuredSelection s) {
	return (super.updateSelection(s) || s.isEmpty()) && getSelectedNonResources().size() == 0;
}
}
