/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.operations.IOperation;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * A base implementation of IOperationHistory. OperationHistory implements a
 * strict linear undo and redo model. The most recently added operation is
 * available for undo, and the most recently undone operation is available for
 * redo.
 * </p>
 * <p>
 * If the operation eligible for undo is not in a state where it can be undone,
 * then no undo is available. No other operations are consulted. Likewise, if
 * the operation available for redo cannot be redone, then no redo is available.
 * </p>
 * 
 * @since 3.1
 */
public class OperationHistory implements IOperationHistory {

	protected static IStatus NOTHING_TO_REDO_STATUS = new OperationStatus(
			IStatus.INFO, OperationStatus.NOTHING_TO_REDO,
			"No operation to redo"); //$NON-NLS-1$

	protected static IStatus NOTHING_TO_UNDO_STATUS = new OperationStatus(
			IStatus.INFO, OperationStatus.NOTHING_TO_UNDO,
			"No operation to undo"); //$NON-NLS-1$

	protected static IStatus OPERATION_INVALID_STATUS = new OperationStatus(
			IStatus.ERROR, OperationStatus.OPERATION_INVALID,
			"Operation is not valid"); //$NON-NLS-1$

	protected List fApprovers = new ArrayList();

	private int fLimit = 20;

	/**
	 * the list of {@link IOperationHistoryListener}s
	 */
	protected List fListeners = new ArrayList();

	private List fRedo = new ArrayList();

	private List fUndo = new ArrayList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#add(org.eclipse.runtime.operations.IOperation)
	 */
	public void add(IOperation operation) {
		Assert.isNotNull(operation);

		// flush redo stack for related contexts
		OperationContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			flushRedo(contexts[i]);
		}

