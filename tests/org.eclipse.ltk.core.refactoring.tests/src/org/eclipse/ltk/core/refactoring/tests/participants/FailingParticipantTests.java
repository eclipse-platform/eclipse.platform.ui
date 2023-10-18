/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> -
 *          Fix for Bug 63149 [ltk] allow changes to be executed after the 'main' change during an undo [refactoring]
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;

public class FailingParticipantTests {

	private ElementRenameRefactoring fRefactoring;

	private ILogListener fLogListener;
	private List<IStatus> fLogEntries;

	@Before
	public void setUp() {
		fLogListener= new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				fLogEntries.add(status);
			}
		};
		Platform.addLogListener(fLogListener);
	}

	@After
	public void tearDown() throws Exception {
		Platform.removeLogListener(fLogListener);
	}

	private void resetLog() {
		fLogEntries= new ArrayList<>();
	}

	@Test
	public void testFailingParticipants() throws Exception {
		fRefactoring= new ElementRenameRefactoring(0);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());

		resetLog();

		boolean exception= false;
		try {
			// blows up because FailingParticipant throws in createChange
			fRefactoring.createChange(new NullProgressMonitor());
		} catch (FailingParticipant.Exception e) {
			exception= true;
		}

		assertEquals(1, fLogEntries.size());
		IStatus status= fLogEntries.get(0);
		assertEquals("Exception wrong", status.getException().getClass(), FailingParticipant.Exception.class);
		assertTrue("No exception generated", exception);

		resetLog();

		// FailingParticipant now disabled

		fRefactoring= new ElementRenameRefactoring(0);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());
		// FailingParticipant2 blows up when executing the change
		Change change= fRefactoring.createChange(new NullProgressMonitor());
		exception= false;
		try {
			change.perform(new NullProgressMonitor());
		} catch (FailingParticipant2.Exception e) {
			exception= true;
		}

		assertEquals(1, fLogEntries.size());
		status= fLogEntries.get(0);
		assertEquals("Exception wrong", status.getException().getClass(), FailingParticipant2.Exception.class);
		assertTrue("No exception generated", exception);

		resetLog();

		// FailingParticipant2 now disabled

		// this time everything must pass - only working participant
		fRefactoring= new ElementRenameRefactoring(0);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());
		change= fRefactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());

		assertEquals(0, fLogEntries.size());
		assertTrue("Working participant not executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_EXEC));
	}

	// If the main refactoring fails to execute, disable any participants contributing preChanges
	@Test
	public void testFailingRefactorWithPreParticipants() throws Exception {
		fRefactoring= new ElementRenameRefactoring(ElementRenameRefactoring.WORKING | ElementRenameRefactoring.FAIL_TO_EXECUTE | ElementRenameRefactoring.PRE_CHANGE);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());

		resetLog();
        boolean exception= false;
		Change change= fRefactoring.createChange(new NullProgressMonitor());
		try {
			// blows up because main refactoring fails to execute
			change.perform(new NullProgressMonitor());
		} catch (RuntimeException e) {
			exception= true;
		}

		//System.out.println(fLogEntries);
		assertEquals(2, fLogEntries.size());
		IStatus status= fLogEntries.get(0);
		assertEquals("Exception wrong", status.getException().getClass(), RuntimeException.class);
		assertEquals("Status code wrong", IRefactoringCoreStatusCodes.REFACTORING_EXCEPTION_DISABLED_PARTICIPANTS, status.getCode());
		status= fLogEntries.get(1);
		assertNull("Exception wrong", status.getException());
		assertEquals("Status code wrong", IRefactoringCoreStatusCodes.PARTICIPANT_DISABLED, status.getCode());
		assertTrue("No exception generated", exception);

		//System.out.println(ElementRenameProcessor.fHistory);

		assertTrue("Working participant not created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_CREATE));
		assertFalse("Working participant executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_EXEC));
		assertTrue("Working participant pre not created pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATEPRE));
		assertTrue("Working participant pre not created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATE));
		assertTrue("Working participant pre not executed pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXECPRE));
		assertFalse("Working participant pre executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXEC));


		// Now try it again and the working participant should not be called at all,
		// since it was disabled by the failure in the main refactoring
		fRefactoring= new ElementRenameRefactoring(ElementRenameRefactoring.WORKING | ElementRenameRefactoring.PRE_CHANGE);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());

		resetLog();

		change= fRefactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());

		assertEquals(0, fLogEntries.size());

		assertTrue("Working participant not created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_CREATE));
		assertTrue("Working participant not executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_EXEC));
		assertFalse("Working participant pre created pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATEPRE));
		assertFalse("Working participant pre created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATE));
		assertFalse("Working participant pre executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXEC));
		assertFalse("Working participant pre executed pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXECPRE));

	}
}
