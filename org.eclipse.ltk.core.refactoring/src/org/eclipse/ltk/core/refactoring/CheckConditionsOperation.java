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

import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.ltk.internal.core.refactoring.Assert;

/**
 * Operation that, when run, check preconditions of the {@link Refactoring}
 * passed on creation.
 * 
 * <p> 
 * Note: this class is not intented to be subclassed by clients.
 * </p>
 * 
 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkInitialConditions(IProgressMonitor)
 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkFinalConditions(IProgressMonitor)
 * @see org.eclipse.ltk.core.refactoring.Refactoring#checkAllConditions(IProgressMonitor)
 * 
 * @since 3.0
 */
public class CheckConditionsOperation implements IWorkspaceRunnable {
	
	private Refactoring fRefactoring;
	private int fStyle;
	private RefactoringStatus fStatus;
	
	/** Flag indicating that no conditions will be checked */
	public final static int NONE=				0;
	/** Flag indicating that only initial conditions will be checked*/
	public final static int INITIAL_CONDITONS=	1 << 1;
	/** Flag indicating that ony final conditiions will be checked */
	public final static int FINAL_CONDITIONS=	1 << 2;
	/** Flag indicating that all conditions will be checked */
	public final static int ALL_CONDITIONS=		INITIAL_CONDITONS | FINAL_CONDITIONS;
	
	private final static int LAST=          	1 << 3;
	
	/**
	 * Creates a new <code>CheckConditionsOperation</code>.
	 * 
	 * @param refactoring the refactoring for which the preconditions are to
	 *  be checked.
	 * @param style style to define which conditions to check. Must be one of
	 *  <code>INITIAL_CONDITONS</code>, <code>FINAL_CONDITIONS</code> or 
	 *  <code>ALL_CONDITIONS</code>
	 */
	public CheckConditionsOperation(Refactoring refactoring, int style) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
		fStyle= style;
		Assert.isTrue(checkStyle(fStyle));
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor pm) throws CoreException {
		try {
			fStatus= null;
			if ((fStyle & ALL_CONDITIONS) == ALL_CONDITIONS)
				fStatus= fRefactoring.checkAllConditions(pm);
			else if ((fStyle & INITIAL_CONDITONS) == INITIAL_CONDITONS)
				fStatus= fRefactoring.checkInitialConditions(pm);
			else if ((fStyle & FINAL_CONDITIONS) == FINAL_CONDITIONS)
				fStatus= fRefactoring.checkFinalConditions(pm);
		} finally {
			pm.done();
		}
	}

	/**
	 * Returns the outcome of the operation or <code>null</code> if an exception 
	 * has occurred while performing the operation.
	 * 
	 * @return the {@link RefactoringStatus} of the condition checking
	 */
	public RefactoringStatus getStatus() {
		return fStatus;
	}
	
	/**
	 * Returns the operation's refactoring
	 * 
	 * @return the operation's refactoring
	 */
	public Refactoring getRefactoring() {
		return fRefactoring;
	}
	
	/**
	 * Returns the condition checking style.
	 * 
	 * @return the condition checking style
	 */
	public int getStyle() {
		return fStyle;
	}
	
	private boolean checkStyle(int style) {
		return style > NONE && style < LAST;
	}
}