		checkUndoLimit();
		fUndo.add(operation);
		notifyAdd(operation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#addOperationApprover(org.eclipse.runtime.operations.IOperationApprover)
	 */
	public void addOperationApprover(IOperationApprover approver) {
		fApprovers.add(approver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#addOperationHistoryListener(org.eclipse.runtime.operations.IOperationHistoryListener)
	 */
	public void addOperationHistoryListener(IOperationHistoryListener listener) {
		fListeners.add(listener);
	}

	public boolean canRedo(OperationContext context) {
		// null context is allowed and passed through
		IOperation operation = getRedoOperation(context);
		return (operation != null && operation.canRedo());
	}

	public boolean canUndo(OperationContext context) {
		// null context is allowed and passed through
		IOperation operation = getUndoOperation(context);
		return (operation != null && operation.canUndo());
	}

	/**
	 * Check the redo limit before adding something.
	 */
	private void checkRedoLimit() {
		forceRedoLimit(getLimit() - 1);
	}

	/**
	 * Check the undo limit before adding something.
	 */
	private void checkUndoLimit() {
		forceUndoLimit(getLimit() - 1);
	}

	/*
	 * null is valid argument indicating all contexts
	 */
	public void dispose(OperationContext context, boolean flushUndo,
			boolean flushRedo) {
		if (flushUndo)
			flushUndo(context);
		if (flushRedo)
			flushRedo(context);
	}

	/**
	 * Perform the redo. All validity checks have already occurred.
	 * 
	 * @param monitor
	 * @param operation
	 */
	protected IStatus doRedo(IProgressMonitor monitor, IOperation operation,
			boolean flushOnError) {

		IStatus status = getRedoApproval(operation);
		if (status.isOK()) {
			notifyAboutToRedo(operation);
			status = operation.redo(monitor);
		}

		// if successful, the operation is removed from the redo history and
		// placed back in the undo history.
		if (status.isOK()) {
			fRedo.remove(operation);
			checkUndoLimit();
			fUndo.add(operation);

			// notify listeners must happen after history is updated
			notifyRedone(operation);
		} else {
			notifyNotOK(operation);
			if (flushOnError && status.getSeverity() == IStatus.ERROR) {
				remove(operation);
			}
		}

		return status;
	}

	/**
	 * Perform the undo. All validity checks have already occurred.
	 * 
	 * @param monitor
	 * @param operation
	 */
	protected IStatus doUndo(IProgressMonitor monitor, IOperation operation,
			boolean flushOnError) {
		IStatus status = getUndoApproval(operation);
		if (status.isOK()) {
			notifyAboutToUndo(operation);
			status = operation.undo(monitor);
		}
		// if successful, the operation is removed from the undo history and
		// placed in the redo history.
		if (status.isOK()) {
			fUndo.remove(operation);
			checkRedoLimit();
			fRedo.add(operation);

			// notification occurs after the undo and redo histories are
			// adjusted
			notifyUndone(operation);
		} else {
			notifyNotOK(operation);
			if (flushOnError && status.getSeverity() == IStatus.ERROR) {
				remove(operation);
			}
		}
		return status;
	}

	public IStatus execute(IOperation operation, IProgressMonitor monitor) {
		Assert.isNotNull(operation);

		// error if operation is invalid
		if (!operation.canExecute())
			return OPERATION_INVALID_STATUS;
		notifyAboutToExecute(operation);
		IStatus status = operation.execute(monitor);

		// if successful, the notify listeners are notified and the operation is
		// added to the history
		if (status.isOK()) {
			notifyDone(operation);
			add(operation);
		} else {
			notifyNotOK(operation);
		}
		// all other severities are not interpreted. Simply return the status.
		return status;
	}

	private IOperation[] filter(List list, OperationContext context) {
		// when the context is null, do not filter the list.
		if (context == null)
			return (IOperation[]) list.toArray(new IOperation[list.size()]);

		// otherwise filter on the context
		List filtered = new ArrayList();
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			IOperation operation = (IOperation) iterator.next();
			if (operation.hasContext(context)) {
				filtered.add(operation);
			}
		}
		return (IOperation[]) filtered.toArray(new IOperation[filtered.size()]);
	}

	void flushRedo(OperationContext context) {
		Object[] filtered = filter(fRedo, context);
		for (int i = 0; i < filtered.length; i++) {
			IOperation operation = (IOperation) filtered[i];
			if (operation.getContexts().length == 1) {
				// remove the operation if it only has the context
				fRedo.remove(operation);
				internalRemove(operation);
			} else {
				// remove the reference to the context
				operation.removeContext(context);
			}
		}
	}

	void flushUndo(OperationContext context) {
		Object[] filtered = filter(fUndo, context);
		for (int i = 0; i < filtered.length; i++) {
			IOperation operation = (IOperation) filtered[i];
			if (operation.getContexts().length == 1) {
				// remove the operation if it only has the context
				fUndo.remove(operation);
				internalRemove(operation);
			} else {
				// remove the reference to the context
				operation.removeContext(context);
			}
		}
	}

	private void forceRedoLimit(int max) {
		if (fRedo.size() > 0) {
			while (fRedo.size() > max) {
				IOperation removed = (IOperation) fRedo.get(0);
				fRedo.remove(removed);
				internalRemove(removed);
			}
		}
	}

	private void forceUndoLimit(int max) {
		if (fUndo.size() > 0) {
			while (fUndo.size() > max) {
				IOperation removed = (IOperation) fUndo.get(0);
				fUndo.remove(removed);
				internalRemove(removed);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#getLimit()
	 */
	public int getLimit() {
		return fLimit;
	}

	protected IStatus getRedoApproval(IOperation operation) {
		for (int i = 0; i < fApprovers.size(); i++) {
			IStatus approval = ((IOperationApprover) fApprovers.get(i))
					.proceedRedoing(operation, this);
			if (!approval.isOK())
				return approval;
		}
		return Status.OK_STATUS;
	}

	/*
	 * null is a valid argument indicating the entire history
	 */
	public IOperation[] getRedoHistory(OperationContext context) {
		return filter(fRedo, context);
	}

	/*
	 * Peek the redo operation. Validity of the operation is not considered.
	 */

	public IOperation getRedoOperation(OperationContext context) {
		if (context == null) {
			if (fRedo.size() > 0) {
				IOperation operation = (IOperation) fRedo.get(fRedo.size() - 1);
				return operation;
			}
			return null;
		}

		for (int i = fRedo.size() - 1; i >= 0; i--) {
			IOperation operation = (IOperation) fRedo.get(i);
			if (operation.hasContext(context)) {
				return operation;
			}
		}
		return null;
	}

	protected IStatus getUndoApproval(IOperation operation) {
		for (int i = 0; i < fApprovers.size(); i++) {
			IStatus approval = ((IOperationApprover) fApprovers.get(i))
					.proceedUndoing(operation, this);
			if (!approval.isOK())
				return approval;
		}
		return Status.OK_STATUS;
	}

	/*
	 * null is a valid argument indicating the entire history
	 */
	public IOperation[] getUndoHistory(OperationContext context) {
		return filter(fUndo, context);
	}

	/*
	 * Validity of the returned operation is not considered.
	 */
	public IOperation getUndoOperation(OperationContext context) {
		if (context == null) {
			if (fUndo.size() > 0) {
				IOperation operation = (IOperation) fUndo.get(fUndo.size() - 1);
				return operation;
			}
			return null;
		}

		for (int i = fUndo.size() - 1; i >= 0; i--) {
			IOperation operation = (IOperation) fUndo.get(i);
			if (operation.hasContext(context)) {
				return operation;
			}
		}
		return null;
	}

	void internalRemove(IOperation operation) {
		operation.dispose();
		notifyRemoved(operation);
	}

	protected void notifyAboutToExecute(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_EXECUTE, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyAboutToRedo(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_REDO, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyAboutToUndo(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_UNDO, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyAdd(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_ADDED, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyDone(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.DONE, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyNotOK(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_NOT_OK, this, operation);

		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyRedone(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.REDONE, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyRemoved(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_REMOVED, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	protected void notifyUndone(IOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.UNDONE, this, operation);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	public IStatus redo(OperationContext context, IProgressMonitor monitor) {
		// null context is allowed and passed through when getting the operation
		IOperation operation = getRedoOperation(context);

		// info if there is no operation
		if (operation == null)
			return NOTHING_TO_REDO_STATUS;

		// error if operation is invalid
		if (!operation.canRedo())
			return OPERATION_INVALID_STATUS;

		return doRedo(monitor, operation, true);
	}

	public IStatus redoOperation(IOperation operation, IProgressMonitor monitor) {
		Assert.isNotNull(operation);
		IStatus status;
		if (operation.canRedo()) {
			status = getRedoApproval(operation);
			if (status.isOK()) {
				status = doRedo(monitor, operation, false);
			}
		} else {
			status = OPERATION_INVALID_STATUS;
		}
		return status;
	}

	public void remove(IOperation operation) {
		if (fUndo.contains(operation)) {
			fUndo.remove(operation);
			internalRemove(operation);
		} else if (fRedo.contains(operation)) {
			fRedo.remove(operation);
			internalRemove(operation);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#removeOperationApprover(org.eclipse.runtime.operations.IOperationApprover)
	 */
	public void removeOperationApprover(IOperationApprover approver) {
		fApprovers.remove(approver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#removeOperationHistoryListener(org.eclipse.runtime.operations.IOperationHistoryListener)
	 */
	public void removeOperationHistoryListener(
			IOperationHistoryListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#setLimit(int)
	 */
	public void setLimit(int limit) {
		if (limit <= 0)
			return;
		fLimit = limit;
		forceUndoLimit(limit);
		forceRedoLimit(limit);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.runtime.operations.IOperationHistory#undo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus undo(OperationContext context, IProgressMonitor monitor) {
		// null context is allowed and passed through when getting the operation
		IOperation operation = getUndoOperation(context);

		// info if there is no operation
		if (operation == null)
			return NOTHING_TO_UNDO_STATUS;

		// error if operation is invalid
		if (!operation.canUndo())
			return OPERATION_INVALID_STATUS;

		return doUndo(monitor, operation, true);
	}

	public IStatus undoOperation(IOperation operation, IProgressMonitor monitor) {
		Assert.isNotNull(operation);
		IStatus status;
		if (operation.canUndo()) {
			status = getUndoApproval(operation);
			if (status.isOK()) {
				status = doUndo(monitor, operation, false);
			}
		} else {
			status = OPERATION_INVALID_STATUS;
		}
		return status;
	}
}
