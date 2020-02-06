/*******************************************************************************
 *  Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.perf;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.1
 */
public class AllTests extends TestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTestSuite(BenchFileStore.class);
		suite.addTestSuite(BenchWorkspace.class);
		suite.addTestSuite(BenchMiscWorkspace.class);
		suite.addTestSuite(BuilderPerformanceTest.class);
		suite.addTestSuite(MarkerPerformanceTest.class);
		suite.addTestSuite(LocalHistoryPerformanceTest.class);
		suite.addTestSuite(WorkspacePerformanceTest.class);
		suite.addTestSuite(PropertyManagerPerformanceTest.class);
		suite.addTestSuite(FileSystemPerformanceTest.class);
		// these tests are flawed - see bug 57137
		// suite.addTest(ContentDescriptionPerformanceTest.suite());
		return suite;
	}

}
