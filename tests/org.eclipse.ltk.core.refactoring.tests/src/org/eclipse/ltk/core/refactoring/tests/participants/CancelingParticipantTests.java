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
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.participants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

public class CancelingParticipantTests {

	private class CancelingParticipant extends RenameParticipant {
		@Override
		protected boolean initialize(Object element) {
			return true;
		}
		@Override
		public String getName() {
			return "canceling participant";
		}

		@Override
		public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
			if (fCancelStep == 0) {
				pm.setCanceled(true);
				throw new OperationCanceledException();
			}
			return new RefactoringStatus();
		}

		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			if (fCancelStep == 1) {
				pm.setCanceled(true);
				throw new OperationCanceledException();
			}
			return new NullChange("1");
		}

		@Override
		public Change createPreChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			if (fCancelStep == 2) {
				pm.setCanceled(true);
				throw new OperationCanceledException();
			}
			return new NullChange("2");
		}
	}

	private class TestProcessor extends RenameProcessor {
		private Object fElement= Boolean.TRUE;

		@Override
		public Object[] getElements() {
			return new Object[] { fElement };
		}
		@Override
		public String getIdentifier() {
			return "org.eclipse.ltk.core.refactoring.tests.TestProcessor";
		}
		@Override
		public String getProcessorName() {
			return "processor";
		}
		@Override
		public boolean isApplicable() throws CoreException {
			return true;
		}
		@Override
		public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}
		@Override
		public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context) throws CoreException, OperationCanceledException {
			return new RefactoringStatus();
		}
		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new NullChange("test change");
		}
		@Override
		public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants) throws CoreException {
			CancelingParticipant participant= new CancelingParticipant();
			participant.initialize(this, fElement, new RenameArguments("", false));
			return new RefactoringParticipant[] { participant };
		}
	}

	private int fCancelStep;

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
		fLogEntries= new ArrayList<>();
	}

	@After
	public void tearDown() throws Exception {
		Platform.removeLogListener(fLogListener);
	}

	@Test
	public void testCheckConditions() throws Exception {
		RenameRefactoring refactoring= new RenameRefactoring(new TestProcessor());

		fCancelStep= 0;
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		NullProgressMonitor pm= new NullProgressMonitor();

		boolean exception= false;
		try {
			ResourcesPlugin.getWorkspace().run(op, pm);
		} catch (OperationCanceledException e) {
			exception= true;
		}

		assertTrue(pm.isCanceled());
		assertEquals(Collections.EMPTY_LIST.toString(), fLogEntries.toString());
		assertTrue(exception);
	}


	@Test
	public void testCreateChange() throws Exception {
		RenameRefactoring refactoring= new RenameRefactoring(new TestProcessor());

		fCancelStep= 1;
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		NullProgressMonitor pm= new NullProgressMonitor();

		boolean exception= false;
		try {
			ResourcesPlugin.getWorkspace().run(op, pm);
		} catch (OperationCanceledException e) {
			exception= true;
		}

		assertTrue(pm.isCanceled());
		assertEquals(Collections.EMPTY_LIST.toString(), fLogEntries.toString());
		assertTrue(exception);
	}

	@Test
	public void testCreatePreChange() throws Exception {
		RenameRefactoring refactoring= new RenameRefactoring(new TestProcessor());

		fCancelStep= 2;
		PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		NullProgressMonitor pm= new NullProgressMonitor();

		boolean exception= false;
		try {
			ResourcesPlugin.getWorkspace().run(op, pm);
		} catch (OperationCanceledException e) {
			exception= true;
		}

		assertTrue(pm.isCanceled());
		assertEquals(Collections.EMPTY_LIST.toString(), fLogEntries.toString());
		assertTrue(exception);
	}
}
