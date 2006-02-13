/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

/**
 * A {@link RefactoringChangeDescriptor} describes changes created by a
 * refactoring.
 * 
 * @since 3.2
 */
public class RefactoringChangeDescriptor extends ChangeDescriptor {
	
	/** The refactoring descriptor */
	private final RefactoringDescriptor fRefactoringDescriptor;

	/**
	 * Creates the <code>RefactoringChangeDescriptor</code> with the {@link RefactoringDescriptor}
	 * that originated the change.
	 * 
	 * @param refactoringDescriptor the {@link RefactoringDescriptor} that originated the change.
	 */
	public RefactoringChangeDescriptor(final RefactoringDescriptor refactoringDescriptor) {
		fRefactoringDescriptor= refactoringDescriptor;
	}
	
	/**
	 * Returns the {@link RefactoringDescriptor} that originated the change.
	 *
	 * @return the {@link RefactoringDescriptor} that originated the change.
	 */
	public RefactoringDescriptor getRefactoringDescriptor() {
		return fRefactoringDescriptor;
	}
}