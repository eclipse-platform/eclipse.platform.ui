/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Operation that, when performed, creates a {@link Change} object for a given
 * refactoring. If created with a refactoring object directly, no precondition
 * checking is performed. If created with a {@link CheckConditionsOperation} the
 * requested precondition checking is performed before creating the change.
 * <p>
 * If the precondition checking returns a fatal error or the status's severity
 * exceeds a certain threshold then no change will be created.
 * </p>
 * <p>
 * If a change has been created the operation calls {@link Change#initializeValidationData(IProgressMonitor)}
 * to initialize the change's validation data.
 * </p>
 * <p>
 * The operation should be executed via the run method offered by
 * <code>IWorkspace</code> to achieve proper delta batching.
 * </p>
 * 
 * <p> 
 * Note: this class is not intented to be subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public class CreateChangeOperation implements IWorkspaceRunnable {

	private Refactoring fRefactoring;
	
	private CheckConditionsOperation fCheckConditionOperation;
	private int fCheckPassedSeverity;
	
	private Change fChange;
	
	/**
	 * Creates a new operation with the given refactoring. No condition checking
	 * is performed before creating the change object. It is assumed that the
	 * condition checking has already been performed outside of this operation.
	 * The operation might fail if the precondition checking has not been performed
	 * yet.
	 *
	 * @param refactoring the refactoring for which the change is to be created
	 */
	public CreateChangeOperation(Refactoring refactoring) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
	}
	
	/**
	 * Creates a new operation with the given {@link CheckConditionsOperation}. When
	 * performed the operation first checks the conditions as specified by the <code>
	 * CheckConditionsOperation</code>. Depending on the result of the condition 
	 * checking a change object is created or not.
	 * 
	 * @param operation the condition checking operation
	 * @param checkPassedSeverity the check passed severity value. This value is used to 
	 *  decide whether the condition check is interpreted as passed or not. The condition 
	 *  check is considered to be passed if the refactoring status's severity is less or 
	 *  equal than the given severity value. The given value must be smaller than {@link 
	 *  RefactoringStatus.FATAL}.
	 * 
	 * @see #setCheckPassedSeverity(int)
	 */
	public CreateChangeOperation(CheckConditionsOperation operation, int checkPassedSeverity) {
		Assert.isNotNull(operation);
		fCheckConditionOperation= operation;
		fRefactoring= operation.getRefactoring();
		Assert.isTrue (checkPassedSeverity < RefactoringStatus.FATAL);
		fCheckPassedSeverity= checkPassedSeverity;
	}
	
	/**
	 * Returns the check passed severity.
	 * 
	 * @return the check passed severity
	 * 
	 * @see RefactoringStatus
	 */
	public int getCheckPassedSeverity() {
		return fCheckPassedSeverity;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor pm) throws CoreException {
		fChange= null;
		try {
			fChange= null;
			if (fCheckConditionOperation != null) {
				pm.beginTask("", 7); //$NON-NLS-1$
				pm.subTask(""); //$NON-NLS-1$
				fCheckConditionOperation.run(new SubProgressMonitor(pm, 4));
				RefactoringStatus status= fCheckConditionOperation.getStatus();
				if (status != null && status.getSeverity() <= fCheckPassedSeverity) {
					fChange= fRefactoring.createChange(new SubProgressMonitor(pm, 2));
					fChange.initializeValidationData(new SubProgressMonitor(pm, 1));
				} else {
					pm.worked(3);
				}
			} else {
				pm.beginTask("", 3); //$NON-NLS-1$
				fChange= fRefactoring.createChange(new SubProgressMonitor(pm, 2));
				fChange.initializeValidationData(new SubProgressMonitor(pm, 1));
			}
		} finally {
			pm.done();
		}
	}

	/**
	 * Returns the outcome of the operation or <code>null</code> if an exception 
	 * occurred when performing the operation.
	 * 
	 * @return the created change or <code>null</code>
	 */
	public Change getChange() {
		return fChange;
	}
	
	/**
	 * Returns the status of the condition checking. Returns <code>null</code> if
	 * no condition checking has been requested.
	 * 
	 * @return the status of the condition checking
	 */
	public RefactoringStatus getConditionCheckingStatus() {
		if (fCheckConditionOperation != null)
			return fCheckConditionOperation.getStatus();
		return null;
	}
	
	/**
	 * Returns the condition checking style as set to the {@link CheckConditionsOperation}.
	 * If no condition checking operation is provided (e.g. the change is created directly
	 * by calling {@link Refactoring#createChange(IProgressMonitor)} then {@link 
	 * CheckConditionsOperation.NONE} is returned.
	 * 
	 * @return the condition checking style
	 */
	public int getConditionCheckingStyle() {
		if (fCheckConditionOperation != null)
			return fCheckConditionOperation.getStyle(); 
		return CheckConditionsOperation.NONE;
	}
}
