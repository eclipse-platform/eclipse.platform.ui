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
package org.eclipse.core.internal.commands.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * A batching operation contains a primary operation and considers any other
 * children as secondary operations that are implied by the primary operation.
 * The label for the operation is assumed to be originating operation's label.
 * </p>
 * 
 * <p>
 * Note:  This class is not intended to be used by clients.  It is part of the implementation
 * of {@link IOperationHistory#openOperation}.
 * 
 * @since 3.1
 * @experimental
 */
public class BatchingOperation extends AbstractOperation {

	private List fChildren = new ArrayList();

	/**
	 * Construct a batching operation using the specified operation as the
	 * primary operation. Use the label of this child as the label of the operation.
	 * 
	 * @param operation -
	 *            the primary operation of the batched operation.
	 */
	public BatchingOperation(IUndoableOperation operation) {
		super(operation.getLabel());
		add(operation);
	}

	/**
	 * Add the specified operation as a child of this composite operation.
	 * 
	 * @param operation -
	 *            the child to be added
	 */
	public void add(IUndoableOperation operation) {
		fChildren.add(operation);
		UndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			addContext(contexts[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus execute(IProgressMonitor monitor) {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) {
			return DefaultOperationHistory.OPERATION_INVALID_STATUS;
		}
		return op.execute(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#redo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus redo(IProgressMonitor monitor) {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) {
			return DefaultOperationHistory.OPERATION_INVALID_STATUS;
		}
		return op.redo(monitor);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#undo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus undo(IProgressMonitor monitor) {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) {
			return DefaultOperationHistory.OPERATION_INVALID_STATUS;
		}
		return op.undo(monitor);

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
	 */
	public boolean canUndo() {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) return false;
		return op.canUndo();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 */
	public boolean canExecute() {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) return false;
		return op.canExecute();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 */
	public boolean canRedo() {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) return false;
		return op.canRedo();
	}
	
	public void dispose() {
		for (int i=0; i<fChildren.size(); i++) {
			((IUndoableOperation)(fChildren.get(i))).dispose();
		}
	}
	
	public String getDescription() {
		IUndoableOperation op = getPrimaryOperation();
		if (op == null) return super.getDescription();
		return op.getDescription();
	}
	
	/**
	 * <p>
	 * Return the primary operation for this batch.
	 * 
	 * <p>
	 * Note:  This method is provided for the operation history.
	 * 
	 * @return the operation that triggered this batch of related operations.
	 */
	public IUndoableOperation getPrimaryOperation() {
		// return the primary operation of this batch
		if (fChildren.size() == 0) return null;
		return (IUndoableOperation)fChildren.get(0);
		
	}
}
