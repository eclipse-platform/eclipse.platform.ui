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
package org.eclipse.core.tests.runtime.perf;

import junit.framework.*;
import org.eclipse.core.tests.runtime.RuntimeTestsPlugin;
import org.eclipse.core.tests.session.PerformanceSessionTestSuite;
import org.eclipse.core.tests.session.UIPerformanceSessionTestSuite;

public class AllTests extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		suite.addTest(new PerformanceSessionTestSuite(RuntimeTestsPlugin.PI_RUNTIME_TESTS, 5, StartupTest.class));
		suite.addTest(new UIPerformanceSessionTestSuite(RuntimeTestsPlugin.PI_RUNTIME_TESTS, 5, UIStartupTest.class));		
		return suite;
	}
}