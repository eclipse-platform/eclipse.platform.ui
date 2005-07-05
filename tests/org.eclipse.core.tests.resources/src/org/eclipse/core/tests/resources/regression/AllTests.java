/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
		suite.addTest(Bug_25457.suite());
		suite.addTest(Bug_26294.suite());
		suite.addTest(Bug_27271.suite());
		suite.addTest(Bug_28981.suite());
		suite.addTest(Bug_29116.suite());
		suite.addTest(Bug_29671.suite());
		suite.addTest(Bug_29851.suite());
		suite.addTest(Bug_32076.suite());
		suite.addTest(Bug_44106.suite());
		suite.addTest(Bug_6708.suite());
		suite.addTest(Bug_98740.suite());
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
