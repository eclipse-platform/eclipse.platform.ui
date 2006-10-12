/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 * IAdvancedUndoableOperation2 defines a method for computing the validity of
 * executing an operation before attempting to execute it. This interface is
 * useful when implementing {@link IOperationApprover2}.
 * </p>
 * 
 * @since 3.3
 * 
 */
public interface IAdvancedUndoableOperation2 {
	/**
	 * Return a status indicating the projected outcome of executing the
	 * receiver.
	 * 
	 * This method should be used to report the possible outcome of executing an
	 * operation when computing the validity of an execute is too expensive to
	 * perform in {@link IUndoableOperation#canExecute()}. It is not called by
	 * the operation history, but instead is used by clients (such as
	 * implementers of {@link IOperationApprover2}) who wish to perform
	 * advanced validation of an operation before attempting to execute it.
	 * 
	 * If the result of this method is the discovery that an operation can in
	 * fact not be executed, then the operation is expected to correctly answer
	 * <code>false</code> on subsequent calls to
	 * {@link IUndoableOperation#canExecute()}.
	 * 
	 * @param monitor
	 *            the progress monitor (or <code>null</code>) to use for
	 *            reporting progress to the user while computing the validity.
	 * 
	 * @return the IStatus indicating the validity of the execute. The status
	 *         severity should be set to <code>OK</code> if the execute can
	 *         successfully be performed, and <code>ERROR</code> if it
	 *         cannot. Any other severity is assumed to represent an ambiguous
	 *         state.
	 * @throws ExecutionException
	 *             if an exception occurs while computing the validity.
	 */
	IStatus computeExecutionStatus(IProgressMonitor monitor)
			throws ExecutionException;
}
