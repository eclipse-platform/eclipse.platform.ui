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
 * A base implementation of IOperationHistory. DefaultOperationHistory implements a
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
public class DefaultOperationHistory implements IOperationHistory {
	
	protected static final int DEFAULT_LIMIT = 20;
	
	/**
	 * An operation info status describing the condition that there is no available
	 * operation for redo.
	 */
	public static final IStatus NOTHING_TO_REDO_STATUS = new OperationStatus(
			IStatus.INFO, OperationStatus.NOTHING_TO_REDO,
			"No operation to redo"); //$NON-NLS-1$

	/**
	 * An operation info status describing the condition that there is no available
	 * operation for undo.
	 */
	public static final IStatus NOTHING_TO_UNDO_STATUS = new OperationStatus(
			IStatus.INFO, OperationStatus.NOTHING_TO_UNDO,
			"No operation to undo"); //$NON-NLS-1$

	/**
	 * An operation error status describing the condition that the operation available
	 * for execution, undo or redo is not in a valid state for the action to be
	 * performed.
	 */
	public static final IStatus OPERATION_INVALID_STATUS = new OperationStatus(
			IStatus.ERROR, OperationStatus.OPERATION_INVALID,
			"Operation is not valid"); //$NON-NLS-1$

	/**
	 * the list of {@link IOperationApprover}s
	 */
	protected List fApprovers = new ArrayList();

	/**
	 * a map of undo limits per context
	 */
	private HashMap fLimits = new HashMap();

	/**
	 * the list of {@link IOperationHistoryListener}s
	 */
	protected List fListeners = new ArrayList();

	/**
	 * the list of operations available for redo, LIFO
	 */
	private List fRedo = new ArrayList();

	/**
	 * the list of operations available for undo, LIFO
	 */
	private List fUndo = new ArrayList();
	
	/**
	 * an operation that is "absorbing" all other operations while
	 * it is open.  When this is not null, other operations added or
	 * executed are ignored and assumed to be triggered by the batching
	 * 
	 */
	private IUndoableOperation fBatchingOperation = null;

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#add(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void add(IUndoableOperation operation) {
		Assert.isNotNull(operation);
		
		/*
		 * If we are in the middle of executing an open batching operation, and this is not that
		 * operation, then we need only add the context of the new operation to the batch.  
		 * The operation itself is disposed since we will never undo or redo it.  We consider
		 * it to be triggered by the batching operation and assume that its undo will be triggered
		 * by the batching operation undo.  
		 */
		if (fBatchingOperation != null && fBatchingOperation != operation) {
			IUndoContext[] contexts = operation.getContexts();
			for (int i = 0; i < contexts.length; i++) {
				fBatchingOperation.addContext(contexts[i]);
			}
			operation.dispose();
			return;
		}
		
