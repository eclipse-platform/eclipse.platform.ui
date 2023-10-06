/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
package org.eclipse.ltk.core.refactoring.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


public class RefactoringContextTest {

	private static class TestRefactoring extends Refactoring {
		RefactoringStatus fInitialConditionStatus= new RefactoringStatus();
		RefactoringStatus fFinalConditionStatus= new RefactoringStatus();

		@Override
		public String getName() {
			return "test Refactoring";
		}

		@Override
		public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return fInitialConditionStatus;
		}

		@Override
		public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return fFinalConditionStatus;
		}

		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new NullChange();
		}
	}

	private static class TestRefactoringContext extends RefactoringContext {
		int fDisposeCalls;

		public TestRefactoringContext(Refactoring refactoring) {
			super(refactoring);
		}

		@Override
		public void dispose() {
			super.dispose();
			fDisposeCalls++;
		}
	}


	@Test
	public void testDisposeNormal() throws Exception {
		TestRefactoring ref= new TestRefactoring();
		TestRefactoringContext context= new TestRefactoringContext(ref);

		new PerformRefactoringOperation(context, CheckConditionsOperation.ALL_CONDITIONS).run(null);
		assertEquals(1, context.fDisposeCalls);

		try {
			context.dispose();
		} catch (IllegalStateException e) {
			return; //expected
		}
		fail("dispose must not be called twice");
	}

	@Test
	public void testDisposeInitialFailed() throws Exception {
		TestRefactoring ref= new TestRefactoring();
		ref.fInitialConditionStatus.addFatalError("fail");
		TestRefactoringContext context= new TestRefactoringContext(ref);

		new PerformRefactoringOperation(context, CheckConditionsOperation.ALL_CONDITIONS).run(null);
		assertEquals(1, context.fDisposeCalls);
	}

	@Test
	public void testDisposeFinalFailed() throws Exception {
		TestRefactoring ref= new TestRefactoring();
		ref.fFinalConditionStatus.addFatalError("fail");
		TestRefactoringContext context= new TestRefactoringContext(ref);

		new PerformRefactoringOperation(context, CheckConditionsOperation.ALL_CONDITIONS).run(null);
		assertEquals(1, context.fDisposeCalls);
	}

	@Test
	public void testDisposeChangeFailed() throws Exception {
		TestRefactoring ref= new TestRefactoring() {
			@Override
			public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
				throw new OperationCanceledException();
			}
		};
		TestRefactoringContext context= new TestRefactoringContext(ref);

		boolean cancelled= false;
		try {
			new PerformRefactoringOperation(context, CheckConditionsOperation.ALL_CONDITIONS).run(null);
		} catch (OperationCanceledException e) {
			cancelled= true;
		}
		assertTrue(cancelled);
		assertEquals(1, context.fDisposeCalls);
	}
}
