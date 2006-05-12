/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ParticipantManager;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

public class ElementRenameProcessor extends RenameProcessor {
	
	Object[] fElements= new Object[] { new Element() };
	
	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements() {
		return fElements;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIdentifier() {
		return ElementRenameProcessor.class.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProcessorName() {
		return ElementRenameProcessor.class.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isApplicable() throws CoreException {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new NullChange();
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
		return ParticipantManager.loadRenameParticipants(new RefactoringStatus(), this, fElements[0], 
			new RenameArguments("test", true), new String[0], new SharableParticipants());
	}
}
