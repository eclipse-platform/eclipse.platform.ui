package org.eclipse.ui.actions;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;

/**
 * The MoveProjectAction is the action designed to move projects specifically
 * as they have different semantics from other resources.
 */
public class MoveProjectAction extends CopyProjectAction {
	private static String MOVE_TOOL_TIP = "Move the project";
	private static String MOVE_TITLE = "Mo&ve";
	private static String PROBLEMS_TITLE = "Move Problems";
	private static String MOVE_PROGRESS_TITLE = "Moving:";

	
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveProjectAction";

/**
 * Creates a new project move action with the given text.
 *
 * @param shell the shell for any dialogs
 */
public MoveProjectAction(Shell shell) {
	super(shell,MOVE_TITLE);
	setToolTipText(MOVE_TOOL_TIP);
	setId(MoveProjectAction.ID);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.MOVE_PROJECT_ACTION});
}
/**
 * Return the title of the errors dialog.
 * @return java.lang.String
 */
protected String getErrorsTitle() {
	return PROBLEMS_TITLE;
}
/**
 * Moves the project to the new values.
 *
 * @param project the project to copy
 * @param projectName the name of the copy
 * @param newLocation IPath
 * @return <code>true</code> if the copy operation completed, and 
 *   <code>false</code> if it was abandoned part way
 */
boolean performMove(
	final IProject project,
	final String projectName,
	final IPath newLocation) {
	WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
		public void execute(IProgressMonitor monitor) {

			monitor.beginTask(MOVE_PROGRESS_TITLE, 100);
			try {
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				IWorkspace workspace = project.getWorkspace();

				//Get a copy of the current description and modify it
				IProjectDescription newDescription =
					createDescription(project, projectName, newLocation);

				monitor.worked(50);

				project.move(newDescription, true, monitor);

				monitor.worked(50);

			} catch (CoreException e) {
				recordError(e); // log error
			} finally {
				monitor.done();
			}
		}
	};

	try {
		new ProgressMonitorDialog(shell).run(true, true, op);
	} catch (InterruptedException e) {
		return false;
	} catch (InvocationTargetException e) {
		// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
		WorkbenchPlugin.log(
			"Exception in "
				+ getClass().getName()
				+ ".performMove(): "
				+ e.getTargetException());
		displayError("Internal error: " + e.getTargetException().getMessage());
		return false;
	}

	return true;
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {

	IProject project = (IProject) getSelectedResources().get(0);

	//Get the project name and location in a two element list
	Object[] destinationPaths = queryDestinationParameters(project);
	if (destinationPaths == null)
		return;

	String projectName = (String) destinationPaths[0];
	IPath newLocation = new Path((String) destinationPaths[1]);

	boolean completed = performMove(project, projectName, newLocation);

	if (!completed) // ie.- canceled
		return; // not appropriate to show errors

	// If errors occurred, open an Error dialog
	if (errorStatus != null) {
		ErrorDialog.openError(this.shell, PROBLEMS_TITLE, null, errorStatus);
	}
}
}
