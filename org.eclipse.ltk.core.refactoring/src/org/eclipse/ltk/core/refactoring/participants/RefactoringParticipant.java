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
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.Assert;


public abstract class RefactoringParticipant {
	
	private RefactoringProcessor fProcessor;
	
	/**
	 * Sets the copy participant with the given processor. The processor
	 * can only be set ones. If this method is called a second time an assertion
	 * failure will be generated.
	 * 
	 * @param processor the processor that is associated with this
	 *  participant
	 */
	protected void setProcessor(RefactoringProcessor processor) {
		Assert.isNotNull(processor);
		Assert.isTrue(fProcessor == null);
		fProcessor= processor;
	}
	
	/**
	 * Returns the processor that is associated with this participant. 
	 * 
	 * @return the processor that is associated with this participant
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}
	
	public abstract void initialize(RefactoringProcessor processor, Object element) throws CoreException;
	
	public abstract boolean isApplicable() throws CoreException;
	
	public abstract RefactoringStatus checkInitialConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException;
	
	public abstract RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException;
	
	public abstract Change createChange(IProgressMonitor pm) throws CoreException;
}
