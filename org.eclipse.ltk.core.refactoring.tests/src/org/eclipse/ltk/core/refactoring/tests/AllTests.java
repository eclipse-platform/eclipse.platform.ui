/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ltk.core.refactoring.tests.participants.FailingParticipantTests;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite= new TestSuite("All LTK Refactoring Core Tests"); //$NON-NLS-1$
		suite.addTestSuite(FailingParticipantTests.class);
		suite.addTestSuite(SharedTextChangeTest.class);
		return suite;
	}
}