		// flush redo stack for related contexts
		IUndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			flushRedo(contexts[i]);
		}

		checkUndoLimit(operation);
		fUndo.add(operation);
		notifyAdd(operation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#addOperationApprover(org.eclipse.core.commands.operations.IOperationApprover)
	 */
	public void addOperationApprover(IOperationApprover approver) {
		fApprovers.add(approver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#addOperationHistoryListener(org.eclipse.core.commands.operations.IOperationHistoryListener)
	 */
	public void addOperationHistoryListener(IOperationHistoryListener listener) {
		fListeners.add(listener);
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
	 * Check the redo limit before adding an operation.  In theory the redo limit
	 * should never be reached, because the redo items are transferred from the undo
	 * history, which has the same limit.  The redo history is cleared whenever a new
	 * operation is added.  We check for completeness since implementations may change
	 * over time.
	 */
	private void checkRedoLimit(IUndoableOperation operation) {
		IUndoContext [] contexts = operation.getContexts();
		for (int i=0; i<contexts.length; i++) {
			int limit = getLimit(contexts[i]);
			if (limit > 0) forceRedoLimit(contexts[i], limit - 1);
		}
	}

	/**
	 * Check the undo limit before adding an operation.
	 */
	private void checkUndoLimit(IUndoableOperation operation) {
		IUndoContext [] contexts = operation.getContexts();
		for (int i=0; i<contexts.length; i++) {
			int limit = getLimit(contexts[i]);
			if (limit > 0) forceUndoLimit(contexts[i], limit - 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#dispose(org.eclipse.core.commands.operations.IUndoContext, boolean, boolean)
	 */
	public void dispose(IUndoContext context, boolean flushUndo,
			boolean flushRedo) {
		if (flushUndo)
			flushUndo(context);
		if (flushRedo)
			flushRedo(context);
		// we currently do not dispose of any limit that was set for the
		// context since it may be used again. 
		
	}

	/**
	 * Perform the redo. All validity checks have already occurred.
	 * 
	 * @param monitor
	 * @param operation
	 */
	protected IStatus doRedo(IProgressMonitor monitor, IAdaptable info, IUndoableOperation operation,
			boolean flushOnError) throws ExecutionException{

		IStatus status = getRedoApproval(operation, info);
		if (status.isOK()) {
			notifyAboutToRedo(operation);
			try {
				status = operation.redo(monitor, info);
			} catch (OperationCanceledException e) {
				status = Status.CANCEL_STATUS;
			}
		}

		// if successful, the operation is removed from the redo history and
		// placed back in the undo history.
		if (status.isOK()) {
			fRedo.remove(operation);
			// Only add the operation to the undo stack if it can indeed be undone.
			// This conservatism is added to support the integration of existing 
			// frameworks (such as Refactoring) that produce undo and redo behavior
			// on the fly and cannot guarantee that a successful redo means a
			// successful undo will be available. 
			// See bug #84444
			if (operation.canUndo()) {
				checkUndoLimit(operation);
				fUndo.add(operation);
			}
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
	protected IStatus doUndo(IProgressMonitor monitor, IAdaptable info, IUndoableOperation operation,
			boolean flushOnError) throws ExecutionException {
		IStatus status = getUndoApproval(operation, info);
		if (status.isOK()) {
			notifyAboutToUndo(operation);
			try {
				status = operation.undo(monitor, info);
			} catch (OperationCanceledException e) {
				status = Status.CANCEL_STATUS;
			}
		}
		// if successful, the operation is removed from the undo history and
		// placed in the redo history.
		if (status.isOK()) {
			fUndo.remove(operation);
			// Only add the operation to the redo stack if it can indeed be redone.
			// This conservatism is added to support the integration of existing 
			// frameworks (such as Refactoring) that produce undo and redo behavior
			// on the fly and cannot guarantee that a successful undo means a
			// successful redo will be available. 
			// See bug #84444
			if (operation.canRedo()) {
				checkRedoLimit(operation);
				fRedo.add(operation);
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#execute(org.eclipse.core.commands.operations.IUndoableOperation, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus execute(IUndoableOperation operation, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Assert.isNotNull(operation);
		
		// error if operation is invalid
		if (!operation.canExecute()) {
			return OPERATION_INVALID_STATUS;
		}
		
		/*
		 * If we are in the middle of an open batch, then we will add this operation's contexts
		 * to the open operation rather than add the operation to the history.  We will still
		 * execute it, but will dispose of it afterward.  Operations triggered during an operation
		 * are assumed to be model-related and we assume the undo of this related operation will be
		 * triggered automatically by the undo of the batching operation.
		 */
		boolean merging = false;
		if (fBatchingOperation != null) {
			// the composite shouldn't be executed explicitly while it is still open
			if (fBatchingOperation == operation) {
				return OPERATION_INVALID_STATUS;
			}
			IUndoContext[] contexts = operation.getContexts();
			for (int i = 0; i < contexts.length; i++) {
				fBatchingOperation.addContext(contexts[i]);
			}
			merging = true;
		}

		/*
		 * Execute the operation
		 */
		if (!merging) notifyAboutToExecute(operation);
		IStatus status;
		try {
			status = operation.execute(monitor, info);
		} catch (OperationCanceledException e) {
			status = Status.CANCEL_STATUS;
		}

		// if successful, the notify listeners are notified and the operation is
		// added to the history
		if (!merging) {
			if (status.isOK()) {
				notifyDone(operation);
				// Only add the operation to the history if it can indeed be undone.
				// This conservatism is added to support the integration of existing 
				// frameworks (such as Refactoring) that may be using a history to execute 
				// all operations, even those that are not undoable.  
				// See bug #84444
				if (operation.canUndo()) {
					add(operation);
				}
			} else {
				notifyNotOK(operation);
			}
		} else {
			// dispose of this operation since it is not to be kept in the history
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
		 * redo history on a particular context.  Currently there are no caches
		 * kept to optimize repeated requests for the same filter.  If benchmarks
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
		return (IUndoableOperation[]) filtered.toArray(new IUndoableOperation[filtered.size()]);
	}

	/*
	 * Flush the redo stack of all operations that have the given context.
	 */
	void flushRedo(IUndoContext context) {
		Object[] filtered = filter(fRedo, context);
		for (int i = 0; i < filtered.length; i++) {
			IUndoableOperation operation = (IUndoableOperation) filtered[i];
			if (context == GLOBAL_UNDO_CONTEXT || operation.getContexts().length == 1) {
				// remove the operation if it only has the context or we are flushing all
				fRedo.remove(operation);
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
	void flushUndo(IUndoContext context) {
		Object[] filtered = filter(fUndo, context);
		for (int i = 0; i < filtered.length; i++) {
			IUndoableOperation operation = (IUndoableOperation) filtered[i];
			if (context == GLOBAL_UNDO_CONTEXT || operation.getContexts().length == 1) {
				// remove the operation if it only has the context or we are flushing all
				fUndo.remove(operation);
				internalRemove(operation);
			} else {
				// remove the reference to the context
				operation.removeContext(context);
			}
		}
		// there may be an open batch.  Notify listeners that it did not complete.
		// Since we did not add it, there's no need to notify of its removal.
		if (fBatchingOperation != null) {
			if (fBatchingOperation.hasContext(context)) {
				notifyNotOK(fBatchingOperation);
				fBatchingOperation = null;
			} 
		}
	}

	/*
	 * Force the redo history for the given context to contain max or less items.
	 */
	private void forceRedoLimit(IUndoContext context, int max) {
		Object[] filtered = filter(fRedo, context);
		int size = filtered.length;
		if (size > 0) {
			int index = 0;
			while (size > max) {
				IUndoableOperation removed = (IUndoableOperation)filtered[index];
				if (context == GLOBAL_UNDO_CONTEXT || removed.getContexts().length == 1) {
					/* remove the operation if we are enforcing a global limit or if
					 * the operation only has the specified context
					 */
					fRedo.remove(removed);
					internalRemove(removed);
				} else {
					/* if the operation has multiple contexts and we've reached the limit 
					 * for only one of them, then just remove the context, not the operation.
					 */
					removed.removeContext(context);
				}
				size--;
				index++;
			}
		}
	}

	/*
	 * Force the undo history for the given context to contain max or less items.
	 */
	private void forceUndoLimit(IUndoContext context, int max) {
		Object[] filtered = filter(fUndo, context);
		int size = filtered.length;
		if (size > 0) {
			int index = 0;
			while (size > max) {
				IUndoableOperation removed = (IUndoableOperation)filtered[index];
				if (context == GLOBAL_UNDO_CONTEXT || removed.getContexts().length == 1) {
					/* remove the operation if we are enforcing a global limit or if
					 * the operation only has the specified context
					 */
					fUndo.remove(removed);
					internalRemove(removed);
				} else {
					/* if the operation has multiple contexts and we've reached the limit 
					 * for only one of them, then just remove the context, not the operation.
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
		if (!fLimits.containsKey(context)) {
			return DEFAULT_LIMIT;
		}
		return ((Integer)(fLimits.get(context))).intValue();
	}

	/*
	 * Consult the IOperationApprovers to see if the proposed redo should be allowed.
	 */
	protected IStatus getRedoApproval(IUndoableOperation operation, IAdaptable info) {
		for (int i = 0; i < fApprovers.size(); i++) {
			IStatus approval = ((IOperationApprover) fApprovers.get(i))
					.proceedRedoing(operation, this, info);
			if (!approval.isOK())
				return approval;
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
		return filter(fRedo, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getOperation(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public IUndoableOperation getRedoOperation(IUndoContext context) {
		Assert.isNotNull(context);
		for (int i = fRedo.size() - 1; i >= 0; i--) {
			IUndoableOperation operation = (IUndoableOperation) fRedo.get(i);
			if (operation.hasContext(context)) {
				return operation;
			}
		}
		return null;
	}

	/*
	 * Consult the IOperationApprovers to see if the proposed undo should be allowed.
	 */
	protected IStatus getUndoApproval(IUndoableOperation operation, IAdaptable info) {
		for (int i = 0; i < fApprovers.size(); i++) {
			IStatus approval = ((IOperationApprover) fApprovers.get(i))
					.proceedUndoing(operation, this, info);
			if (!approval.isOK())
				return approval;
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
		return filter(fUndo, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#getUndoOperation(org.eclipse.core.commands.operations.IUndoContext)
	 */
	public IUndoableOperation getUndoOperation(IUndoContext context) {
		Assert.isNotNull(context);
		for (int i = fUndo.size() - 1; i >= 0; i--) {
			IUndoableOperation operation = (IUndoableOperation) fUndo.get(i);
			if (operation.hasContext(context)) {
				return operation;
			}
		}
		return null;
	}

	/*
	 * Remove the operation by disposing it and notifying listeners.
	 */
	void internalRemove(IUndoableOperation operation) {
		operation.dispose();
		notifyRemoved(operation);
	}

	/*
	 * Notify listeners that an operation is about to execute.
	 */
	protected void notifyAboutToExecute(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_EXECUTE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation is about to redo.
	 */
	protected void notifyAboutToRedo(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_REDO, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}
	
	/*
	 * Notify listeners that an operation is about to undo.
	 */
	protected void notifyAboutToUndo(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.ABOUT_TO_UNDO, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation has been added.
	 */
	protected void notifyAdd(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_ADDED, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation is done executing.
	 */
	protected void notifyDone(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.DONE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation did not succeed after
	 * an attempt to execute, undo, or redo was made.
	 */
	protected void notifyNotOK(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_NOT_OK, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation was redone.
	 */
	protected void notifyRedone(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.REDONE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation has been removed from the
	 * history.
	 */
	protected void notifyRemoved(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_REMOVED, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * Notify listeners that an operation has been undone.
	 */
	protected void notifyUndone(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.UNDONE, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}
	
	/*
	 * Notify listeners that an operation has been undone.
	 */
	protected void notifyChanged(IUndoableOperation operation) {
		OperationHistoryEvent event = new OperationHistoryEvent(
				OperationHistoryEvent.OPERATION_CHANGED, this, operation);
		preNotifyOperation(operation, event);
		for (int i = 0; i < fListeners.size(); i++)
			((IOperationHistoryListener) fListeners.get(i))
					.historyNotification(event);
	}

	/*
	 * A history notification is about to be sent.  Notify the operation before hand
	 * if it implements IHistoryNotificationAwareOperation.
	 * 
	 * This method is provided for legacy undo frameworks that rely on notification
	 * from their undo managers before any listeners are notified about changes
	 * in the operation.
	 */

	private void preNotifyOperation(IUndoableOperation operation, OperationHistoryEvent event) {
		if (operation instanceof IHistoryNotificationAwareOperation) {
			((IHistoryNotificationAwareOperation)operation).aboutToNotify(event);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#redo(org.eclipse.core.commands.operations.IUndoContext, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus redo(IUndoContext context, IProgressMonitor monitor, IAdaptable info) throws ExecutionException{
		Assert.isNotNull(context);
		IUndoableOperation operation = getRedoOperation(context);

		// info if there is no operation
		if (operation == null)
			return NOTHING_TO_REDO_STATUS;

		// error if operation is invalid
		if (!operation.canRedo())
			return OPERATION_INVALID_STATUS;

		return doRedo(monitor, info, operation, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#redoOperation(org.eclipse.core.commands.operations.IUndoableOperation, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */

	public IStatus redoOperation(IUndoableOperation operation, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Assert.isNotNull(operation);
		IStatus status;
		if (operation.canRedo()) {
			status = getRedoApproval(operation, info);
			if (status.isOK()) {
				status = doRedo(monitor, info, operation, false);
			}
		} else {
			status = OPERATION_INVALID_STATUS;
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#remove(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void remove(IUndoableOperation operation) {
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
	 * @see org.eclipse.core.commands.operations.IOperationHistory#removeOperationApprover(org.eclipse.core.commands.operations.IOperationApprover)
	 */
	public void removeOperationApprover(IOperationApprover approver) {
		fApprovers.remove(approver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#removeOperationHistoryListener(org.eclipse.core.commands.operations.IOperationHistoryListener)
	 */
	public void removeOperationHistoryListener(
			IOperationHistoryListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#setLimit(org.eclipse.core.commands.operations.IUndoContext, int)
	 */
	public void setLimit(IUndoContext context, int limit) {
		Assert.isTrue(limit > 0);
		/*
		 * The limit checking methods interpret a null context as a global limit to be
		 * enforced.  We do not wish to support a global limit in this implementation,
		 * so we throw an exception for a null context.  The rest of the implementation
		 * can handle a null context, so subclasses can override this if a global limit
		 * is desired.
		 */	
		Assert.isNotNull(context);
		fLimits.put(context, new Integer(limit));
		forceUndoLimit(context, limit);
		forceRedoLimit(context, limit);

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#undo(org.eclipse.core.commands.operations.IUndoContext, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undo(IUndoContext context, IProgressMonitor monitor, IAdaptable info) throws ExecutionException{
		Assert.isNotNull(context);
		IUndoableOperation operation = getUndoOperation(context);

		// info if there is no operation
		if (operation == null)
			return NOTHING_TO_UNDO_STATUS;

		// error if operation is invalid
		if (!operation.canUndo())
			return OPERATION_INVALID_STATUS;

		return doUndo(monitor, info, operation, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#undoOperation(org.eclipse.core.commands.operations.IUndoableOperation, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus undoOperation(IUndoableOperation operation, IProgressMonitor monitor, IAdaptable info) throws ExecutionException{
		Assert.isNotNull(operation);
		IStatus status;
		if (operation.canUndo()) {
			status = getUndoApproval(operation, info);
			if (status.isOK()) {
				status = doUndo(monitor, info, operation, false);
			}
		} else {
			status = OPERATION_INVALID_STATUS;
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#openOperation(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void openOperation(IUndoableOperation operation) {
		if (fBatchingOperation != null) {
			// unexpected nesting of operations.  The original batching operation
			// will be closed and will be considered unsuccessful.
			closeOperation(false, false);	
		} 
		fBatchingOperation = operation;
		notifyAboutToExecute(operation);	
	}
	
	/**
	 * @see org.eclipse.core.commands.operations.IOperationHistory#closeOperation()
	 * @deprecated
	 */
	public void closeOperation() {
		closeOperation(true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#closeOperation(boolean, boolean)
	 */
	public void closeOperation(boolean operationOK, boolean addToHistory) {
		if (fBatchingOperation != null) {
			if (operationOK) {
				notifyDone(fBatchingOperation);
				if (addToHistory) add(fBatchingOperation);
			} else {
				notifyNotOK(fBatchingOperation);
			}
			fBatchingOperation = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationHistory#operationChanged(org.eclipse.core.commands.operations.IUndoableOperation)
	 */
	public void operationChanged(IUndoableOperation operation) {
		if (fUndo.contains(operation) || fRedo.contains(operation)) {
			notifyChanged(operation);
		}
	}

}
