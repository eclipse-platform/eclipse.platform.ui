/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
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
		suite.addTest(BenchFileStore.suite());
		suite.addTest(BenchWorkspace.suite());
		suite.addTest(BenchMiscWorkspace.suite());
		suite.addTest(MarkerPerformanceTest.suite());
		suite.addTest(LocalHistoryPerformanceTest.suite());
		suite.addTest(WorkspacePerformanceTest.suite());
		suite.addTest(PropertyManagerPerformanceTest.suite());
		// these tests are flawed - see bug 57137
		// suite.addTest(ContentDescriptionPerformanceTest.suite());
		return suite;
	}

}
