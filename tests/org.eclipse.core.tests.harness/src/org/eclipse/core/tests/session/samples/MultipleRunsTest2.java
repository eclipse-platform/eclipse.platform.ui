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
package org.eclipse.core.tests.session.samples;

import junit.framework.*;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.session.PerformanceSessionTestSuite;

public class MultipleRunsTest2 extends TestCase {
	public static Test suite() {
		PerformanceSessionTestSuite suite = new PerformanceSessionTestSuite(EclipseWorkspaceTest.PI_HARNESS, 1000);
		suite.addTest(new TestSuite(SampleSessionTest.class));
		return suite;
	}
}