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
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Creates a new operation to perform a refactoring.
 * 
 * The undo change isn't initialized.
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
	 * 
	 * @param refactoring
	 * @param style either ACTIVATION, INPUT or PRECONDITIONS.
	 */
	public PerformRefactoringOperation(Refactoring refactoring, int style) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
		fStyle= style;
	}
	
	public RefactoringStatus getConditionStatus() {
		return fPreconditionStatus;
	}
	
	public RefactoringStatus getValidationStatus() {
		return fValidationStatus;
	}
	
	public Change getUndoChange() {
		return fUndo;
	}

	public void run(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("", 10); //$NON-NLS-1$
		CreateChangeOperation create= new CreateChangeOperation(
			new CheckConditionsOperation(fRefactoring, fStyle),
			RefactoringStatus.ERROR);
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
