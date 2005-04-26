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
package org.eclipse.core.commands.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.commands.util.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * A base implementation of IOperationHistory that implements a linear undo and
 * redo model . The most recently added operation is available for undo, and the
 * most recently undone operation is available for redo.
 * </p>
 * <p>
 * If the operation eligible for undo is not in a state where it can be undone,
 * then no undo is available. No other operations are considered. Likewise, if
 * the operation available for redo cannot be redone, then no redo is available.
 * </p>
 * <p>
 * Implementations for the direct undo and redo of a specified operation are
 * available. If a strict linear undo is to be enforced, than an
 * IOperationApprover should be installed that prevents undo and redo of any
 * operation that is not the most recently undone or redone operation in all of
 * its undo contexts.
 * </p>
 * 
 * <p>
 * This implementation is not intended to be subclassed.
 * </p>
 * 
 * @see org.eclipse.core.commands.operations.IOperationHistory
 * @see org.eclipse.core.commands.operations.IOperationApprover
 * 
 * @since 3.1
 */
public class DefaultOperationHistory implements IOperationHistory {
	/**
	 * This flag can be set to <code>true</code> if the history should print
	 * information to <code>System.out</code> whenever notifications about
	 * changes to the history occur.
	 */
	public static boolean DEBUG_OPERATION_HISTORY_NOTIFICATION = false;

	/**
	 * This flag can be set to <code>true</code> if the history should print
	 * information to <code>System.out</code> whenever an unexpected condition
	 * arises.
	 */
	public static boolean DEBUG_OPERATION_HISTORY_UNEXPECTED = false;

	/**
	 * This flag can be set to <code>true</code> if the history should print
	 * information to <code>System.out</code> whenever an undo context is
	 * disposed.
	 */
	public static boolean DEBUG_OPERATION_HISTORY_DISPOSE = false;

	/**
	 * This flag can be set to <code>true</code> if the history should print
	 * information to <code>System.out</code> during the open/close sequence.
	 */
	public static boolean DEBUG_OPERATION_HISTORY_OPENOPERATION = false;

	/**
	 * This flag can be set to <code>true</code> if the history should print
	 * information to <code>System.out</code> whenever an operation is not
	 * approved.
	 */
	public static boolean DEBUG_OPERATION_HISTORY_APPROVAL = false;

	protected static final int DEFAULT_LIMIT = 20;

	/**
	 * the list of {@link IOperationApprover}s
	 */
	protected List approvers = new ArrayList();

	/**
	 * a map of undo limits per context
	 */
	private HashMap limits = new HashMap();

	/**
	 * the list of {@link IOperationHistoryListener}s
	 */
	protected List listeners = new ArrayList();

	/**
	 * the list of operations available for redo, LIFO
	 */
	private List redoList = new ArrayList();

	/**
	 * the list of operations available for undo, LIFO
	 */
	private List undoList = new ArrayList();

	/**
	 * An operation that is "absorbing" all other operations while it is open.
	 * When this is not null, other operations added or executed are added to
	 * this composite.
	 * 
	 */
	private ICompositeOperation openComposite = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#add(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void add(IUndoableOperation operation) {
		Assert.isNotNull(operation);

		/*
		 * If we are in the middle of executing an open batching operation, and
		 * this is not that operation, then we need only add the context of the
		 * new operation to the batch. The operation itself is disposed since we
		 * will never undo or redo it. We consider it to be triggered by the
		 * batching operation and assume that its undo will be triggered by the
		 * batching operation undo.
		 */
		if (openComposite != null && openComposite != operation) {
			openComposite.add(operation);
			return;
		}

