/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationMoveDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

/**
 * The MoveProjectAction is the action designed to move projects specifically as
 * they have different semantics from other resources.
 */
public class MoveProjectAction extends CopyProjectAction {
	private static String MOVE_TOOL_TIP = IDEWorkbenchMessages.MoveProjectAction_toolTip;

	private static String MOVE_TITLE = IDEWorkbenchMessages.MoveProjectAction_text;

	private static String PROBLEMS_TITLE = IDEWorkbenchMessages.MoveProjectAction_dialogTitle;

	private static String MOVE_PROGRESS_TITLE = IDEWorkbenchMessages.MoveProjectAction_progressMessage;

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveProjectAction";//$NON-NLS-1$

	/**
	 * Creates a new project move action with the given text.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 */
	public MoveProjectAction(Shell shell) {
		super(shell, MOVE_TITLE);
		setToolTipText(MOVE_TOOL_TIP);
		setId(MoveProjectAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.MOVE_PROJECT_ACTION);
	}

	/**
	 * Return the title of the errors dialog.
	 * 
	 * @return java.lang.String
	 */
	protected String getErrorsTitle() {
		return PROBLEMS_TITLE;
	}

	/**
	 * Moves the project to the new values.
	 * 
	 * @param project
	 *            the project to copy
	 * @param projectName
	 *            the name of the copy
	 * @param newLocation
	 *            IPath
	 * @return <code>true</code> if the copy operation completed, and
	 *         <code>false</code> if it was abandoned part way
	 */
	boolean performMove(final IProject project, final String projectName,
			final IPath newLocation) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
				try {
					if (monitor.isCanceled())
						throw new OperationCanceledException();
					
					monitor.setTaskName(MOVE_PROGRESS_TITLE);
					//Get a copy of the current description and modify it
					IProjectDescription newDescription = createDescription(
							project, projectName, newLocation);

					project.move(newDescription, IResource.FORCE
							| IResource.SHALLOW, monitor);

				} catch (CoreException e) {
					recordError(e); // log error
				} finally {
					monitor.done();
				}
			}
		};

		try {
			new ProgressMonitorJobsDialog(shell).run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected above, but unexpected runtime
			// exceptions and errors may still occur.
			IDEWorkbenchPlugin.log(getClass(),
                    "performMove()", e.getTargetException()); //$NON-NLS-1$
			displayError(NLS.bind(IDEWorkbenchMessages.MoveProjectAction_internalError, e.getTargetException().getMessage()));
			return false;
		}

		return true;
	}

	/**
	 * Query for a new project destination using the parameters in the existing
	 * project.
	 * 
	 * @return Object[] or null if the selection is cancelled
	 * @param project
	 *            the project we are going to move.
	 */
	protected Object[] queryDestinationParameters(IProject project) {
		ProjectLocationMoveDialog dialog = new ProjectLocationMoveDialog(shell,
				project);
		dialog.setTitle(IDEWorkbenchMessages.MoveProjectAction_moveTitle);
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

		if (!validateMove(project, projectName))
			return;
		
		boolean completed = performMove(project, projectName, newLocation);

		if (!completed) // ie.- canceled
			return; // not appropriate to show errors

		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
			ErrorDialog
					.openError(this.shell, PROBLEMS_TITLE, null, errorStatus);
			errorStatus = null;
		}
	}
	
	/**
	 * Validates the operation against the model providers.
	 *
	 * @param project the project to move
	 * @param newName the new name
	 * @return whether the operation should proceed
	 * @since 3.2
	 */
    private boolean validateMove(IProject project, String newName) {
    	if (project.getName().equals(newName)) {
    		// Only the location changed
    		return true;
    	}
    	IResourceChangeDescriptionFactory factory = ResourceChangeValidator.getValidator().createDeltaFactory();
    	factory.move(project, new Path(newName));
		return IDE.promptToConfirm(shell, IDEWorkbenchMessages.CopyProjectAction_confirm, NLS.bind(IDEWorkbenchMessages.CopyProjectAction_warning, project.getName()), factory.getDelta(), getModelProviderIds(), false /* no need to syncExec */);
	}
}
