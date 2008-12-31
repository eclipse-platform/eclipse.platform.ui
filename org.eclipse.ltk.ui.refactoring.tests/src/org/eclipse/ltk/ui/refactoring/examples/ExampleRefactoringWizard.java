/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.ui.refactoring.RefactoringWizard#addUserInputPages()
	 */
	protected void addUserInputPages() {
		addPage(new ExampleRefactoringConfigurationPage((ExampleRefactoring) getRefactoring()));
	}

}
