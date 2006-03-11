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

package org.eclipse.ui.internal.ide.undo;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * @since 3.2
 * 
 */
public abstract class AbstractWorkspaceOperation extends AbstractOperation
		implements IAdvancedUndoableOperation {

	protected IResource[] resources;

	private IResourceChangeListener listener;

	private boolean isValid = true;

	String[] modelProviderIds;

	AbstractWorkspaceOperation(String name) {
		super(name);
		this.addContext(PlatformUI.getWorkbench().getOperationSupport()
				.getUndoContext());
	}

	/**
	 * Set the ids of any model providers for the resources involved.
	 * 
	 * @param ids
	 */
	public void setModelProviderIds(String[] ids) {
		modelProviderIds = ids;
	}

	protected void setTargetResources(IResource[] resources) {
		this.resources = resources;
		if (listener == null && resources != null) {
			addWorkspaceListener();
		}
	}

	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private void addWorkspaceListener() {
		listener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					event.getDelta().accept(getDeltaVisitor());
				} catch (CoreException e) {
					markInvalid();
				}
			}

		};
		getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_CHANGE);
	}

	public void dispose() {
		getWorkspace().removeResourceChangeListener(listener);
		super.dispose();
	}

	protected void markInvalid() {
		isValid = false;
	}

	public boolean canUndo() {
		return isValid();
	}

	public boolean canRedo() {
		return isValid();
	}

	public IStatus execute(IProgressMonitor monitor, final IAdaptable info) {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor, info);
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(info),
					UndoMessages.AbstractWorkspaceOperation_ExecuteErrorTitle,
					null, e.getStatus());
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */

	public IStatus redo(IProgressMonitor monitor, final IAdaptable info) {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doExecute(monitor, info);
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(info),
					UndoMessages.AbstractWorkspaceOperation_RedoErrorTitle,
					null, e.getStatus());
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IProgressMonitor monitor, final IAdaptable info) {
		try {
			getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					doUndo(monitor, info);
				}
			}, null);
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(info),
					UndoMessages.AbstractWorkspaceOperation_UndoErrorTitle,
					null, e.getStatus());
		}
		isValid = true;
		return Status.OK_STATUS;
	}

	protected abstract void doUndo(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	protected abstract void doExecute(IProgressMonitor monitor, IAdaptable info)
			throws CoreException;

	protected IResourceDeltaVisitor getDeltaVisitor() {
		return new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) {
				if (resources == null) {
					return false;
				}
				for (int i = 0; i < resources.length; i++) {
					if (isResourceInvalid(resources[i], delta)) {
						markInvalid();
						return false;
					}
				}
				return true;
			}
		};
	}

	/**
	 * A resource has changed. Return whether this operation is now invalid.
	 * Default implementation is that if one of this operation's resources has
	 * changed, then the operation is invalid.
	 */
	protected boolean isResourceInvalid(IResource resource, IResourceDelta delta) {
		return resource.equals(delta.getResource());
	}

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
	 * Is the proposed operation valid. Subclasses may override. The default
	 * implementation simply checks to see if the flag has been marked as
	 * invalid.
	 * 
	 * @return a boolean indicating whether the operation is valid.
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

	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		IResourceChangeDescriptionFactory factory = ResourceChangeValidator
				.getValidator().createDeltaFactory();
		if (updateResourceChangeDescriptionFactory(factory, true)) {
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

	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = Status.OK_STATUS;
		IResourceChangeDescriptionFactory factory = ResourceChangeValidator
				.getValidator().createDeltaFactory();
		if (updateResourceChangeDescriptionFactory(factory, false)) {
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
	 * Return a boolean indicating whether any update was done.
	 * 
	 * @param factory
	 *            the factory to update
	 * @param undo
	 *            true if the proposed change is undo, false if redo.
	 * @return a boolean indicating whether the factory was updated.
	 */
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, boolean undo) {
		return false;
	}

	/**
	 * Get an error status describing an invalid operation
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
	 * Get a warning status whose mess
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
}
