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
package org.eclipse.core.commands.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * <p>
 * A composite operation performs the execution, undo, or redo of multiple
 * operations as a single operation. The label for the operation is explicitly
 * set. It can be set using a constructor that assigns the label, or by
 * constructing the composite with the operation whose label is used as the
 * composite label.
 * </p>
 * 
 * <p>
 * Composites can be configured to indicate whether any operations that occur
 * during the execution, undo, or redo of the composite should be merged into
 * the composite.
 * </p>
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */
public class CompositeOperation extends AbstractOperation {

	private List fChildren = new ArrayList();

	/**
	 * Construct a composite operation with the specified label
	 * 
	 * @param label -
	 *            the label to be used to identify the operation.
	 */
	public CompositeOperation(String label) {
		super(label);
	}

	/**
	 * Construct a composite operation using the specified operation as the
	 * first child. Use the label of this child as the label of the operation.
	 * 
	 * @param operation -
	 *            the first child of the composite.
	 */
	public CompositeOperation(IUndoableOperation operation) {
		super(operation.getLabel());
		add(operation);
	}

	/**
	 * Returns whether the operation is a composite operation.
	 * 
	 * @return <code>true</code>
	 */

	public boolean isComposite() {
		return true;
	}
	
	/**
	 * Returns whether the operation is empty.
	 * 
	 * @return <code>true</code> if it is empty, <code>false</code> if it is not.
	 */

	public boolean isEmpty() {
		return fChildren.size() == 0;
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
	
	/**
	 * Return the children of this operation as an array of 
	 * IUndoableOperation.
	 * 
	 * @return returns the children as an array of IUndoableOperation.
	 */
	public IUndoableOperation [] getChildren() {
		return (IUndoableOperation [])fChildren.toArray(new IUndoableOperation[fChildren.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus execute(IProgressMonitor monitor) {
		MultiStatus multiStatus = new MultiStatus(OperationStatus.PLUGIN_ID, 0,
				"", null); //$NON-NLS-1$
		for (int i = 0; i < fChildren.size(); i++) {
			IUndoableOperation child = (IUndoableOperation)fChildren.get(i);
			IStatus status;
			if (child.canExecute()) {
				status = child.execute(monitor);
			} else {
				status = DefaultOperationHistory.OPERATION_INVALID_STATUS;
			}
			multiStatus.merge(status);
			if (status.getSeverity() == IStatus.ERROR) {
				// roll back the ones that were already done
				for (int j = 0; j < i; j++) {
					((IUndoableOperation) fChildren.get(j)).undo(monitor);
				}
				return multiStatus;
			}
		}
		return multiStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#redo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus redo(IProgressMonitor monitor) {
		MultiStatus multiStatus = new MultiStatus(OperationStatus.PLUGIN_ID, 0,
				"", null); //$NON-NLS-1$
		for (int i = 0; i < fChildren.size(); i++) {
			IUndoableOperation child = (IUndoableOperation)fChildren.get(i);
			IStatus status;
			if (child.canRedo()) {
				status = child.redo(monitor);
			} else {
				status = DefaultOperationHistory.OPERATION_INVALID_STATUS;
			}
			multiStatus.merge(status);
			if (status.getSeverity() == IStatus.ERROR) {
				// roll back the ones that were already done
				for (int j = 0; j < i; j++) {
					((IUndoableOperation) fChildren.get(j)).undo(monitor);
				}
				return multiStatus;
			}
		}
		return multiStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#undo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus undo(IProgressMonitor monitor) {
		MultiStatus multiStatus = new MultiStatus(OperationStatus.PLUGIN_ID, 0,
				"", null); //$NON-NLS-1$
		for (int i = fChildren.size() - 1; i >= 0; i--) {
			IUndoableOperation child = (IUndoableOperation)fChildren.get(i);
			IStatus status;
			if (child.canUndo()) {
				status = child.undo(monitor);
			} else {
				status = DefaultOperationHistory.OPERATION_INVALID_STATUS;
			}
			multiStatus.merge(status);
			if (status.getSeverity() == IStatus.ERROR) {
				// roll back the ones that were already done
				for (int j = fChildren.size() - 1; j > i; j--) {
					((IUndoableOperation) fChildren.get(j)).redo(monitor);
				}
				return multiStatus;
			}
		}
		return multiStatus;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canUndo()
	 */
	public boolean canUndo() {
		int size = fChildren.size();
		if (size == 0) return false;
		// we only check the last child in case the others are dependent on the 
		// last one getting undone first.
		return ((IUndoableOperation)(fChildren.get(size-1))).canUndo();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 */
	public boolean canExecute() {
		if (fChildren.size() == 0) return false;
		// we only check the first child in case the others depend on
		// on the first one getting executed first.
		return ((IUndoableOperation)(fChildren.get(0))).canExecute();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IUndoableOperation#canExecute()
	 */
	public boolean canRedo() {
		if (fChildren.size() == 0) return false;
		// we only check the first child in case the others depend on
		// on the first one getting redone first.
		return ((IUndoableOperation)(fChildren.get(0))).canRedo();
	}
	
	public void dispose() {
		for (int i=0; i<fChildren.size(); i++) {
			((IUndoableOperation)(fChildren.get(i))).dispose();
		}
	}
}
