/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests;

import junit.framework.TestCase;

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


public class RefactoringContextTest extends TestCase {

	private static class TestRefactoring extends Refactoring {
		RefactoringStatus fInitialConditionStatus= new RefactoringStatus();
		RefactoringStatus fFinalConditionStatus= new RefactoringStatus();

		public String getName() {
			return "test Refactoring";
		}

		public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return fInitialConditionStatus;
		}

		public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return fFinalConditionStatus;
		}

		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			return new NullChange();
		}
	}

	private static class TestRefactoringContext extends RefactoringContext {
		int fDisposeCalls;

		public TestRefactoringContext(Refactoring refactoring) {
			super(refactoring);
		}

		public void dispose() {
			super.dispose();
			fDisposeCalls++;
		}
	}


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

	public void testDisposeInitialFailed() throws Exception {
		TestRefactoring ref= new TestRefactoring();
		ref.fInitialConditionStatus.addFatalError("fail");
		TestRefactoringContext context= new TestRefactoringContext(ref);

		new PerformRefactoringOperation(context, CheckConditionsOperation.ALL_CONDITIONS).run(null);
		assertEquals(1, context.fDisposeCalls);
	}

	public void testDisposeFinalFailed() throws Exception {
		TestRefactoring ref= new TestRefactoring();
		ref.fFinalConditionStatus.addFatalError("fail");
		TestRefactoringContext context= new TestRefactoringContext(ref);

		new PerformRefactoringOperation(context, CheckConditionsOperation.ALL_CONDITIONS).run(null);
		assertEquals(1, context.fDisposeCalls);
	}

	public void testDisposeChangeFailed() throws Exception {
		TestRefactoring ref= new TestRefactoring() {
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