		if (checkUndoLimit(operation)) {
			undoList.add(operation);
			notifyAdd(operation);

			// flush redo stack for related contexts
			IUndoContext[] contexts = operation.getContexts();
			for (int i = 0; i < contexts.length; i++) {
				flushRedo(contexts[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#addOperationApprover(org.eclipse.core.commands.operations.IOperationApprover)
	 */
	public void addOperationApprover(IOperationApprover approver) {
		approvers.add(approver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#addOperationHistoryListener(org.eclipse.core.commands.operations.IOperationHistoryListener)
	 */
	public void addOperationHistoryListener(IOperationHistoryListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#canRedo(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public boolean canRedo(IUndoContext context) {
		// null context is allowed and passed through
		IUndoableOperation operation = getRedoOperation(context);
		return (operation != null && operation.canRedo());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#canUndo(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public boolean canUndo(IUndoContext context) {
		// null context is allowed and passed through
		IUndoableOperation operation = getUndoOperation(context);
		return (operation != null && operation.canUndo());
	}

	/**
	 * Check the redo limit before adding an operation. In theory the redo limit
	 * should never be reached, because the redo items are transferred from the
	 * undo history, which has the same limit. The redo history is cleared
	 * whenever a new operation is added. We check for completeness since
	 * implementations may change over time.
	 * 
	 * Return a boolean indicating whether the redo should proceed.
	 */
	private boolean checkRedoLimit(IUndoableOperation operation) {
		IUndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			int limit = getLimit(contexts[i]);
			if (limit > 0) {
				forceRedoLimit(contexts[i], limit - 1);
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check the undo limit before adding an operation. Return a boolean
	 * indicating whether the undo should proceed.
	 */
	private boolean checkUndoLimit(IUndoableOperation operation) {
		IUndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			int limit = getLimit(contexts[i]);
			if (limit > 0) {
				forceUndoLimit(contexts[i], limit - 1);
			} else {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#dispose(org.eclipse.core.commands.operations.IUndoContext,
	 *      boolean, boolean, boolean)
	 */
	public void dispose(IUndoContext context, boolean flushUndo,
			boolean flushRedo, boolean flushContext) {
		// dispose of any limit that was set for the context if it is not to be
		// used again.
		if (flushContext) {
			if (DEBUG_OPERATION_HISTORY_DISPOSE) {
				System.out.print("OPERATIONHISTORY >>> Flushing context "); //$NON-NLS-1$ 
				System.out.print(context);
				System.out.println();
			}
			limits.remove(context);
			flushUndo(context);
			flushRedo(context);
			return;
		}
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
	private IStatus doRedo(IProgressMonitor monitor, IAdaptable info,
			IUndoableOperation operation)
			throws ExecutionException {

		IStatus status = getRedoApproval(operation, info);
		if (status.isOK()) {
			notifyAboutToRedo(operation);
			try {
				status = operation.redo(monitor, info);
			} catch (OperationCanceledException e) {
				status = Status.CANCEL_STATUS;
			} catch (ExecutionException e) {
				notifyNotOK(operation);
				if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
					System.out
							.print("OPERATIONHISTORY >>> ExecutionException while redoing "); //$NON-NLS-1$ 
					System.out.print(operation);
					System.out.println();
				}
				throw e;
			} catch (Exception e) {
				notifyNotOK(operation);
				if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
					System.out
							.print("OPERATIONHISTORY >>> Exception while redoing "); //$NON-NLS-1$ 
					System.out.print(operation);
					System.out.println();
				}
				throw new ExecutionException(
						"While redoing the operation, an exception occurred", e); //$NON-NLS-1$
			}
		}

		// if successful, the operation is removed from the redo history and
		// placed back in the undo history.
		if (status.isOK()) {
			redoList.remove(operation);
			// Only add the operation to the undo stack if it can indeed be
			// undone.
			// This conservatism is added to support the integration of existing
			// frameworks (such as Refactoring) that produce undo and redo
			// behavior
			// on the fly and cannot guarantee that a successful redo means a
			// successful undo will be available.
			// See bug #84444
			if (operation.canUndo() && checkUndoLimit(operation)) {
				undoList.add(operation);
			}
			// notify listeners must happen after history is updated
			notifyRedone(operation);
		} else {
			notifyNotOK(operation);
		}

		return status;
	}

	/**
	 * Perform the undo. All validity checks have already occurred.
	 * 
	 * @param monitor
	 * @param operation
	 */
	private IStatus doUndo(IProgressMonitor monitor, IAdaptable info,
			IUndoableOperation operation)
			throws ExecutionException {
		IStatus status = getUndoApproval(operation, info);
		if (status.isOK()) {
			notifyAboutToUndo(operation);
			try {
				status = operation.undo(monitor, info);
			} catch (OperationCanceledException e) {
				status = Status.CANCEL_STATUS;
			} catch (ExecutionException e) {
				notifyNotOK(operation);
				if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
					System.out
							.print("OPERATIONHISTORY >>> ExecutionException while undoing "); //$NON-NLS-1$ 
					System.out.print(operation);
					System.out.println();
				}
				throw e;
			} catch (Exception e) {
				notifyNotOK(operation);
				if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
					System.out
							.print("OPERATIONHISTORY >>> Exception while undoing "); //$NON-NLS-1$ 
					System.out.print(operation);
					System.out.println();
				}
				throw new ExecutionException(
						"While undoing the operation, an exception occurred", e); //$NON-NLS-1$
			}
		}
		// if successful, the operation is removed from the undo history and
		// placed in the redo history.
		if (status.isOK()) {
			undoList.remove(operation);
			// Only add the operation to the redo stack if it can indeed be
			// redone.
			// This conservatism is added to support the integration of existing
			// frameworks (such as Refactoring) that produce undo and redo
			// behavior
			// on the fly and cannot guarantee that a successful undo means a
			// successful redo will be available.
			// See bug #84444
			if (operation.canRedo() && checkRedoLimit(operation)) {
				redoList.add(operation);
			}
			// notification occurs after the undo and redo histories are
			// adjusted
			notifyUndone(operation);
		} else {
			notifyNotOK(operation);
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#execute(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IUndoableOperation operation,
			IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Assert.isNotNull(operation);

		// error if operation is invalid
		if (!operation.canExecute()) {
			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		/*
		 * If we are in the middle of an open batch, then we will add this
		 * operation's contexts to the open operation rather than add the
		 * operation to the history. We will still execute it, but will dispose
		 * of it afterward. Operations triggered during an operation are assumed
		 * to be model-related and we assume the undo of this related operation
		 * will be triggered automatically by the undo of the batching
		 * operation.
		 */
		boolean merging = false;
		if (openComposite != null) {
			// the composite shouldn't be executed explicitly while it is still
			// open
			if (openComposite == operation) {
				return IOperationHistory.OPERATION_INVALID_STATUS;
			}
			openComposite.add(operation);
			merging = true;
		}

		/*
		 * Execute the operation
		 */
		if (!merging)
			notifyAboutToExecute(operation);
		IStatus status;
		try {
			status = operation.execute(monitor, info);
		} catch (OperationCanceledException e) {
			status = Status.CANCEL_STATUS;
		} catch (ExecutionException e) {
			notifyNotOK(operation);
			throw e;
		} catch (Exception e) {
			notifyNotOK(operation);
			throw new ExecutionException(
					"While executing the operation, an exception occurred", e); //$NON-NLS-1$
		}

		// if successful, the notify listeners are notified and the operation is
		// added to the history
		if (!merging) {
			if (status.isOK()) {
				notifyDone(operation);
				// Only add the operation to the history if it can indeed be
				// undone.
				// This conservatism is added to support the integration of
				// existing
				// frameworks (such as Refactoring) that may be using a history
				// to execute
				// all operations, even those that are not undoable.
				// See bug #84444
				if (operation.canUndo()) {
					add(operation);
				}
			} else {
				notifyNotOK(operation);
			}
		} else {
			// dispose of this operation since it is not to be kept in the
			// history
			operation.dispose();
		}
		// all other severities are not interpreted. Simply return the status.
		return status;
	}

	/*
	 * Filter the specified list to include only the specified undo context.
	 */
	private IUndoableOperation[] filter(List list, IUndoContext context) {
		/*
		 * This method is used whenever there is a need to filter the undo or
		 * redo history on a particular context. Currently there are no caches
		 * kept to optimize repeated requests for the same filter. If benchmarks
		 * show this to be a common pattern that causes performances problems,
		 * we could implement a filtered cache here that is nullified whenever
		 * the global history changes.
		 */

		List filtered = new ArrayList();
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			IUndoableOperation operation = (IUndoableOperation) iterator.next();
			if (operation.hasContext(context)) {
				filtered.add(operation);
			}
		}
		return (IUndoableOperation[]) filtered
				.toArray(new IUndoableOperation[filtered.size()]);
	}

	/*
	 * Flush the redo stack of all operations that have the given context.
	 */
	private void flushRedo(IUndoContext context) {
		if (DEBUG_OPERATION_HISTORY_DISPOSE) {
			System.out.print("OPERATIONHISTORY >>> Flushing redo history for "); //$NON-NLS-1$ 
			System.out.print(context);
			System.out.println();
		}

		Object[] filtered = filter(redoList, context);
		for (int i = 0; i < filtered.length; i++) {
			IUndoableOperation operation = (IUndoableOperation) filtered[i];
			if (context == GLOBAL_UNDO_CONTEXT
					|| operation.getContexts().length == 1) {
				// remove the operation if it only has the context or we are
				// flushing all
				redoList.remove(operation);
				internalRemove(operation);
			} else {
				// remove the reference to the context
				operation.removeContext(context);
			}
		}
	}

	/*
	 * Flush the undo stack of all operations that have the given context.
	 */
	private void flushUndo(IUndoContext context) {
		if (DEBUG_OPERATION_HISTORY_DISPOSE) {
			System.out.print("OPERATIONHISTORY >>> Flushing undo history for "); //$NON-NLS-1$ 
			System.out.print(context);
			System.out.println();
		}

		Object[] filtered = filter(undoList, context);
		for (int i = 0; i < filtered.length; i++) {
			IUndoableOperation operation = (IUndoableOperation) filtered[i];
			if (context == GLOBAL_UNDO_CONTEXT
					|| operation.getContexts().length == 1) {
				// remove the operation if it only has the context or we are
				// flushing all
				undoList.remove(operation);
				internalRemove(operation);
			} else {
				// remove the reference to the context
				operation.removeContext(context);
			}
		}
		/*
		 * There may be an open composite. If it has this context, then the
		 * context must be removed.  If it has only this context or we are flushing
		 * all operations, then null it out and notify that we are ending it.  
		 * We don't remove it since it was never added.
		 */ 
		if (openComposite != null) {
			if (openComposite.hasContext(context)) {
				if (context == GLOBAL_UNDO_CONTEXT || openComposite.getContexts().length == 1) {
					notifyNotOK(openComposite);
					openComposite = null;
				} else {
					openComposite.removeContext(context);
				}
			}
		}
	}

	/*
	 * Force the redo history for the given context to contain max or less
	 * items.
	 */
	private void forceRedoLimit(IUndoContext context, int max) {
		Object[] filtered = filter(redoList, context);
		int size = filtered.length;
		if (size > 0) {
			int index = 0;
			while (size > max) {
				IUndoableOperation removed = (IUndoableOperation) filtered[index];
				if (context == GLOBAL_UNDO_CONTEXT
						|| removed.getContexts().length == 1) {
					/*
					 * remove the operation if we are enforcing a global limit
					 * or if the operation only has the specified context
					 */
					redoList.remove(removed);
					internalRemove(removed);
				} else {
					/*
					 * if the operation has multiple contexts and we've reached
					 * the limit for only one of them, then just remove the
					 * context, not the operation.
					 */
					removed.removeContext(context);
				}
				size--;
				index++;
			}
		}
	}

	/*
	 * Force the undo history for the given context to contain max or less
	 * items.
	 */
	private void forceUndoLimit(IUndoContext context, int max) {
		Object[] filtered = filter(undoList, context);
		int size = filtered.length;
		if (size > 0) {
			int index = 0;
			while (size > max) {
				IUndoableOperation removed = (IUndoableOperation) filtered[index];
				if (context == GLOBAL_UNDO_CONTEXT
						|| removed.getContexts().length == 1) {
					/*
					 * remove the operation if we are enforcing a global limit
					 * or if the operation only has the specified context
					 */
					undoList.remove(removed);
					internalRemove(removed);
				} else {
					/*
					 * if the operation has multiple contexts and we've reached
					 * the limit for only one of them, then just remove the
					 * context, not the operation.
					 */
					removed.removeContext(context);
				}
				size--;
				index++;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getLimit()
	 */
	public int getLimit(IUndoContext context) {
		if (!limits.containsKey(context)) {
			return DEFAULT_LIMIT;
		}
		return ((Integer) (limits.get(context))).intValue();
	}

	/*
	 * Consult the IOperationApprovers to see if the proposed redo should be
	 * allowed.
	 */
	private IStatus getRedoApproval(IUndoableOperation operation,
			IAdaptable info) {
		for (int i = 0; i < approvers.size(); i++) {
			IOperationApprover approver = (IOperationApprover) approvers.get(i);
			IStatus approval = approver.proceedRedoing(operation, this, info);
			if (!approval.isOK()) {
				if (DEBUG_OPERATION_HISTORY_APPROVAL) {
					System.out
							.print("OPERATIONHISTORY >>> Redo not approved by "); //$NON-NLS-1$ 
					System.out.print(approver);
					System.out.print("for operation "); //$NON-NLS-1$ 
					System.out.print(operation);
					System.out.print(approval);
					System.out.println();
				}
				return approval;
			}
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getRedoHistory(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public IUndoableOperation[] getRedoHistory(IUndoContext context) {
		Assert.isNotNull(context);
		return filter(redoList, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getOperation(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public IUndoableOperation getRedoOperation(IUndoContext context) {
		Assert.isNotNull(context);
		for (int i = redoList.size() - 1; i >= 0; i--) {
			IUndoableOperation operation = (IUndoableOperation) redoList.get(i);
			if (operation.hasContext(context)) {
				return operation;
			}
		}
		return null;
	}

	/*
	 * Consult the IOperationApprovers to see if the proposed undo should be
	 * allowed.
	 */
	private IStatus getUndoApproval(IUndoableOperation operation,
			IAdaptable info) {
		for (int i = 0; i < approvers.size(); i++) {
			IOperationApprover approver = (IOperationApprover) approvers.get(i);
			IStatus approval = approver.proceedUndoing(operation, this, info);
			if (!approval.isOK()) {
				if (DEBUG_OPERATION_HISTORY_APPROVAL) {
					System.out
							.print("OPERATIONHISTORY >>> Undo not approved by "); //$NON-NLS-1$ 
					System.out.print(approver);
					System.out.print("for operation "); //$NON-NLS-1$ 
					System.out.print(operation);
					System.out.print(" with status "); //$NON-NLS-1$ 
					System.out.print(approval);
					System.out.println();
				}
				return approval;
			}
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getUndoHistory(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public IUndoableOperation[] getUndoHistory(IUndoContext context) {
		Assert.isNotNull(context);
		return filter(undoList, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getUndoOperation(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public IUndoableOperation getUndoOperation(IUndoContext context) {
		Assert.isNotNull(context);
		for (int i = undoList.size() - 1; i >= 0; i--) {
			IUndoableOperation operation = (IUndoableOperation) undoList.get(i);
			if (operation.hasContext(context)) {
				return operation;
			}
		}
		return null;
	}

	/*
	 * Remove the operation by disposing it and notifying listeners.
	 */
	private void internalRemove(IUndoableOperation operation) {
		operation.dispose();
		notifyRemoved(operation);
	}

	/*
	 * Notify listeners that an operation is about to execute.
	 */
	private void notifyAboutToExecute(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> ABOUT_TO_EXECUTE "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_EXECUTE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation is about to redo.
	 */
	private void notifyAboutToRedo(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> ABOUT_TO_REDO "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_REDO, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation is about to undo.
	 */
	private void notifyAboutToUndo(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> ABOUT_TO_UNDO "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_UNDO, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}

	}

	/*
	 * Notify listeners that an operation has been added.
	 */
	private void notifyAdd(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> OPERATION_ADDED "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_ADDED, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation is done executing.
	 */
	private void notifyDone(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> DONE "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.DONE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation did not succeed after an attempt to
	 * execute, undo, or redo was made.
	 */
	private void notifyNotOK(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> OPERATION_NOT_OK "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_NOT_OK, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation was redone.
	 */
	private void notifyRedone(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> REDONE "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.REDONE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation has been removed from the history.
	 */
	private void notifyRemoved(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> OPERATION_REMOVED "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_REMOVED, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation has been undone.
	 */
	private void notifyUndone(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> UNDONE "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.UNDONE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * Notify listeners that an operation has been undone.
	 */
	private void notifyChanged(IUndoableOperation operation) {
		if (DEBUG_OPERATION_HISTORY_NOTIFICATION) {
			System.out.print("OPERATIONHISTORY >>> OPERATION_CHANGED "); //$NON-NLS-1$ 
			System.out.print(operation);
			System.out.println();
		}

		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_CHANGED, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < listeners.size(); i++)
			try {
				((IOperationHistoryListener) listeners.get(i))
						.historyNotification(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
	}

	/*
	 * A history notification is about to be sent. Notify the operation before
	 * hand if it implements IHistoryNotificationAwareOperation.
	 * 
	 * This method is provided for legacy undo frameworks that rely on
	 * notification from their undo managers before any listeners are notified
	 * about changes in the operation.
	 */

	private void preNotifyOperation(IUndoableOperation operation,
			OperationHistoryEvent event) {

		if (operation instanceof IHistoryNotificationAwareOperation) {
			try {
				((IHistoryNotificationAwareOperation) operation)
						.aboutToNotify(event);
			} catch (Exception e) {
				handleNotificationException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#redo(org.eclipse.core.commands.operations.IUndoContext,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus redo(IUndoContext context, IProgressMonitor monitor,
			IAdaptable info) throws ExecutionException {
		Assert.isNotNull(context);
		IUndoableOperation operation = getRedoOperation(context);

		// info if there is no operation
		if (operation == null)
			return IOperationHistory.NOTHING_TO_REDO_STATUS;

		// error if operation is invalid
		if (!operation.canRedo()) {
			if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
				System.out
						.print("OPERATIONHISTORY >>> Redo operation not valid - "); //$NON-NLS-1$ 
				System.out.print(operation);
				System.out.println();
			}

			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		return doRedo(monitor, info, operation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#redoOperation(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */

	public IStatus redoOperation(IUndoableOperation operation,
			IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Assert.isNotNull(operation);
		IStatus status;
		if (operation.canRedo()) {
			status = getRedoApproval(operation, info);
			if (status.isOK()) {
				status = doRedo(monitor, info, operation);
			}
		} else {
			if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
				System.out
						.print("OPERATIONHISTORY >>> Redo operation not valid - "); //$NON-NLS-1$ 
				System.out.print(operation);
				System.out.println();
			}

			status = IOperationHistory.OPERATION_INVALID_STATUS;
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#removeOperationApprover(org.eclipse.core.commands.operations.IOperationApprover)
	 */
	public void removeOperationApprover(IOperationApprover approver) {
		approvers.remove(approver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#removeOperationHistoryListener(org.eclipse.core.commands.operations.IOperationHistoryListener)
	 */
	public void removeOperationHistoryListener(
			IOperationHistoryListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#replaceOperation(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IUndoableOperation [])
	 */
	public void replaceOperation(IUndoableOperation operation,
			IUndoableOperation[] replacements) {
		// check the undo history first.
		int index = undoList.indexOf(operation);
		if (index > -1) {
			undoList.remove(operation);
			internalRemove(operation);
			ArrayList allContexts = new ArrayList(replacements.length);
			for (int i = 0; i < replacements.length; i++) {
				IUndoContext[] opContexts = replacements[i].getContexts();
				for (int j = 0; j < opContexts.length; j++) {
					allContexts.add(opContexts[j]);
				}
				undoList.add(index, replacements[i]);
				notifyAdd(replacements[i]);
			}
			// recheck all the limits. We do this at the end so the index
			// doesn't change during replacement
			for (int i = 0; i < allContexts.size(); i++) {
				IUndoContext context = (IUndoContext) allContexts.get(i);
				forceUndoLimit(context, getLimit(context));
			}

			// look in the redo history
		} else {
			index = redoList.indexOf(operation);
			if (index == -1)
				return;
			ArrayList allContexts = new ArrayList(replacements.length);
			redoList.remove(operation);
			internalRemove(operation);
			for (int i = 0; i < replacements.length; i++) {
				IUndoContext[] opContexts = replacements[i].getContexts();
				for (int j = 0; j < opContexts.length; j++) {
					allContexts.add(opContexts[j]);
				}
				redoList.add(index, replacements[i]);
				notifyAdd(replacements[i]);
			}
			// recheck all the limits. We do this at the end so the index
			// doesn't change during replacement
			for (int i = 0; i < allContexts.size(); i++) {
				IUndoContext context = (IUndoContext) allContexts.get(i);
				forceRedoLimit(context, getLimit(context));
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#setLimit(org.eclipse.core.commands.operations.IUndoContext,
	 *      int)
	 */
	public void setLimit(IUndoContext context, int limit) {
		Assert.isTrue(limit >= 0);
		/*
		 * The limit checking methods interpret a null context as a global limit
		 * to be enforced. We do not wish to support a global limit in this
		 * implementation, so we throw an exception for a null context. The rest
		 * of the implementation can handle a null context, so subclasses can
		 * override this if a global limit is desired.
		 */
		Assert.isNotNull(context);
		limits.put(context, new Integer(limit));
		forceUndoLimit(context, limit);
		forceRedoLimit(context, limit);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#undo(org.eclipse.core.commands.operations.IUndoContext,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IUndoContext context, IProgressMonitor monitor,
			IAdaptable info) throws ExecutionException {
		Assert.isNotNull(context);
		IUndoableOperation operation = getUndoOperation(context);

		// info if there is no operation
		if (operation == null)
			return IOperationHistory.NOTHING_TO_UNDO_STATUS;

		// error if operation is invalid
		if (!operation.canUndo()) {
			if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
				System.out
						.print("OPERATIONHISTORY >>> Undo operation not valid - "); //$NON-NLS-1$ 
				System.out.print(operation);
				System.out.println();
			}
			return IOperationHistory.OPERATION_INVALID_STATUS;
		}

		return doUndo(monitor, info, operation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#undoOperation(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undoOperation(IUndoableOperation operation,
			IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		Assert.isNotNull(operation);
		IStatus status;
		if (operation.canUndo()) {
			status = getUndoApproval(operation, info);
			if (status.isOK()) {
				status = doUndo(monitor, info, operation);
			}
		} else {
			if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
				System.out
						.print("OPERATIONHISTORY >>> Undo operation not valid - "); //$NON-NLS-1$ 
				System.out.print(operation);
				System.out.println();
			}
			status = IOperationHistory.OPERATION_INVALID_STATUS;
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#openOperation(org.eclipse.core.commands.operations.ICompositeOperation)
	 */
	public void openOperation(ICompositeOperation operation, int mode) {
		if (openComposite != null && openComposite != operation) {
			// unexpected nesting of operations. 
			if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
				System.out
						.print("OPERATIONHISTORY >>> Open operation called while another operation is open.  old: "); //$NON-NLS-1$ 
				System.out.print(openComposite);
				System.out.print("new:  "); //$NON-NLS-1$
				System.out.print(operation);
				System.out.println();
			}

			throw new IllegalStateException("Cannot open an operation while one is already open"); //$NON-NLS-1$
		}
		openComposite = operation;
		if (DEBUG_OPERATION_HISTORY_OPENOPERATION) {
			System.out.print("OPERATIONHISTORY >>> Opening operation "); //$NON-NLS-1$ 
			System.out.print(openComposite);
			System.out.println();
		}

		if (mode == EXECUTE) {
			notifyAboutToExecute(openComposite);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#closeOperation(boolean,
	 *      boolean)
	 */
	public void closeOperation(boolean operationOK, boolean addToHistory,
			int mode) {
		if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
			if (openComposite == null) {
				System.out
						.print("OPERATIONHISTORY >>> Attempted to close operation when none was open "); //$NON-NLS-1$ 
				System.out.println();
				return;
			}
		}
		if (openComposite != null) {
			if (DEBUG_OPERATION_HISTORY_OPENOPERATION) {
				System.out.print("OPERATIONHISTORY >>> Closing operation "); //$NON-NLS-1$ 
				System.out.print(openComposite);
				System.out.println();
			}
			// any mode other than EXECUTE was triggered by a request to undo or
			// redo something already in the history, so undo and redo
			// notification will occur at
			// the end of that sequence.
			if (operationOK) {
				if (mode == EXECUTE)
					notifyDone(openComposite);
				if (addToHistory)
					add(openComposite);
			} else {
				if (mode == EXECUTE)
					notifyNotOK(openComposite);
			}
			openComposite = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#operationChanged(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void operationChanged(IUndoableOperation operation) {
		if (undoList.contains(operation) || redoList.contains(operation)) {
			notifyChanged(operation);
		}
	}

	/*
	 * Handle an exception that occurred while sending a notification about
	 * something happening in the operation history. When notifications fail,
	 * execution should continue, but the exception should be logged.
	 */
	private void handleNotificationException(Throwable e) {
		if (e instanceof OperationCanceledException)
			return;
		// This plug-in is intended to run stand-alone outside of the
		// platform, so we do not employ standard platform exception logging.
		if (DEBUG_OPERATION_HISTORY_UNEXPECTED) {
			System.out
					.print("OPERATIONHISTORY >>> Exception during notification callback "); //$NON-NLS-1$ 
			System.out.print(e);
			System.out.println();
		}
		e.printStackTrace();
	}

}
