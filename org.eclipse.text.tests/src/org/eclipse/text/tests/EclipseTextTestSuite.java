/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.text.tests;

import org.eclipse.text.tests.link.LinkTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test Suite for org.eclipse.text.
 * 
 * @since 3.0
 */
public class EclipseTextTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Suite for org.eclipse.text"); //$NON-NLS-1$
		//$JUnit-BEGIN$
		suite.addTest(LineTrackerTest4.suite());
		suite.addTest(DocumentExtensionTest.suite());
		suite.addTest(LineTrackerTest3.suite());
		suite.addTest(DocumentTest.suite());
		suite.addTest(FindReplaceDocumentAdapterTest.suite());
		suite.addTest(PositionUpdatingCornerCasesTest.suite());
		suite.addTest(TextEditTests.suite());
		suite.addTest(GapTextTest.suite());
		suite.addTest(ChildDocumentTest.suite());
		suite.addTest(ProjectionTestSuite.suite());
		suite.addTest(LinkTestSuite.suite());
		suite.addTest(CopyOnWriteTextStoreTest.suite());
		//$JUnit-END$
		
		return suite;
	}
}
