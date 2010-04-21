/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.perf;

import junit.framework.*;
import org.eclipse.test.performance.*;

public class UIStartupTest extends TestCase {

	static private final String EXPLANATION = "Performance decrease caused by a change to how the tests are installed. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=294094 for details.";

	public static Test suite() {
		return new TestSuite(UIStartupTest.class);
	}

	public UIStartupTest(String methodName) {
		super(methodName);
	}

	public void testUIApplicationStartup() {
		PerformanceMeter meter = Performance.getDefault().createPerformanceMeter(getClass().getName() + '.' + getName());
		try {
			meter.stop();
			Performance performance = Performance.getDefault();
			//TODO remove from global summary due to bug 294094. This should be re-enabled in 3.7.
			//			performance.tagAsGlobalSummary(meter, "Core UI Startup", Dimension.ELAPSED_PROCESS);
			performance.setComment(meter, Performance.EXPLAINS_DEGRADATION_COMMENT, EXPLANATION);
			meter.commit();
			performance.assertPerformanceInRelativeBand(meter, Dimension.ELAPSED_PROCESS, -50, 5);
		} finally {
			meter.dispose();
		}
	}
}
