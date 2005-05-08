/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * An operation approver that rechecks the validity of a proposed undo or redo
 * operation using
 * {@link IAdvancedUndoableOperation#computeUndoableStatus(IProgressMonitor)} or
 * {@link IAdvancedUndoableOperation#computeRedoableStatus(IProgressMonitor)}.
 * Some complex operations do not compute their validity in canUndo() or
 * canRedo() because it is too time-consuming. To save time on complex
 * validations, the true validity is not determined until it is time to perform
 * the operation.
 * </p>
 * 
 * @since 3.1
 */
public class AdvancedValidationUserApprover implements IOperationApprover {

	private IUndoContext context;

	/**
	 * Create an AdvancedValidationUserApprover that performs advanced
	 * validations on proposed undo and redo operations for a given undo
	 * context.
	 * 
	 * @param context -
	 *            the undo context of operations in question.
	 */
	public AdvancedValidationUserApprover(IUndoContext context) {
		super();
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedRedoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedRedoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {
		return proceedWithOperation(operation, history, uiInfo, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedUndoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedUndoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {

		return proceedWithOperation(operation, history, uiInfo, true);
	}

	/*
	 * Determine whether the operation in question is still valid.
	 */
	private IStatus proceedWithOperation(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo, boolean undoing) {

		// return immediately if the operation is not relevant
		if (!operation.hasContext(context))
			return Status.OK_STATUS;

		// if the operation does not support advanced validation,
		// then we assume it is valid.
		if (!(operation instanceof IAdvancedUndoableOperation))
			return Status.OK_STATUS;

		// Compute the undoable or redoable status
		IStatus status;
		try {
			if (undoing)
				status = ((IAdvancedUndoableOperation) operation)
						.computeUndoableStatus(getProgressMonitor());
			else
				status = ((IAdvancedUndoableOperation) operation)
						.computeRedoableStatus(getProgressMonitor());
		} catch (OperationCanceledException e) {
			status = Status.CANCEL_STATUS;
		} catch (ExecutionException e) {
			status = IOperationHistory.OPERATION_INVALID_STATUS;
			reportException(e, uiInfo);
			// return immediately since error is already reported.
			return status;
		}

		// Report non-OK statuses to the user. In some cases, the user may
		// choose to proceed, and the returned status will be different than
		// what is reported.
		if (!status.isOK()) {
			status = reportAndInterpretStatus(status, uiInfo, operation,
					undoing);
		}

		// If the operation is still not OK, inform the history that the
		// operation has changed, since it was previously believed to be valid.
		// We rely here on the ability of an IAdvancedUndoableOperation to
		// correctly report canUndo() and canRedo() once the undoable and
		// redoable status have been computed.
		if (!status.isOK()) {
			history.operationChanged(operation);
		}
		return status;
	}

	/*
	 * Return the progress monitor that should be used for computing validity
	 * checks for undo and redo.
	 */
	private IProgressMonitor getProgressMonitor() {
		// temporary implementation
		return null;
	}

	/*
	 * Report the specified execution exception to the log and to the user.
	 */
	private void reportException(ExecutionException e, IAdaptable uiInfo) {
		Throwable nestedException = e.getCause();
		Throwable exception = (nestedException == null) ? e : nestedException;
		String title = WorkbenchMessages.Error;
		String message = WorkbenchMessages.WorkbenchWindow_exceptionMessage;
		String exceptionMessage = exception.getMessage();
		if (exceptionMessage == null) {
			exceptionMessage = message;
		}
		IStatus status = new Status(IStatus.ERROR,
				WorkbenchPlugin.PI_WORKBENCH, 0, exceptionMessage, exception);
		WorkbenchPlugin.log(message, status);

		boolean createdShell = false;
		Shell shell = getShell(uiInfo);
		if (shell == null) {
			createdShell = true;
			shell = new Shell();
		}
		ErrorDialog.openError(shell, title, message, status);
		if (createdShell)
			shell.dispose();
	}

	/*
	 * Report a non-OK status to the user
	 */
	private IStatus reportAndInterpretStatus(IStatus status, IAdaptable uiInfo,
			IUndoableOperation operation, boolean undoing) {
		// CANCEL status is assumed to be initiated by the user, so there
		// is nothing to report.
		if (status.getSeverity() == IStatus.CANCEL)
			return status;

		// Other status severities are reported with a message dialog.
		// First obtain a shell and set up the dialog title.
		boolean createdShell = false;
		IStatus reportedStatus = status;

		Shell shell = getShell(uiInfo);
		if (shell == null) {
			createdShell = true;
			shell = new Shell();
		}

		// Set up the dialog. For non-error statuses, we use a warning dialog
		// that allows the user to proceed or to cancel out of the operation.

		if (!(status.getSeverity() == IStatus.ERROR)) {
			String command, title;
			if (undoing) {
				command = WorkbenchMessages.Workbench_undo;
				if (status.getSeverity() == IStatus.INFO)
					title = WorkbenchMessages.Operations_undoInfo;
				else
					title = WorkbenchMessages.Operations_undoWarning;
			} else {
				command = WorkbenchMessages.Workbench_redo;
				if (status.getSeverity() == IStatus.INFO)
					title = WorkbenchMessages.Operations_redoInfo;
				else
					title = WorkbenchMessages.Operations_redoWarning;
			}

			String message = NLS.bind(
					WorkbenchMessages.Operations_proceedWithNonOKStatus,
					new String[] { status.getMessage(), command,
							operation.getLabel() });
			String[] buttons = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
			MessageDialog dialog = new MessageDialog(shell, title, null,
					message, MessageDialog.WARNING, buttons, 0);
			boolean proceed = (dialog.open() == 0);
			// if the user chooses to proceed anyway, map the status to OK so
			// that the operation is considered approved. Otherwise leave
			// the status as is to stop the operation.
			if (proceed)
				reportedStatus = Status.OK_STATUS;
		} else {
			String title, stopped;
			if (undoing) {
				title = WorkbenchMessages.Operations_undoProblem;
				stopped = WorkbenchMessages.Operations_stoppedOnUndoErrorStatus;
			} else {
				title = WorkbenchMessages.Operations_redoProblem;
				stopped = WorkbenchMessages.Operations_stoppedOnRedoErrorStatus;
			}

			// It is an error condition. The user has no choice to proceed, so
			// we only report what has gone on. We use a warning icon instead of
			// an error icon since there has not yet been a failure.

			String message = NLS.bind(stopped, status.getMessage(), operation
					.getLabel());

			MessageDialog dialog = new MessageDialog(shell, title, null,
					message, MessageDialog.WARNING,
					new String[] { IDialogConstants.OK_LABEL }, 0); // ok
			dialog.open();
		}

		if (createdShell)
			shell.dispose();

		return reportedStatus;

	}

	/*
	 * Return the shell described by the supplied uiInfo, or null if no shell is
	 * described.
	 */
	Shell getShell(IAdaptable uiInfo) {
		if (uiInfo != null) {
			Shell shell = (Shell) uiInfo.getAdapter(Shell.class);
			if (shell != null)
				return shell;
		}
		return null;
	}
}
