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
package org.eclipse.ltk.core.refactoring.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ltk.core.refactoring.tests.history.RefactoringHistoryTests;
import org.eclipse.ltk.core.refactoring.tests.participants.ParticipantTests;
import org.eclipse.ltk.core.refactoring.tests.resource.ResourceRefactoringTests;
import org.eclipse.ltk.core.refactoring.tests.scripting.RefactoringScriptingTests;

public class AllTests {

	public static Test suite() {
		TestSuite suite= new TestSuite(AllTests.class.getName());
		
		suite.addTestSuite(RefactoringContextTest.class);
		
		suite.addTest(ParticipantTests.suite());
		suite.addTest(RefactoringHistoryTests.suite());
		suite.addTest(RefactoringScriptingTests.suite());
		suite.addTest(ResourceRefactoringTests.suite());
		return suite;
	}
}
