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
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(Bug_006708.suite());
		suite.addTest(Bug_025457.suite());
		suite.addTest(Bug_026294.suite());
		suite.addTest(Bug_027271.suite());
		suite.addTest(Bug_028981.suite());
		suite.addTest(Bug_029116.suite());
		suite.addTest(Bug_029671.suite());
		suite.addTest(Bug_029851.suite());
		suite.addTest(Bug_032076.suite());
		suite.addTest(Bug_044106.suite());
		suite.addTest(Bug_092108.suite());
		suite.addTest(Bug_098740.suite());
		suite.addTest(Bug_126104.suite());
		suite.addTest(Bug_127562.suite());
		suite.addTest(Bug_132510.suite());
		suite.addTest(Bug_134364.suite());
		suite.addTest(Bug_147232.suite());
		suite.addTest(Bug_160251.suite());
		suite.addTest(Bug_165892.suite());
		suite.addTest(Bug_226264.suite());
		suite.addTest(Bug_231301.suite());
		suite.addTest(IFileTest.suite());
		suite.addTest(IFolderTest.suite());
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
