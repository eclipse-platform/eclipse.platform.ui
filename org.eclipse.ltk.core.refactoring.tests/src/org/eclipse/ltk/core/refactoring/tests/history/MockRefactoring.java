/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.history;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class MockRefactoring extends Refactoring {

	private Map fArguments= Collections.EMPTY_MAP;

	private String fComment= "A mock comment";

	private String fDescription= "A mock description";

	private int fFlags= RefactoringDescriptor.BREAKING_CHANGE;

	private String fProject= "MockProject";

	public MockRefactoring() {

	}

	public MockRefactoring(String project, String description, String comment, Map arguments, int flags) {
		fProject= project;
		fDescription= description;
		fComment= comment;
		fArguments= arguments;
		fFlags= flags;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		return new NullChange() {

			/**
			 * {@inheritDoc}
			 */
			public ChangeDescriptor getDescriptor() {
				MockRefactoringDescriptor descriptor= createRefactoringDescriptor();
				return new RefactoringChangeDescriptor(descriptor);
			}
		};
	}

	public MockRefactoringDescriptor createRefactoringDescriptor() {
		return new MockRefactoringDescriptor(fProject, fDescription, fComment, fArguments, fFlags);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return "mock";
	}
}