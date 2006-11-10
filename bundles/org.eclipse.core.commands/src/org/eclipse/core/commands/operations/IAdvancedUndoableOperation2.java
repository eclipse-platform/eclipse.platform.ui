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
 * executing an operation before attempting to execute it. It also defines a way
 * for clients to specify that computing status should be done quietly, without
 * consulting the user. This interface is useful when implementing
 * {@link IOperationApprover2}, or any other object that performs validation of
 * the undo history. It also allows operations to specify whether they should be
 * run in the UI thread.
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
	 *         successfully be performed, and <code>ERROR</code> if it cannot.
	 *         Any other severity is assumed to represent an ambiguous state.
	 * @throws ExecutionException
	 *             if an exception occurs while computing the validity.
	 */
	IStatus computeExecutionStatus(IProgressMonitor monitor)
			throws ExecutionException;

	/**
	 * Set a boolean that instructs whether the computation of the receiver's
	 * execution, undo, or redo status should quietly compute status without
	 * consulting or prompting the user. The default value is <code>false</code>.
	 * This flag should only be set to <code>true</code> while the execution,
	 * undo, or redo status computations are being performed in the background,
	 * and should be restored to <code>false</code> when complete.
	 * <p>
	 * If the status computation methods typically need to consult the user in
	 * order to determine the severity of a particular situation, the least
	 * severe status that could be chosen by the user should be returned when
	 * this flag is <code>true</code>. This can help to prevent overzealous
	 * disposal of the operation history when an operation is in an ambiguous
	 * state. Typically, the status computation methods are invoked with this
	 * flag set to <code>false</code> just before the actual execution, undo,
	 * or redo occurs, so the user can be consulted for the final outcome.
	 * 
	 * @param quiet
	 *            <code>true</code> if it is inappropriate to consult or
	 *            otherwise prompt the user while computing status, and
	 *            <code>false</code> if the user may be prompted.
	 * 
	 * @see #computeExecutionStatus(IProgressMonitor)
	 * @see IAdvancedUndoableOperation#computeUndoableStatus(IProgressMonitor)
	 * @see IAdvancedUndoableOperation#computeRedoableStatus(IProgressMonitor)
	 */
	public void setQuietCompute(boolean quiet);

	/**
	 * Return a boolean that instructs whether the operation should be executed,
	 * undone, or redone in a background thread.
	 * 
	 * @return <code>true</code> if the operation should be run in the
	 *         background, <code>false</code> if it should not.
	 */
	public boolean runInBackground();
}
