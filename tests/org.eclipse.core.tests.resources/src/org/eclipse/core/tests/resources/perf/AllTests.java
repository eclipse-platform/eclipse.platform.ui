/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
		suite.addTest(BenchHistoryStoreTest.suite());
		suite.addTest(BenchWorkspace.suite());
		suite.addTest(BenchMiscWorkspace.suite());
		suite.addTest(MarkerPerformanceTest.suite());
		suite.addTest(LocalHistoryPerformanceTest.suite());
		return suite;
	}

}
