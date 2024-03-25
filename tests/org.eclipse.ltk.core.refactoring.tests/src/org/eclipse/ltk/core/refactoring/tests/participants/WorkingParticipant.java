/*******************************************************************************
 * Copyright (c) 2008 Oakland Software Incorporated, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

public class WorkingParticipant extends RenameParticipant {

	@Override
	protected boolean initialize(Object element) {
		return true;
	}

	@Override
	public String getName() {
		return WorkingParticipant.class.getName();
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKING_CREATE);

		return  new NullChange() {
			@Override
			public Change perform(IProgressMonitor m1) throws CoreException {
				ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKING_EXEC);
				return new NullChange() {
					@Override
					public Change perform(IProgressMonitor m2) throws CoreException {
						ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKING_EXEC_UNDO);
						return new NullChange() {
							@Override
							public Change perform(IProgressMonitor m3) throws CoreException {
								ElementRenameProcessor.fHistory.add(ElementRenameProcessor.WORKING_EXEC);
								return null;
							}
						};
					}
				};
			}
		};
	}
}
