/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.*;

public class AllTests extends TestCase {
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests() {
	super(null);
}
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests(String name) {
	super(name);
}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(Bug_6708.suite());
		suite.addTest(IProjectTest.suite());
		suite.addTest(IResourceTest.suite());
		suite.addTest(IWorkspaceTest.suite());
		suite.addTest(LocalStoreRegressionTests.suite());
		suite.addTest(NLTest.suite());
		suite.addTest(PR_1GEAB3C_Test.suite());
		suite.addTest(PR_1GH2B0N_Test.suite());
		suite.addTest(PR_1GHOM0N_Test.suite());
		return suite;
	}
}
