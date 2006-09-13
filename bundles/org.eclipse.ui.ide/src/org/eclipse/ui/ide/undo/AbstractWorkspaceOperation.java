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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.resources.mapping.ResourceChangeValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
abstract class AbstractWorkspaceOperation extends AbstractOperation implements
		IAdvancedUndoableOperation, IAdvancedUndoableOperation2 {

	private static String ELLIPSIS = "..."; //$NON-NLS-1$

	private static int EXECUTE = 1;

	private static int UNDO = 2;

	private static int REDO = 3;

	protected IResource[] resources;

	private boolean isValid = true;

	String[] modelProviderIds;

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
		return ResourcesPlugin.getWorkspace();
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
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canExecute()
	 */
	public boolean canExecute() {
		return isValid();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canUndo()
	 */
	public boolean canUndo() {
		return isValid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#canRedo()
	 */
	public boolean canRedo() {
		return isValid();
	}

	/*
	 * Execute the specified operation. This implementation executes the
	 * operation in a workspace runnable and catches any CoreExceptions
	 * resulting from the operation. An error dialog is shown to the user if a
	 * CoreException occurs and the exception is propagated as an
	 * ExecutionException.
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
			}, null);
		} catch (final CoreException e) {
			getShell(info).getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog
							.openError(
									getShell(info),
									NLS
											.bind(
													UndoMessages.AbstractWorkspaceOperation_ExecuteErrorTitle,
													getLabel()), null, e
											.getStatus());

				}

			});
			throw new ExecutionException(NLS.bind(
					UndoMessages.AbstractWorkspaceOperation_ExecuteErrorTitle,
					getLabel()), e);
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/*
	 * Redo the specified operation. This implementation redoes the operation in
	 * a workspace runnable and catches any CoreExceptions resulting from the
	 * operation. An error dialog is shown to the user if a CoreException occurs
	 * and the exception is propagated as an ExecutionException.
	 * 
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
			}, null);
		} catch (final CoreException e) {
			getShell(info).getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog
							.openError(
									getShell(info),
									NLS
											.bind(
													UndoMessages.AbstractWorkspaceOperation_RedoErrorTitle,
													getLabel()), null, e
											.getStatus());

				}

			});
			throw new ExecutionException(NLS.bind(
					UndoMessages.AbstractWorkspaceOperation_RedoErrorTitle,
					getLabel()), e);
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/*
	 * Undo the specified operation. This implementation undoes the operation in
	 * a workspace runnable and catches any CoreExceptions resulting from the
	 * operation. An error dialog is shown to the user if a CoreException occurs
	 * and the exception is propagated as an ExecutionException.
	 * 
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
			}, null);
		} catch (final CoreException e) {
			getShell(info).getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog
							.openError(
									getShell(info),
									NLS
											.bind(
													UndoMessages.AbstractWorkspaceOperation_UndoErrorTitle,
													getLabel()), null, e
											.getStatus());

				}

			});
			throw new ExecutionException(NLS.bind(
					UndoMessages.AbstractWorkspaceOperation_UndoErrorTitle,
					getLabel()), e);
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/*
	 * Perform the specific work involved in undoing this operation.
	 */
	protected abstract void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	/*
	 * Perform the specific work involved in executing this operation.
	 */
	protected abstract void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	/*
	 * Return the shell described by the specified adaptable, or the active
	 * shell if no shell has been specified in the adaptable.
	 */
	protected Shell getShell(IAdaptable info) {
		if (info != null) {
			Shell shell = (Shell) info.getAdapter(Shell.class);
			if (shell != null) {
				return shell;
			}
		}
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/**
	 * Return whether the proposed operation is valid. The default
	 * implementation simply checks to see if the flag has been marked as
	 * invalid, relying on subclasses to mark the flag invalid when
	 * appropriate.
	 * 
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
	
	/*
	 * Return a status indicating the projected outcome of executing the receiver.
	 * 
	 * This method computes the validity of execution by computing the resource
	 * delta that would be generated on execution, and checking whether any
	 * registered model providers are affected by the operation. This method is
	 * not called by the operation history, but instead is used by clients (such
	 * as implementers of {@link IOperationApprover2}) who wish to perform
	 * advanced validation of an operation before attempting to execute it. If the
	 * execute is not valid, then the validity flag on the operation should be
	 * marked invalid.
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
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

	/*
	 * Return a status indicating the projected outcome of undoing the receiver.
	 * 
	 * This method computes the validity of an undo by computing the resource
	 * delta that would be generated on undo, and checking whether any
	 * registered model providers are affected by the operation. This method is
	 * not called by the operation history, but instead is used by clients (such
	 * as implementers of {@link IOperationApprover}) who wish to perform
	 * advanced validation of an operation before attempting to undo it. If the
	 * undo is not valid, then the validity flag on the operation should be
	 * marked invalid.
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
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

	/*
	 * Return a status indicating the projected outcome of redoing the receiver.
	 * 
	 * This method computes the validity of a redo by computing the resource
	 * delta that would be generated on redo, and checking whether any
	 * registered model providers are affected by the operation. This method is
	 * not called by the operation history, but instead is used by clients (such
	 * as implementers of {@link IOperationApprover}) who wish to perform
	 * advanced validation of an operation before attempting to redo it. If the
	 * redo is not valid, then the validity flag on the operation should be
	 * marked invalid.
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
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

	/*
	 * Get an error status describing an invalid operation using the provided
	 * message.
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

	/*
	 * Get a warning status describing the warning state for an operation using
	 * the provided message and code.
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
}
