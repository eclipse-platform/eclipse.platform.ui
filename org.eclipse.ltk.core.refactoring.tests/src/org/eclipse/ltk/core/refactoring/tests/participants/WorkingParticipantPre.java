/*******************************************************************************
 * Copyright (c) 2008 Oakland Software Incorporated, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software (francisu@ieee.org) - initial contribution - bug 63149
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class WorkingParticipantPre extends RenameParticipant {

	/**
	 * {@inheritDoc}
	 */
	protected boolean initialize(Object element) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return WorkingParticipant.class.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_CREATE);
		return new NullChange() {
			public Change perform(IProgressMonitor m1) throws CoreException {
				ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_EXEC);
				return new NullChange() {
					public Change perform(IProgressMonitor m2) throws CoreException {
						ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_EXEC_UNDO);
						return new NullChange() {
							public Change perform(IProgressMonitor m3) throws CoreException {
								ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_EXEC);
								return null;
							}
						};
					}
				};
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_CREATEPRE);
		return  new NullChange() {
			public Change perform(IProgressMonitor m1) throws CoreException {
				ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_EXECPRE);
				return new NullChange() {
					public Change perform(IProgressMonitor m2) throws CoreException {
						ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_EXECPRE_UNDO);
						return new NullChange() {
							public Change perform(IProgressMonitor m3) throws CoreException {
								ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKINGPRE_EXECPRE);
								return null;
							}
						};
					}
				};
			}
		};
	}
}
