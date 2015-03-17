/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.history;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class MockRefactoringContribution extends RefactoringContribution {

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map<String, String> arguments, int flags) {
		return new MockRefactoringDescriptor(project, description, comment, arguments, flags);
	}

	@Override
	public Map<String, String> retrieveArgumentMap(RefactoringDescriptor descriptor) {
		if (descriptor instanceof MockRefactoringDescriptor) {
			MockRefactoringDescriptor extended= (MockRefactoringDescriptor) descriptor;
			return extended.getArguments();
		}
		return super.retrieveArgumentMap(descriptor);
	}
}