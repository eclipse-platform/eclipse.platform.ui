/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.eclipse.ltk.core.refactoring.tests.history.RefactoringHistoryTests;
import org.eclipse.ltk.core.refactoring.tests.participants.ParticipantTests;
import org.eclipse.ltk.core.refactoring.tests.resource.ResourceRefactoringTests;
import org.eclipse.ltk.core.refactoring.tests.resource.ResourceRefactoringUndoTests;
import org.eclipse.ltk.core.refactoring.tests.scripting.RefactoringScriptingTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	RefactoringContextTest.class,
	ParticipantTests.class,
	RefactoringHistoryTests.class,
	RefactoringScriptingTests.class,
	ResourceRefactoringTests.class,
	ResourceRefactoringUndoTests.class
})
public class AllTests {
}
