/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - 
 *          Fix for Bug 63149 [ltk] allow changes to be executed after the 'main' change during an undo [refactoring] 
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import java.util.ArrayList;
import java.util.List;

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

	public static List fHistory;

	public static final String WORKING_CREATE= "workingCreate";
	public static final String WORKING_EXEC= "workingExec";
	public static final String WORKING_EXEC_UNDO= "workingExecUndo";
	public static final String WORKINGPRE_CREATE= "workingPreCreate";
	public static final String WORKINGPRE_CREATEPRE= "workingPreCreatePre";
	public static final String WORKINGPRE_EXEC= "workingPreExec";
	public static final String WORKINGPRE_EXECPRE= "workingPreExecPre";
	public static final String WORKINGPRE_EXEC_UNDO= "workingPreExecUndo";
	public static final String WORKINGPRE_EXECPRE_UNDO= "workingPreExecPreUndo";
	public static final String MAIN_CREATE= "mainCreate";
	public static final String MAIN_EXEC= "mainExec";
	public static final String MAIN_EXEC_UNDO= "mainExecUndo";

	Object[] fElements;

	int fOptions;

	public static void resetHistory() {
		fHistory= new ArrayList();
	}

	public ElementRenameProcessor(int options) {
		resetHistory();
		fOptions= options;
		if ((options & ElementRenameRefactoring.WORKING) != 0) {
			if ((options & ElementRenameRefactoring.PRE_CHANGE) != 0) {
				if ((options & ElementRenameRefactoring.ALWAYS_ENABLED) != 0)
					fElements= new Object[] { new ElementWorkingPreAlways() };
				else
					fElements= new Object[] { new ElementWorkingPre() };
			} else
				fElements= new Object[] { new ElementWorking() };
		} else
			fElements= new Object[] { new Element() };
	}

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
		fHistory.add(MAIN_CREATE);
		return new NullChange() {
			public Change perform(IProgressMonitor monitor) throws CoreException {
				if ((fOptions & ElementRenameRefactoring.FAIL_TO_EXECUTE) != 0)
					throw new RuntimeException();
				fHistory.add(MAIN_EXEC);
				// Undo change
				return new NullChange() {
					public Change perform(IProgressMonitor m2) throws CoreException {
						fHistory.add(MAIN_EXEC_UNDO);
						// Redo change
						return new NullChange() {
							public Change perform(IProgressMonitor m3) throws CoreException {
								fHistory.add(MAIN_EXEC);
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
	public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
		return ParticipantManager.loadRenameParticipants(new RefactoringStatus(), this, fElements[0], new RenameArguments("test", true), new String[0], new SharableParticipants());
	}
}
