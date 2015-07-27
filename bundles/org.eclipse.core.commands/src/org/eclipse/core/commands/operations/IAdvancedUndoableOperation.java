/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * IAdvancedUndoableOperation defines an interface for undoable operations that
 * modify one or more elements in a model and attempt to keep model listeners up
 * to date with changes that occur in the undo and redo history involving particular
 * model elements.  It also defines methods for computing the validity of an operation
 * for undo or redo before attempting to perform the undo or redo.
 * </p>
 * <p>
 * This interface is intended to be used by legacy frameworks that are adapting
 * their original undo and redo support to this framework. The methods in this
 * interface allow legacy clients to maintain features not supported in the
 * basic operations framework.
 * </p>
 *
 * @since 3.1
 *
 */
public interface IAdvancedUndoableOperation {

	/**
	 * <p>
	 * An operation history notification about this operation is about to be
	 * sent to operation history listeners. Any preparation needed before
	 * listeners are notified about this operation should be performed here.
	 *
	 * <p>
	 * This method has been added to support legacy undo frameworks that are
	 * adapting to IUndoableOperation. Operations that previously relied on
	 * notification from their containing history or stack before any listeners
	 * are notified about changes to the operation should implement this
	 * interface.
	 *
	 * @param event
	 *            the event that is about to be sent with the pending
	 *            notification
	 *
	 */
	void aboutToNotify(OperationHistoryEvent event);

	/**
	 * <p>
	 * Return an array of objects that are affected by executing, undoing, or
	 * redoing this operation. If it cannot be determined which objects are
	 * affected, return null.
	 * </p>
	 *
	 * @return the array of Objects modified by this operation, or
	 *         <code>null</code> if the affected objects cannot be determined.
	 */
	Object[] getAffectedObjects();

	/**
	 * Return a status indicating the projected outcome of undoing the receiver.
	 *
	 * This method should be used to report the possible outcome of an undo and
	 * is used when computing the validity of an undo is too expensive to
	 * perform in {@link IUndoableOperation#canUndo()}. It is not called by the
	 * operation history, but instead is used by clients (such as implementers
	 * of {@link IOperationApprover}) who wish to perform advanced validation of
	 * an operation before attempting to undo it.
	 *
	 * If the result of this method is the discovery that an operation can in
	 * fact not be undone, then the operation is expected to correctly answer
	 * <code>false</code> on subsequent calls to
	 * {@link IUndoableOperation#canUndo()}.
	 *
	 * @param monitor
	 *            the progress monitor (or <code>null</code>) to use for
	 *            reporting progress to the user while computing the validity.
	 *
	 * @return the IStatus indicating the validity of the undo. The status
	 *         severity should be set to <code>OK</code> if the undo can
	 *         successfully be performed, and <code>ERROR</code> if it
	 *         cannnot. Any other status is assumed to represent an ambiguous
	 *         state.
	 * @throws ExecutionException
	 *             if an exception occurs while computing the validity.
	 */
	IStatus computeUndoableStatus(IProgressMonitor monitor)
			throws ExecutionException;

	/**
	 * Return a status indicating the projected outcome of redoing the receiver.
	 *
	 * This method should be used to report the possible outcome of a redo and
	 * is used when computing the validity of a redo is too expensive to perform
	 * in {@link IUndoableOperation#canRedo()}. It is not called by the
	 * operation history, but instead is used by clients (such as implementers
	 * of {@link IOperationApprover}) who wish to perform advanced validation of
	 * an operation before attempting to redo it.
	 *
	 * If the result of this method is the discovery that an operation can in
	 * fact not be redone, then the operation is expected to correctly answer
	 * <code>false</code> on subsequent calls to
	 * {@link IUndoableOperation#canRedo()}.
	 *
	 * @param monitor
	 *            the progress monitor (or <code>null</code>) to use for
	 *            reporting progress to the user while computing the validity.
	 *
	 * @return the IStatus indicating the validity of the redo. The status
	 *         severity should be set to <code>OK</code> if the redo can
	 *         successfully be performed, and <code>ERROR</code> if it
	 *         cannnot. Any other status is assumed to represent an ambiguous
	 *         state.
	 * @throws ExecutionException
	 *             if an exception occurs while computing the validity.
	 */
	IStatus computeRedoableStatus(IProgressMonitor monitor)
			throws ExecutionException;

}
