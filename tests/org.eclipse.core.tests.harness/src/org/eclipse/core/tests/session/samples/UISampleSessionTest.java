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

import junit.framework.Test;
import junit.framework.TestCase;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.tests.harness.CoreTest;
import org.eclipse.core.tests.session.SessionTestSuite;
import org.eclipse.test.performance.*;

public class UISampleSessionTest extends TestCase {
	public UISampleSessionTest(String methodName) {
		super(methodName);
	}

	public void testApplicationStartup() {
		Policy.debug("Running " + getName());
		PerformanceMeter meter = Performance.getDefault().createPerformanceMeter(getClass().getName() + ".UIStartup");
		try {
			meter.stop();
			meter.commit();
			Performance.getDefault().assertPerformanceInRelativeBand(meter, Dimension.ELAPSED_PROCESS, -50, 5);
		} finally {
			meter.dispose();
		}
	}

	public static Test suite() {
		SessionTestSuite suite = new SessionTestSuite(CoreTest.PI_HARNESS);
		suite.setApplicationId(SessionTestSuite.UI_TEST_APPLICATION);
		for (int i = 0; i < 3; i++)
			suite.addTest(new UISampleSessionTest("testApplicationStartup"));
		return suite;
	}

}