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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IRefactoringCoreStatusCodes;

public class FailingParticipantTests extends TestCase {

	private ElementRenameRefactoring fRefactoring;

	private List fLogEntries;

	public FailingParticipantTests() {
		super("Failing Participants Tests");
	}

	public void setUp() {
		Platform.addLogListener(new ILogListener() {
			public void logging(IStatus status, String plugin) {
				fLogEntries.add(status);
			}
		});
		resetLog();
	}

	private void resetLog() {
		fLogEntries= new ArrayList();
	}

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

		Assert.assertEquals(1, fLogEntries.size());
		IStatus status= (IStatus) fLogEntries.get(0);
		Assert.assertTrue("Exception wrong", status.getException().getClass().equals(FailingParticipant.Exception.class));
		Assert.assertTrue("No exception generated", exception);

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

		Assert.assertEquals(1, fLogEntries.size());
		status= (IStatus) fLogEntries.get(0);
		Assert.assertTrue("Exception wrong", status.getException().getClass().equals(FailingParticipant2.Exception.class));
		Assert.assertTrue("No exception generated", exception);

		resetLog();

		// FailingParticipant2 now disabled

		// this time everything must pass - only working participant
		fRefactoring= new ElementRenameRefactoring(0);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());
		change= fRefactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());

		Assert.assertEquals(0, fLogEntries.size());
		Assert.assertTrue("Working participant not executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_EXEC));
	}

	// If the main refactoring fails to execute, disable any participants contributing preChanges
	public void testFailingRefactorWithPreParticipants() throws Exception {
		fRefactoring= new ElementRenameRefactoring(ElementRenameRefactoring.WORKING | ElementRenameRefactoring.FAIL_TO_EXECUTE | ElementRenameRefactoring.PRE_CHANGE);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());

		resetLog();
		boolean exception= false;
		Change change= fRefactoring.createChange(new NullProgressMonitor());
		exception= false;
		try {
			// blows up because main refactoring fails to execute
			change.perform(new NullProgressMonitor());
		} catch (RuntimeException e) {
			exception= true;
		}

		//System.out.println(fLogEntries);
		Assert.assertEquals(2, fLogEntries.size());
		IStatus status= (IStatus) fLogEntries.get(0);
		Assert.assertTrue("Exception wrong", status.getException().getClass().equals(RuntimeException.class));
		Assert.assertEquals("Status code wrong", IRefactoringCoreStatusCodes.REFACTORING_EXCEPTION_DISABLED_PARTICIPANTS, status.getCode());
		status= (IStatus) fLogEntries.get(1);
		Assert.assertEquals("Exception wrong", null, status.getException());
		Assert.assertEquals("Status code wrong", IRefactoringCoreStatusCodes.PARTICIPANT_DISABLED, status.getCode());
		Assert.assertTrue("No exception generated", exception);

		//System.out.println(ElementRenameProcessor.fHistory);
		
		Assert.assertTrue("Working participant not created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_CREATE));
		Assert.assertFalse("Working participant executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_EXEC));
		Assert.assertTrue("Working participant pre not created pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATEPRE));
		Assert.assertTrue("Working participant pre not created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATE));
		Assert.assertTrue("Working participant pre not executed pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXECPRE));
		Assert.assertFalse("Working participant pre executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXEC));


		// Now try it again and the working participant should not be called at all,
		// since it was disabled by the failure in the main refactoring
		fRefactoring= new ElementRenameRefactoring(ElementRenameRefactoring.WORKING | ElementRenameRefactoring.PRE_CHANGE);
		fRefactoring.checkInitialConditions(new NullProgressMonitor());
		fRefactoring.checkFinalConditions(new NullProgressMonitor());

		resetLog();

		change= fRefactoring.createChange(new NullProgressMonitor());
		change.perform(new NullProgressMonitor());

		Assert.assertEquals(0, fLogEntries.size());

		Assert.assertTrue("Working participant not created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_CREATE));
		Assert.assertTrue("Working participant not executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKING_EXEC));
		Assert.assertFalse("Working participant pre created pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATEPRE));
		Assert.assertFalse("Working participant pre created", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_CREATE));
		Assert.assertFalse("Working participant pre executed", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXEC));
		Assert.assertFalse("Working participant pre executed pre", ElementRenameProcessor.fHistory.contains(ElementRenameProcessor.WORKINGPRE_EXECPRE));

	}
}
