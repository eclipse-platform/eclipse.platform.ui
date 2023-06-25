/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ProjectLocationSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

/**
 * Implementation class to perform the actual copying of project resources from
 * the clipboard when paste action is invoked.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CopyProjectOperation {

	/**
	 * Status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	private MultiStatus errorStatus;

	/**
	 * The parent shell used to show any dialogs.
	 */
	private Shell parentShell;

	private String[] modelProviderIds;

	/**
	 * Validates that the copy of the project will not have undesirable side
	 * effects.
	 *
	 * @param shell
	 *            a shell
	 * @param project
	 *            the project being copied
	 * @param newName
	 *            the new name of the project
	 * @param modelProviderIds
	 *            the model provider ids of models that are known to the client
	 *            (and can hence be ignored)
	 * @return whether the operation should proceed
	 * @since 3.2
	 * @deprecated As of 3.3, validation is performed in the undoable operation
	 *             executed by this operation.
	 */
	@Deprecated
	protected static boolean validateCopy(Shell shell, IProject project,
			String newName, String[] modelProviderIds) {
		IResourceChangeDescriptionFactory factory = ResourceChangeValidator
				.getValidator().createDeltaFactory();
		factory.copy(project, IPath.fromOSString(newName));
		return IDE.promptToConfirm(shell,
				IDEWorkbenchMessages.CopyProjectAction_confirm, NLS.bind(
						IDEWorkbenchMessages.CopyProjectAction_warning, project
								.getName()), factory.getDelta(),
				modelProviderIds, false /* no need to sync exec */);
	}

	/**
	 * Create a new operation initialized with a shell.
	 *
	 * @param shell
	 *            parent shell for error dialogs
	 */
	public CopyProjectOperation(Shell shell) {
		parentShell = shell;
	}

	/**
	 * Paste a copy of the project on the clipboard to the workspace.
	 *
	 * @param project
	 *            The project that is beign copied.
	 */
	public void copyProject(IProject project) {
		errorStatus = null;

		// Get the project name and location in a two element list
		ProjectLocationSelectionDialog dialog = new ProjectLocationSelectionDialog(
				parentShell, project);
		dialog.setTitle(IDEWorkbenchMessages.CopyProjectOperation_copyProject);
		if (dialog.open() != Window.OK) {
			return;
		}

		Object[] destinationPaths = dialog.getResult();
		if (destinationPaths == null) {
			return;
		}

		String newName = (String) destinationPaths[0];
		URI newLocation = URIUtil.toURI((String)destinationPaths[1]);

		boolean completed = performProjectCopy(project, newName, newLocation);

		if (!completed) {
			return; // not appropriate to show errors
		}

		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
			ErrorDialog.openError(parentShell,
					IDEWorkbenchMessages.CopyProjectOperation_copyFailedTitle,
					null, errorStatus);
			errorStatus = null;
		}
	}

	/**
	 * Copies the project to the new values.
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
	private boolean performProjectCopy(final IProject project,
			final String projectName, final URI newLocation) {
		IRunnableWithProgress op = monitor -> {
			org.eclipse.ui.ide.undo.CopyProjectOperation op1 = new org.eclipse.ui.ide.undo.CopyProjectOperation(
					project, projectName, newLocation,
					IDEWorkbenchMessages.CopyProjectOperation_copyProject);
			op1.setModelProviderIds(getModelProviderIds());
			try {
				// If we are Copying projects and their content, do not execute
				// the operation in the undo history, since it cannot be
				// properly restored, if it has modified or new files added to
				// it. Just execute it directly so it won't be added to the undo
				// history.
				op1.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(parentShell));
			} catch (final ExecutionException e) {
				if (e.getCause() instanceof CoreException) {
					recordError((CoreException)e.getCause());
				} else {
					throw new InvocationTargetException(e);
				}
			}
		};

		try {
			new ProgressMonitorJobsDialog(parentShell).run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			final String message = e.getTargetException().getMessage();
			parentShell.getDisplay().syncExec(() -> MessageDialog
					.openError(
							parentShell,
							IDEWorkbenchMessages.CopyProjectOperation_copyFailedTitle,
							NLS.bind(IDEWorkbenchMessages.CopyProjectOperation_internalError, message)));
			return false;
		}

		return true;
	}

	/**
	 * Records the core exception to be displayed to the user
	 * once the action is finished.
	 *
	 * @param error a <code>CoreException</code>
	 */
	private void recordError(CoreException error) {

		if (errorStatus == null) {
			errorStatus = new MultiStatus(
					PlatformUI.PLUGIN_ID,
					IStatus.ERROR,
					IDEWorkbenchMessages.CopyProjectOperation_copyFailedMessage,
					error);
		}

		errorStatus.merge(error.getStatus());
	}

	/**
	 * Returns the model provider ids that are known to the client
	 * that instantiated this operation.
	 *
	 * @return the model provider ids that are known to the client
	 * that instantiated this operation.
	 * @since 3.2
	 */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
	 * Sets the model provider ids that are known to the client that
	 * instantiated this operation. Any potential side effects reported by these
	 * models during validation will be ignored.
	 *
	 * @param modelProviderIds
	 *            the model providers known to the client who is using this
	 *            operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}
}
