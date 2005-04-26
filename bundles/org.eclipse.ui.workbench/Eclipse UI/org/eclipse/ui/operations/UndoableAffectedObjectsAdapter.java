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
package org.eclipse.ui.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IAdvancedUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * UndoableAffectedObjectsAdapter defines an implementation for
 * IAdvancedUndoableOperation that is used only for returning the objects
 * affected by an operation.
 * 
 * Most implementers of IAdvancedUndoableOperation mix the interface in with
 * their implementation of IUndoableOperation. However, some clients provide an
 * adapter for IAdvancedUndoableOperation for the sole purpose of describing
 * their affected objects, and in this case having a default implementation is
 * useful.
 * </p>
 * 
 * @since 3.1
 * 
 */

public class UndoableAffectedObjectsAdapter implements
		IAdvancedUndoableOperation {
	private Object[] affectedObjects;

	/**
	 * Create an instance of UndoableAffectedObjectsAdapter that has the
	 * specified affected objects.
	 * 
	 * @param affectedObjects -
	 *            the array of Objects to be returned by the receiver whenever
	 *            its affected objects are requested.
	 */
	public UndoableAffectedObjectsAdapter(Object[] affectedObjects) {
		this.affectedObjects = affectedObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedModelOperation#aboutToNotify(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void aboutToNotify(OperationHistoryEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedModelOperation#getAffectedObjects()
	 */
	public Object[] getAffectedObjects() {
		return affectedObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor)
			throws ExecutionException {
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IAdvancedUndoableOperation#computeRedoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor)
			throws ExecutionException {
		return Status.OK_STATUS;
	}

}
