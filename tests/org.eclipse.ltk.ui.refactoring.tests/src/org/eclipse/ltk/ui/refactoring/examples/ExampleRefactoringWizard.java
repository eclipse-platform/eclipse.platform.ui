/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.ui.refactoring.examples;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;


public class ExampleRefactoringWizard extends RefactoringWizard {

	public ExampleRefactoringWizard(ExampleRefactoring refactoring, int flags) {
		super(refactoring, flags);
		setDefaultPageTitle("My Example Refactoring");
		setWindowTitle("My Example Refactoring");
	}

	@Override
	protected void addUserInputPages() {
		addPage(new ExampleRefactoringConfigurationPage((ExampleRefactoring) getRefactoring()));
	}

}
