/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation2;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationApprover2;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.Util;

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
 * <p>
 * Since 3.3, this operation approver also checks the validity of a proposed
 * execute by determining whether the redo is viable.
 * 
 * @since 3.1
 */
public class AdvancedValidationUserApprover implements IOperationApprover,
		IOperationApprover2 {
	
    /**
     * Static to prevent opening of error dialogs for automated testing.
     * 
     * @since 3.3
     */
    public static boolean AUTOMATED_MODE = false;

	private IUndoContext context;

	private static final int EXECUTING = 1;

	private static final int UNDOING = 2;

	private static final int REDOING = 3;

	private class StatusReportingRunnable implements IRunnableWithProgress {
		IStatus status;

		int doing;

		IUndoableOperation operation;

		IOperationHistory history;

		IAdaptable uiInfo;

		StatusReportingRunnable(IUndoableOperation operation,
				IOperationHistory history, IAdaptable uiInfo, int doing) {
			super();
			this.operation = operation;
			this.history = history;
			this.doing = doing;
			this.uiInfo = uiInfo;
		}

		// The casts to IAdvancedUndoableOperation and
		// IAdvancedUndoableOperation2 are safe because these types were checked
		// in the call chain.
		public void run(IProgressMonitor pm) {
			try {
				switch (doing) {
				case UNDOING:
					status = ((IAdvancedUndoableOperation) operation)
							.computeUndoableStatus(pm);
					break;
				case REDOING:
					status = ((IAdvancedUndoableOperation) operation)
							.computeRedoableStatus(pm);
					break;
				case EXECUTING:
					status = ((IAdvancedUndoableOperation2) operation)
							.computeExecutionStatus(pm);
					break;
				}

			} catch (ExecutionException e) {
				reportException(e, uiInfo);
				status = IOperationHistory.OPERATION_INVALID_STATUS;
			}
		}

		IStatus getStatus() {
			return status;
		}
	}

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
		return proceedWithOperation(operation, history, uiInfo, REDOING);
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

		return proceedWithOperation(operation, history, uiInfo, UNDOING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover2#proceedExecuting(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedExecuting(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {
		return proceedWithOperation(operation, history, uiInfo, EXECUTING);
	}

	/*
	 * Determine whether the operation in question is still valid.
	 */
	private IStatus proceedWithOperation(final IUndoableOperation operation,
			final IOperationHistory history, final IAdaptable uiInfo,
			final int doing) {

		// return immediately if the operation is not relevant
		if (!operation.hasContext(context)) {
			return Status.OK_STATUS;
		}

		// if the operation does not support advanced validation,
		// then we assume it is valid.
		if (doing == EXECUTING) {
			if (!(operation instanceof IAdvancedUndoableOperation2)) {
				return Status.OK_STATUS;
			}
		} else {
			if (!(operation instanceof IAdvancedUndoableOperation)) {
				return Status.OK_STATUS;
			}
		}

		// The next two methods make a number of UI calls, so we wrap the
		// whole thing up in a syncExec.
		final IStatus[] status = new IStatus[1];
		Workbench.getInstance().getDisplay().syncExec(new Runnable() {
			public void run() {
				// Compute the undoable or redoable status
				status[0] = computeOperationStatus(operation, history, uiInfo,
						doing);

				// Report non-OK statuses to the user. In some cases, the user
				// may choose to proceed, and the returned status will be
				// different than what is reported.
				if (!status[0].isOK()) {
					status[0] = reportAndInterpretStatus(status[0], uiInfo,
							operation, doing);
				}

			}
		});

		// If the operation is still not OK, inform the history that the
		// operation has changed, since it was previously believed to be valid.
		// We rely here on the ability of an IAdvancedUndoableOperation to
		// correctly report canUndo() and canRedo() once the undoable and
		// redoable status have been computed.
		if (!status[0].isOK()) {
			history.operationChanged(operation);
		}
		return status[0];
	}

	private IStatus computeOperationStatus(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo, int doing) {
		try {
			StatusReportingRunnable runnable = new StatusReportingRunnable(
					operation, history, uiInfo, doing);
			TimeTriggeredProgressMonitorDialog progressDialog = new TimeTriggeredProgressMonitorDialog(
					getShell(uiInfo), PlatformUI.getWorkbench()
							.getProgressService().getLongOperationTime());

			progressDialog.run(false, true, runnable);
			return runnable.getStatus();
		} catch (OperationCanceledException e) {
			return Status.CANCEL_STATUS;
		} catch (InvocationTargetException e) {
			reportException(e, uiInfo);
			return IOperationHistory.OPERATION_INVALID_STATUS;
		} catch (InterruptedException e) {
			// Operation was cancelled and acknowledged by runnable with this
			// exception. Do nothing.
			return Status.CANCEL_STATUS;
		}
	}

	/*
	 * Report the specified execution exception to the log and to the user.
	 */
	private void reportException(Exception e, IAdaptable uiInfo) {
		Throwable nestedException = StatusUtil.getCause(e);
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
		if (createdShell) {
			shell.dispose();
		}
	}

	/*
	 * Report a non-OK status to the user
	 */
	private IStatus reportAndInterpretStatus(IStatus status, IAdaptable uiInfo,
			IUndoableOperation operation, int doing) {
		// Nothing to report if we are running automated tests.  We will treat
		// warnings as if they were authorized by the user.
		if (AUTOMATED_MODE) {
			if (status.getSeverity() == IStatus.WARNING) {
				return Status.OK_STATUS;
			}
			return status;
		}
		
		// CANCEL status is assumed to be initiated by the user, so there
		// is nothing to report.
		if (status.getSeverity() == IStatus.CANCEL) {
			return status;
		}

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
			String warning, title;
			switch (doing) {
			case UNDOING:
				warning = WorkbenchMessages.Operations_proceedWithNonOKUndoStatus;
				if (status.getSeverity() == IStatus.INFO) {
					title = WorkbenchMessages.Operations_undoInfo;
				} else {
					title = WorkbenchMessages.Operations_undoWarning;
				}
				break;
			case REDOING:
				warning = WorkbenchMessages.Operations_proceedWithNonOKRedoStatus;
				if (status.getSeverity() == IStatus.INFO) {
					title = WorkbenchMessages.Operations_redoInfo;
				} else {
					title = WorkbenchMessages.Operations_redoWarning;
				}
				break;
			default: // EXECUTING
				warning = WorkbenchMessages.Operations_proceedWithNonOKExecuteStatus;
				if (status.getSeverity() == IStatus.INFO) {
					title = WorkbenchMessages.Operations_executeInfo;
				} else {
					title = WorkbenchMessages.Operations_executeWarning;
				}
				break;
			}

			String message = NLS.bind(warning, new Object[] { status.getMessage(), operation.getLabel() });
			String[] buttons = new String[] { IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL };
			MessageDialog dialog = new MessageDialog(shell, title, null,
					message, MessageDialog.WARNING, buttons, 0);
			int dialogAnswer = dialog.open();
			// The user has been given the specific status and has chosen
			// to proceed or to cancel. The user choice determines what
			// the status should be at this point, OK or CANCEL.
			if (dialogAnswer == Window.OK) {
				reportedStatus = Status.OK_STATUS;
			} else {
				reportedStatus = Status.CANCEL_STATUS;
			}
		} else {
			String title, stopped;
			switch (doing) {
			case UNDOING:
				title = WorkbenchMessages.Operations_undoProblem;
				stopped = WorkbenchMessages.Operations_stoppedOnUndoErrorStatus;
				break;
			case REDOING:
				title = WorkbenchMessages.Operations_redoProblem;
				stopped = WorkbenchMessages.Operations_stoppedOnRedoErrorStatus;
				break;
			default: // EXECUTING
				title = WorkbenchMessages.Operations_executeProblem;
				stopped = WorkbenchMessages.Operations_stoppedOnExecuteErrorStatus;

				break;
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

		if (createdShell) {
			shell.dispose();
		}

		return reportedStatus;

	}

	/*
	 * Return the shell described by the supplied uiInfo, or null if no shell is
	 * described.
	 */
	Shell getShell(IAdaptable uiInfo) {
		if (uiInfo != null) {
			Shell shell = (Shell) Util.getAdapter(uiInfo, Shell.class);
			if (shell != null) {
				return shell;
			}
		}
		return null;
	}
}
