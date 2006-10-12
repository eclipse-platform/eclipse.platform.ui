/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation2;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractWorkspaceOperation represents an undoable operation that affects
 * the workspace. It handles common workspace operation activities such as
 * tracking which resources are affected by an operation, prompting the user
 * when there are possible side effects of operations, error handling for core
 * exceptions, etc.
 * 
 * This class is not intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public abstract class AbstractWorkspaceOperation extends AbstractOperation
		implements IAdvancedUndoableOperation, IAdvancedUndoableOperation2 {

	private static String ELLIPSIS = "..."; //$NON-NLS-1$

	protected static int EXECUTE = 1;

	protected static int UNDO = 2;

	protected static int REDO = 3;

	protected IResource[] resources;

	private boolean isValid = true;

	/*
	 * Specifies whether any user prompting is appropriate while computing
	 * status.
	 */
	protected boolean quietCompute = false;

	String[] modelProviderIds;

	/**
	 * Return the shell described by the specified adaptable, or the active
	 * shell if no shell has been specified in the adaptable.
	 * 
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * 
	 * @return the shell specified in the adaptable, or the active shell if no
	 *         shell has been specified
	 * 
	 */
	protected static Shell getShell(IAdaptable uiInfo) {
		if (uiInfo != null) {
			Shell shell = (Shell) uiInfo.getAdapter(Shell.class);
			if (shell != null) {
				return shell;
			}
		}
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Create an AbstractWorkspaceOperation with the specified name.
	 * 
	 * @param name
	 *            the name used to describe the operation
	 */
	AbstractWorkspaceOperation(String name) {
		// Many operation names are based on the triggering action's name, so
		// we strip out the any mnemonics that may be embedded in the name.
		super(Action.removeMnemonics(name));

		// For the same reason, check for an ellipsis and strip out
		String label = this.getLabel();
		if (label.endsWith(ELLIPSIS)) {
			this.setLabel(label
					.substring(0, label.length() - ELLIPSIS.length()));
		}
	}

	/**
	 * Set the ids of any model providers for the resources involved.
	 * 
	 * @param ids
	 *            the array of String model provider ids that provide models
	 *            associated with the resources involved in this operation
	 */
	public void setModelProviderIds(String[] ids) {
		modelProviderIds = ids;
	}

	/**
	 * Set the resources which are affected by this operation
	 * 
	 * @param resources
	 *            an array of resources
	 */
	protected void setTargetResources(IResource[] resources) {
		this.resources = resources;
	}

	/**
	 * Return the workspace manipulated by this operation.
	 * 
	 * @return the IWorkspace used by this operation.
	 */
	protected IWorkspace getWorkspace() {
		return WorkspaceUndoUtil.getWorkspace();
	}

	/**
	 * Mark this operation invalid due to some external change. May be used by
	 * subclasses.
	 * 
	 */
	protected void markInvalid() {
		isValid = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation checks a validity flag.
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canExecute()
	 */
	public boolean canExecute() {
		return isValid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation checks a validity flag.
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canUndo()
	 */
	public boolean canUndo() {
		return isValid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation checks a validity flag.
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canRedo()
	 */
	public boolean canRedo() {
		return isValid();
	}

	/**
	 * Execute the specified operation. This implementation executes the
	 * operation in a workspace runnable and catches any CoreExceptions
	 * resulting from the operation. An error dialog is shown to the user if a
	 * CoreException occurs and the exception is propagated as an
	 * ExecutionException if specified by the core exception handler
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return the IStatus of the execution. The status severity should be set
	 *         to <code>OK</code> if the operation was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the execution.
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor, info);
				}
			}, getExecuteSchedulingRule(), IWorkspace.AVOID_UPDATE, null);
		} catch (final CoreException e) {
			final boolean[] propagateException = new boolean[1];
			getShell(info).getDisplay().syncExec(new Runnable() {
				public void run() {
					propagateException[0] = handleCoreException(
							e,
							getShell(info),
							NLS
									.bind(
											UndoMessages.AbstractWorkspaceOperation_ExecuteErrorTitle,
											getLabel()));

				}

			});
			if (propagateException[0]) {
				throw new ExecutionException(
						NLS
								.bind(
										UndoMessages.AbstractWorkspaceOperation_ExecuteErrorTitle,
										getLabel()), e);
			}
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/**
	 * Redo the specified operation. This implementation redoes the operation in
	 * a workspace runnable and catches any CoreExceptions resulting from the
	 * operation. An error dialog is shown to the user if a CoreException occurs
	 * and the exception is propagated as an ExecutionException if specified by
	 * the core exception handler.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return the IStatus of the redo. The status severity should be set to
	 *         <code>OK</code> if the operation was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the redo.
	 * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus redo(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor, info);
				}
			}, getRedoSchedulingRule(), IWorkspace.AVOID_UPDATE, null);
		} catch (final CoreException e) {
			final boolean[] propagateException = new boolean[1];
			getShell(info).getDisplay().syncExec(new Runnable() {
				public void run() {
					propagateException[0] = handleCoreException(
							e,
							getShell(info),
							NLS
									.bind(
											UndoMessages.AbstractWorkspaceOperation_RedoErrorTitle,
											getLabel()));
				}

			});
			if (propagateException[0]) {
				throw new ExecutionException(NLS.bind(
						UndoMessages.AbstractWorkspaceOperation_RedoErrorTitle,
						getLabel()), e);
			}
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/**
	 * Undo the specified operation. This implementation undoes the operation in
	 * a workspace runnable and catches any CoreExceptions resulting from the
	 * operation. An error dialog is shown to the user if a CoreException occurs
	 * and the exception is propagated as an ExecutionException if specified by
	 * the core exception handler.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return the IStatus of the undo. The status severity should be set to
	 *         <code>OK</code> if the operation was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the undo.
	 * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IProgressMonitor monitor, final IAdaptable info)
			throws ExecutionException {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doUndo(monitor, info);
				}
			}, getUndoSchedulingRule(), IWorkspace.AVOID_UPDATE, null);
		} catch (final CoreException e) {
			final boolean[] propagateException = new boolean[1];
			getShell(info).getDisplay().syncExec(new Runnable() {
				public void run() {
					propagateException[0] = handleCoreException(
							e,
							getShell(info),
							NLS
									.bind(
											UndoMessages.AbstractWorkspaceOperation_UndoErrorTitle,
											getLabel()));

				}

			});
			if (propagateException[0]) {
				throw new ExecutionException(NLS.bind(
						UndoMessages.AbstractWorkspaceOperation_UndoErrorTitle,
						getLabel()), e);
			}
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/**
	 * Perform the specific work involved in undoing this operation.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * 
	 */
	protected abstract void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	/**
	 * Perform the specific work involved in executing this operation.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 */
	protected abstract void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	/**
	 * Return whether the proposed operation is valid. The default
	 * implementation simply checks to see if the flag has been marked as
	 * invalid, relying on subclasses to mark the flag invalid when appropriate.
	 * 
	 * @return the validity flag
	 */
	protected boolean isValid() {
		return isValid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#aboutToNotify(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void aboutToNotify(OperationHistoryEvent event) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#getAffectedObjects()
	 */
	public Object[] getAffectedObjects() {
		return resources;
	}

	/**
	 * Return a status indicating the projected outcome of executing the
	 * receiver. This method is not called by the operation history, but instead
	 * is used by clients (such as implementers of {@link IOperationApprover2})
	 * who wish to perform advanced validation of an operation before attempting
	 * to execute it.
	 * 
	 * If an ERROR status is returned, the operation will not proceed and the
	 * user notified if deemed necessary by the caller. The validity flag on the
	 * operation should be marked as invalid. If an OK status is returned, the
	 * operation will proceed. The caller must interpret any other returned
	 * status severity, and may choose to prompt the user as to how to proceed.
	 * 
	 * If there are multiple conditions that result in an ambiguous status
	 * severity, it is best for the implementor of this method to consult the
	 * user as to how to proceed for each one, and return an OK or ERROR status
	 * that accurately reflects the user's wishes, or to return a multi-status
	 * that accurately describes all of the issues at hand, so that the caller
	 * may potentially consult the user. (Note that the user should not be
	 * consulted at all if a client has called {@link #setQuietCompute(boolean)}
	 * with a value of <code>true</code>.)
	 * 
	 * This implementation computes the validity of execution by computing the
	 * resource delta that would be generated on execution, and checking whether
	 * any registered model providers are affected by the operation.
	 * 
	 * @param monitor
	 *            the progress monitor to be used for computing the status
	 * @return the status indicating the projected outcome of executing the
	 *         receiver
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 * @see #setQuietCompute(boolean)
	 */
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;

		// If we are not to prompt the user, nothing to do.
		if (quietCompute) {
			return status;
		}

		IResourceChangeDescriptionFactory factory = ResourceChangeValidator
				.getValidator().createDeltaFactory();
		if (updateResourceChangeDescriptionFactory(factory, EXECUTE)) {
			boolean proceed = IDE
					.promptToConfirm(
							getShell(null),
							UndoMessages.AbstractWorkspaceOperation_SideEffectsWarningTitle,
							NLS
									.bind(
											UndoMessages.AbstractWorkspaceOperation_ExecuteSideEffectsWarningMessage,
											getLabel()), factory.getDelta(),
							modelProviderIds, true /* syncExec */);
			if (!proceed) {
				status = IOperationHistory.OPERATION_INVALID_STATUS;
				markInvalid();
			}
		}
		return status;

	}

	/**
	 * Return a status indicating the projected outcome of undoing the receiver.
	 * This method is not called by the operation history, but instead is used
	 * by clients (such as implementers of {@link IOperationApprover2}) who
	 * wish to perform advanced validation of an operation before attempting to
	 * undo it.
	 * 
	 * If an ERROR status is returned, the undo will not proceed and the user
	 * notified if deemed necessary by the caller. The validity flag on the
	 * operation should be marked as invalid. If an OK status is returned, the
	 * undo will proceed. The caller must interpret any other returned status
	 * severity, and may choose to prompt the user as to how to proceed.
	 * 
	 * If there are multiple conditions that result in an ambiguous status
	 * severity, it is best for the implementor of this method to consult the
	 * user as to how to proceed for each one, and return an OK or ERROR status
	 * that accurately reflects the user's wishes, or to return a multi-status
	 * that accurately describes all of the issues at hand, so that the caller
	 * may potentially consult the user. (Note that the user should not be
	 * consulted at all if a client has called {@link #setQuietCompute(boolean)}
	 * with a value of <code>true</code>.)
	 * 
	 * This implementation computes the validity of undo by computing the
	 * resource delta that would be generated on undo, and checking whether any
	 * registered model providers are affected by the operation.
	 * 
	 * @param monitor
	 *            the progress monitor to be used for computing the status
	 * @return the status indicating the projected outcome of undoing the
	 *         receiver
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 * @see #setQuietCompute(boolean)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		// If we are not to prompt the user, nothing to do.
		if (quietCompute) {
			return status;
		}

		IResourceChangeDescriptionFactory factory = ResourceChangeValidator
				.getValidator().createDeltaFactory();
		if (updateResourceChangeDescriptionFactory(factory, UNDO)) {
			boolean proceed = IDE
					.promptToConfirm(
							getShell(null),
							UndoMessages.AbstractWorkspaceOperation_SideEffectsWarningTitle,
							NLS
									.bind(
											UndoMessages.AbstractWorkspaceOperation_UndoSideEffectsWarningMessage,
											getLabel()), factory.getDelta(),
							modelProviderIds, true /* syncExec */);
			if (!proceed) {
				status = IOperationHistory.OPERATION_INVALID_STATUS;
				markInvalid();
			}
		}
		return status;

	}

	/**
	 * Return a status indicating the projected outcome of redoing the receiver.
	 * This method is not called by the operation history, but instead is used
	 * by clients (such as implementers of {@link IOperationApprover2}) who
	 * wish to perform advanced validation of an operation before attempting to
	 * redo it.
	 * 
	 * If an ERROR status is returned, the redo will not proceed and the user
	 * notified if deemed necessary by the caller. The validity flag on the
	 * operation should be marked as invalid. If an OK status is returned, the
	 * redo will proceed. The caller must interpret any other returned status
	 * severity, and may choose to prompt the user as to how to proceed.
	 * 
	 * If there are multiple conditions that result in an ambiguous status
	 * severity, it is best for the implementor of this method to consult the
	 * user as to how to proceed for each one, and return an OK or ERROR status
	 * that accurately reflects the user's wishes, or to return a multi-status
	 * that accurately describes all of the issues at hand, so that the caller
	 * may potentially consult the user. (Note that the user should not be
	 * consulted at all if a client has called {@link #setQuietCompute(boolean)}
	 * with a value of <code>true</code>.)
	 * 
	 * This implementation computes the validity of redo by computing the
	 * resource delta that would be generated on redo, and checking whether any
	 * registered model providers are affected by the operation.
	 * 
	 * @param monitor
	 *            the progress monitor to be used for computing the status
	 * @return the status indicating the projected outcome of redoing the
	 *         receiver
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 * @see #setQuietCompute(boolean)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		// If we are not to prompt the user, nothing to do.
		if (quietCompute) {
			return status;
		}

		IResourceChangeDescriptionFactory factory = ResourceChangeValidator
				.getValidator().createDeltaFactory();
		if (updateResourceChangeDescriptionFactory(factory, REDO)) {
			boolean proceed = IDE
					.promptToConfirm(
							getShell(null),
							UndoMessages.AbstractWorkspaceOperation_SideEffectsWarningTitle,
							NLS
									.bind(
											UndoMessages.AbstractWorkspaceOperation_RedoSideEffectsWarningMessage,
											getLabel()), factory.getDelta(),
							modelProviderIds, true /* syncExec */);
			if (!proceed) {
				status = IOperationHistory.OPERATION_INVALID_STATUS;
				markInvalid();
			}
		}
		return status;
	}

	/**
	 * Update the provided resource change description factory so it can
	 * generate a resource delta describing the result of an undo or redo.
	 * Return a boolean indicating whether any update was done. The default
	 * implementation does not update the factory. Subclasses are expected to
	 * override this method to more specifically describe their modifications to
	 * the workspace.
	 * 
	 * @param factory
	 *            the factory to update
	 * @param operation
	 *            an integer indicating whether the change is part of an
	 *            execute, undo, or redo
	 * @return a boolean indicating whether the factory was updated.
	 */
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		return false;
	}

	/**
	 * Return an error status describing an invalid operation using the provided
	 * message.
	 * 
	 * @param message
	 *            the message to be used in the status, or <code>null</code>
	 *            if a generic message should be used
	 * @return the error status
	 */
	protected IStatus getErrorStatus(String message) {
		String statusMessage = message;
		if (statusMessage == null) {
			statusMessage = NLS
					.bind(
							UndoMessages.AbstractWorkspaceOperation_ErrorInvalidMessage,
							getLabel());
		}
		return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
				OperationStatus.OPERATION_INVALID, statusMessage, null);
	}

	/**
	 * Return a warning status describing the warning state of an operation
	 * using the provided message and code.
	 * 
	 * @param message
	 *            the message to be used in the status, or <code>null</code>
	 *            if a generic message should be used
	 * @param code
	 *            the integer code to be assigned to the status
	 * @return the warning status
	 */
	protected IStatus getWarningStatus(String message, int code) {
		String statusMessage = message;
		if (statusMessage == null) {
			statusMessage = NLS
					.bind(
							UndoMessages.AbstractWorkspaceOperation_GenericWarningMessage,
							getLabel());
		}
		return new Status(IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH,
				code, statusMessage, null);
	}

	/**
	 * Return whether the resources known by this operation currently exist.
	 * 
	 * @return <code>true</code> if there are existing resources and
	 *         <code>false</code> if there are no known resources or any one
	 *         of them does not exist
	 */
	protected boolean resourcesExist() {
		if (resources == null || resources.length == 0) {
			return false;
		}
		for (int i = 0; i < resources.length; i++) {
			if (!resources[i].exists()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Return whether the resources known by this operation contain any
	 * projects.
	 * 
	 * @return <code>true</code> if there is one or more projects known by
	 *         this operation and false if there are no projects.
	 */
	protected boolean resourcesIncludesProjects() {
		if (resources == null || resources.length == 0) {
			return false;
		}
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() == IResource.PROJECT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handle the core exception that occurred while trying to execute, undo, or
	 * redo the operation, returning a boolean to indicate whether this
	 * exception should cause a {@link ExecutionException} to be thrown.
	 * 
	 * It is safe to access UI in this method. The default implementation is to
	 * show an error dialog, but subclasses may override this method to swallow
	 * certain exceptions or handle them differently.
	 * 
	 * If the only difference in handling the exception is to show a different
	 * error message, subclasses should override
	 * {@link #getErrorMessage(CoreException)} instead.
	 * 
	 * @param e
	 *            the CoreException
	 * @param shell
	 *            the shell to be used for showing any UI information
	 * @param errorTitle
	 *            the title to be used in the error dialog.
	 */
	protected boolean handleCoreException(CoreException e, Shell shell,
			String errorTitle) {
		ErrorDialog.openError(shell, errorTitle, getErrorMessage(e), e
				.getStatus());
		return true;
	}

	/**
	 * Return the specific error message to use when the specified core
	 * exception occurs, or <code>null</code> to indicate that the the
	 * exception's message should be used.
	 * 
	 * @param e
	 *            the CoreException
	 * @return the string to be used in any shown error message, or
	 *         <code>null</code> if the exception's message should be shown.
	 */
	protected String getErrorMessage(CoreException e) {
		return null;
	}

	/**
	 * Return a scheduling rule appropriate for executing this operation.
	 * 
	 * The default implementation is to return a rule that locks out the entire
	 * workspace. Subclasses are encouraged to provide more specific rules that
	 * affect only their resources.
	 * 
	 * @return the scheduling rule to use when executing this operation, or
	 *         <code>null</code> if there are no scheduling restrictions for
	 *         this operation.
	 * 
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int,
	 *      IProgressMonitor)
	 */
	protected ISchedulingRule getExecuteSchedulingRule() {
		return WorkspaceUndoUtil.getWorkspaceRoot();
	}

	/**
	 * Return a scheduling rule appropriate for undoing this operation.
	 * 
	 * The default implementation is to return a rule that locks out the entire
	 * workspace. Subclasses are encouraged to provide more specific rules that
	 * affect only their resources.
	 * 
	 * @return the scheduling rule to use when undoing this operation, or
	 *         <code>null</code> if there are no scheduling restrictions for
	 *         this operation.
	 * 
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int,
	 *      IProgressMonitor)
	 */
	protected ISchedulingRule getUndoSchedulingRule() {
		return WorkspaceUndoUtil.getWorkspaceRoot();
	}

	/**
	 * Return a scheduling rule appropriate for redoing this operation.
	 * 
	 * The default implementation considers the redo scheduling rule the same as
	 * the original execution scheduling rule.
	 * 
	 * @return the scheduling rule to use when redoing this operation, or
	 *         <code>null</code> if there are no scheduling restrictions for
	 *         this operation.
	 * 
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int,
	 *      IProgressMonitor)
	 */
	protected ISchedulingRule getRedoSchedulingRule() {
		return getExecuteSchedulingRule();
	}

	/**
	 * Set the boolean that determines whether the computation of the receiver's
	 * execution, undo, or redo status may consult the user. The default value
	 * is <code>false</code>. This flag should only be set to
	 * <code>true</code> while some type of background/quiet computation of
	 * validity is being performed, and should be restored to <code>false</code>
	 * when complete.
	 * 
	 * @param quiet
	 *            <code>true</code> if it is inappropriate to consult the user
	 *            while computing status, and <code>false</code> if the user
	 *            may be consulted.
	 * 
	 * @see #computeExecutionStatus(IProgressMonitor)
	 * @see #computeUndoableStatus(IProgressMonitor)
	 * @see #computeRedoableStatus(IProgressMonitor)
	 */
	public void setQuietCompute(boolean quiet) {
		quietCompute = quiet;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append("\n"); //$NON-NLS-1$
		text.append(this.getClass().getName());
		appendDescriptiveText(text);
		return text.toString();
	}

	/**
	 * Append any descriptive text to the specified string buffer to be shown in
	 * the receiver's {@link #toString()} text.
	 * 
	 * @param text
	 *            the StringBuffer on which to append the text
	 */
	protected void appendDescriptiveText(StringBuffer text) {
		text.append(" resources: "); //$NON-NLS-1$
		text.append(resources);
		text.append('\'');
	}
}
