/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationMoveDialog;
import org.eclipse.ui.ide.undo.MoveProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
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

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".MoveProjectAction";//$NON-NLS-1$

	/**
	 * Creates a new project move action and initializes it.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 *  
	 * @deprecated {@link #MoveProjectAction(IShellProvider)}
	 */
	public MoveProjectAction(Shell shell) {
		super(shell, MOVE_TITLE);
		initAction();
	}
	
	/**
	 * Creates a new project move action and initializes it.
	 * @param provider
	 * 				the IShellProvider for any dialogs
	 * @since 3.4
	 */
	public MoveProjectAction(IShellProvider provider){
		super(provider, MOVE_TITLE);
		initAction();
	}

	private void initAction(){
		setToolTipText(MOVE_TOOL_TIP);
		setId(MoveProjectAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.MOVE_PROJECT_ACTION);
	}
	/**
	 * Return the title of the errors dialog.
	 * 
	 * @return java.lang.String
	 * 
	 * @deprecated As of 3.3, the error handling is performed by the undoable 
	 * operation which handles the move.
	 */
	protected String getErrorsTitle() {
		return PROBLEMS_TITLE;
	}

	/**
	 * Moves the project to the new values.
	 * 
	 * @param project
	 *            the project to move
	 * @param newLocation
	 *            URI
	 * @return <code>true</code> if the copy operation completed, and
	 *         <code>false</code> if it was abandoned part way
	 */
	boolean performMove(final IProject project, 
			final URI newLocation) {
		
		IRunnableWithProgress op =  new IRunnableWithProgress() {
    		public void run(IProgressMonitor monitor) {
    			MoveProjectOperation op = new MoveProjectOperation(project, newLocation, IDEWorkbenchMessages.MoveProjectAction_moveTitle);
    			op.setModelProviderIds(getModelProviderIds());
    			try {
    				PlatformUI.getWorkbench().getOperationSupport()
    						.getOperationHistory().execute(op, monitor, 
    								WorkspaceUndoUtil.getUIInfoAdapter(shellProvider.getShell()));
    			} catch (ExecutionException e) {
					if (e.getCause() instanceof CoreException) {
						recordError((CoreException)e.getCause());
					} else {
						IDEWorkbenchPlugin.log(e.getMessage(), e);
						displayError(e.getMessage());
					}
    			}
    		}
    	};
		
		try {
			new ProgressMonitorJobsDialog(shellProvider.getShell()).run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected by the operation, but unexpected runtime
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
		ProjectLocationMoveDialog dialog = new ProjectLocationMoveDialog(shellProvider.getShell(),
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

		//Get the project name and location 
		Object[] destinationPaths = queryDestinationParameters(project);
		if (destinationPaths == null) {
			return;
		}

		// Ideally we would have gotten the URI directly from the
		// ProjectLocationDialog, but for backward compatibility, we
		// use the raw string and map back to a URI.  
		URI newLocation = URIUtil.toURI((String)destinationPaths[1]);
		
		
		boolean completed = performMove(project, newLocation);

		if (!completed) {
			return; // not appropriate to show errors
		}

		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
			ErrorDialog
					.openError(this.shellProvider.getShell(), PROBLEMS_TITLE, null, errorStatus);
			errorStatus = null;
		}
	}
}
