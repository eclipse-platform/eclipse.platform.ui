/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

 
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationMoveDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
/**
 * The MoveProjectAction is the action designed to move projects specifically
 * as they have different semantics from other resources.
 */
public class MoveProjectAction extends CopyProjectAction {
	private static String MOVE_TOOL_TIP = WorkbenchMessages.getString("MoveProjectAction.toolTip"); //$NON-NLS-1$
	private static String MOVE_TITLE = WorkbenchMessages.getString("MoveProjectAction.text"); //$NON-NLS-1$
	private static String PROBLEMS_TITLE = WorkbenchMessages.getString("MoveProjectAction.dialogTitle"); //$NON-NLS-1$
	private static String MOVE_PROGRESS_TITLE = WorkbenchMessages.getString("MoveProjectAction.progressMessage"); //$NON-NLS-1$

	
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveProjectAction";//$NON-NLS-1$

/**
 * Creates a new project move action with the given text.
 *
 * @param shell the shell for any dialogs
 */
public MoveProjectAction(Shell shell) {
	super(shell,MOVE_TITLE);
	setToolTipText(MOVE_TOOL_TIP);
	setId(MoveProjectAction.ID);
	WorkbenchHelp.setHelp(this, IHelpContextIds.MOVE_PROJECT_ACTION);
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
				//Get a copy of the current description and modify it
				IProjectDescription newDescription =
					createDescription(project, projectName, newLocation);

				monitor.worked(50);

				project.move(newDescription, IResource.FORCE | IResource.SHALLOW, monitor);

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
			MessageFormat.format("Exception in {0}.performMove(): {1}", new Object[] {getClass().getName(),e.getTargetException()}));//$NON-NLS-1$
		displayError(WorkbenchMessages.format("MoveProjectAction.internalError", new Object[] {e.getTargetException().getMessage()})); //$NON-NLS-1$
		return false;
	}

	return true;
}
/**
 * Query for a new project destination using the parameters in the existing
 * project.
 * @return Object[]  or null if the selection is cancelled
 * @param IProject - the project we are going to move.
 */
protected Object[] queryDestinationParameters(IProject project) {
	ProjectLocationMoveDialog dialog =
		new ProjectLocationMoveDialog(shell, project);
	dialog.setTitle(WorkbenchMessages.getString("MoveProjectAction.moveTitle")); //$NON-NLS-1$
	dialog.open();
	return dialog.getResult();
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {

	errorStatus = null; 

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
		errorStatus = null; 
	}
}
}
