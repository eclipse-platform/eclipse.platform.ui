/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Operation that, when run, executes a refactoring. This includes
 * condition checking, change creation, change execution and remembering
 * of the undo change on the refactoring's undo stack.
 * <p>
 * The operation should be executed via the run method offered by
 * <code>IWorkspace</code> to achieve proper delta batching.
 * </p>
 * <p> 
 * Note: this class is not intended to be extended by clients.
 * </p>
 * 
 * @see org.eclipse.core.resources.IWorkspace
 * 
 * @since 3.0 
 */
public class PerformRefactoringOperation implements IWorkspaceRunnable {
	
	private int fStyle;
	private Refactoring fRefactoring;
	
	private RefactoringStatus fPreconditionStatus;
	private RefactoringStatus fValidationStatus;
	private Change fUndo;
	
	/**
	 * Create a new perform refactoring operation. The operation will not
	 * perform the refactoring if the refactoring's condition checking returns 
	 * an error	of severity {@link RefactoringStatus#FATAL}. 
	 * 
	 * @param refactoring the refactoring to perform
	 * @param style the condition checking style as defined by 
	 *  {@link CheckConditionsOperation} 
	 */
	public PerformRefactoringOperation(Refactoring refactoring, int style) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
		fStyle= style;
	}
	
	/**
	 * Return the refactoring status of the condition checking.
	 * 
	 * @return the refactoring status of the condition checking or <code>null</code>
	 *  if the operation hasn't been performed yet
	 */
	public RefactoringStatus getConditionStatus() {
		return fPreconditionStatus;
	}
	
	/**
	 * Returns the refactoring status of the change's validation checking
	 * or <code>null</code> if a change couldn't be created or the operation
	 * hasn't been performed yet.  
	 *
	 * @return the refactoring status of the change's validation checking
	 */
	public RefactoringStatus getValidationStatus() {
		return fValidationStatus;
	}
	
	/**
	 * The undo object or <code>null</code> if no undo exists. The undo
	 * object is initialize via the call {@link Change#initializeValidationData(IProgressMonitor)}
	 * 
	 * @return the undo object or <code>null</code>
	 */
	public Change getUndoChange() {
		return fUndo;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
			monitor= new NullProgressMonitor();
		monitor.beginTask("", 10); //$NON-NLS-1$
		CreateChangeOperation create= new CreateChangeOperation(
			new CheckConditionsOperation(fRefactoring, fStyle),
			RefactoringStatus.FATAL);
		create.run(new SubProgressMonitor(monitor, 6));
		fPreconditionStatus= create.getConditionCheckingStatus();
		if (fPreconditionStatus.hasFatalError()) {
			monitor.done();
			return;
		}
		Change change= create.getChange();
		PerformChangeOperation perform= new PerformChangeOperation(change);
		perform.setUndoManager(RefactoringCore.getUndoManager(), fRefactoring.getName());
		perform.run(new SubProgressMonitor(monitor, 2));
		fValidationStatus= perform.getValidationStatus();
		fUndo= perform.getUndoChange();
	}
}
