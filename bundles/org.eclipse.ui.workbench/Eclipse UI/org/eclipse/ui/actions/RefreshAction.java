package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

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
	public static final String ID = PlatformUI.PLUGIN_ID + ".RefreshAction";//$NON-NLS-1$
/**
 * Creates a new action.
 *
 * @param shell the shell for any dialogs
 */
public RefreshAction(Shell shell) {
	super(shell, WorkbenchMessages.getString("RefreshAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("RefreshAction.toolTip")); //$NON-NLS-1$
	setId(ID);
	WorkbenchHelp.setHelp(this, IHelpContextIds.REFRESH_ACTION);
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
		String message = WorkbenchMessages.format("RefreshAction.locationDeletedMessage", new Object[] {project.getName(),location.getAbsolutePath()});//$NON-NLS-1$
		
		final MessageDialog dialog = new MessageDialog(
			getShell(),
			WorkbenchMessages.getString("RefreshAction.dialogTitle"), // dialog title //$NON-NLS-1$
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
	return WorkbenchMessages.getString("RefreshAction.progressMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsMessage() {
	return WorkbenchMessages.getString("RefreshAction.problemMessage"); //$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on WorkspaceAction.
 */
String getProblemsTitle() {
	return WorkbenchMessages.getString("RefreshAction.problemTitle"); //$NON-NLS-1$
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
	// Check if project's location has been deleted, 
	// as per 1G83UCE: ITPUI:WINNT - Refresh from local doesn't detect new or deleted projects
	// and also for bug report #18283
	if (resource.getType() == IResource.PROJECT) {
		checkLocationDeleted((IProject) resource);
	} else if (resource.getType() == IResource.ROOT) {
		IProject[] projects = ((IWorkspaceRoot)resource).getProjects();
		for (int i = 0; i < projects.length; i++) {
			checkLocationDeleted(projects[i]);
		}
	}
	resource.refreshLocal(IResource.DEPTH_INFINITE,monitor);
}
/**
 * The <code>RefreshAction</code> implementation of this
 * <code>SelectionListenerAction</code> method ensures that this action is
 * enabled if the selection is empty, but is disabled if any of the selected 
 * elements are not resources.
 */
protected boolean updateSelection(IStructuredSelection s) {
	return (super.updateSelection(s) || s.isEmpty()) && getSelectedNonResources().size() == 0;
}

/**
 * Handle the key release.
 */
public void handleKeyReleased(KeyEvent event) {

	if (event.keyCode == SWT.F5 && event.stateMask == 0) {
		refreshAll();
	}
}

/**
 * Refreshes the entire workspace.
 */
public void refreshAll() {
	IStructuredSelection currentSelection = getStructuredSelection();
	selectionChanged(StructuredSelection.EMPTY);
	run();
	selectionChanged(currentSelection);
}
}
