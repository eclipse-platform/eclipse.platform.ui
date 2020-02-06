/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.tests.resources.regression;

import junit.framework.*;

/**
 * A suite that runs all regression tests.
 */
public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(Bug_006708.class);
		suite.addTestSuite(Bug_025457.class);
		suite.addTestSuite(Bug_026294.class);
		suite.addTestSuite(Bug_027271.class);
		suite.addTestSuite(Bug_028981.class);
		suite.addTestSuite(Bug_029116.class);
		suite.addTestSuite(Bug_029671.class);
		suite.addTestSuite(Bug_029851.class);
		suite.addTestSuite(Bug_032076.class);
		suite.addTestSuite(Bug_044106.class);
		suite.addTestSuite(Bug_092108.class);
		suite.addTestSuite(Bug_097608.class);
		suite.addTestSuite(Bug_098740.class);
		suite.addTestSuite(Bug_126104.class);
		suite.addTestSuite(Bug_127562.class);
		suite.addTestSuite(Bug_132510.class);
		suite.addTestSuite(Bug_134364.class);
		suite.addTestSuite(Bug_147232.class);
		suite.addTestSuite(Bug_160251.class);
		suite.addTestSuite(Bug_165892.class);
		suite.addTestSuite(Bug_192631.class);
		suite.addTestSuite(Bug_226264.class);
		suite.addTestSuite(Bug_231301.class);
		suite.addTestSuite(Bug_233939.class);
		suite.addTestSuite(Bug_265810.class);
		suite.addTestSuite(Bug_264182.class);
		suite.addTestSuite(Bug_288315.class);
		suite.addTestSuite(Bug_303517.class);
		suite.addTestSuite(Bug_329836.class);
		suite.addTestSuite(Bug_331445.class);
		suite.addTestSuite(Bug_332543.class);
		suite.addTestSuite(Bug_378156.class);
		suite.addTestSuite(IFileTest.class);
		suite.addTestSuite(IFolderTest.class);
		suite.addTestSuite(IProjectTest.class);
		suite.addTestSuite(IResourceTest.class);
		suite.addTestSuite(IWorkspaceTest.class);
		suite.addTestSuite(LocalStoreRegressionTests.class);
		suite.addTestSuite(NLTest.class);
		suite.addTestSuite(PR_1GEAB3C_Test.class);
		suite.addTestSuite(PR_1GH2B0N_Test.class);
		suite.addTestSuite(PR_1GHOM0N_Test.class);
		suite.addTestSuite(Bug_530868.class);
		suite.addTestSuite(Bug_185247_recursiveLinks.class);
		suite.addTestSuite(Bug_185247_LinuxTests.class);
		return suite;
	}
}
