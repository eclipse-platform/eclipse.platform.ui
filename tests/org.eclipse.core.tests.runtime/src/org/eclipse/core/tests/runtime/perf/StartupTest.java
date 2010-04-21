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

public class StartupTest extends TestCase {

	static private final String explanation = "Performance decrease caused by a change to how the tests are installed. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=294094 for details.";

	public static Test suite() {
		return new TestSuite(StartupTest.class);
	}

	public StartupTest(String methodName) {
		super(methodName);
	}

	public void testApplicationStartup() {
		PerformanceMeter meter = Performance.getDefault().createPerformanceMeter(getClass().getName() + '.' + getName());
		try {
			meter.stop();
			// tag for showing in the performance fingerprint graph
			Performance performance = Performance.getDefault();
			//TODO remove from global summary due to bug 294094. This should be re-enabled in 3.7.
			//			performance.tagAsGlobalSummary(meter, "Core Headless Startup", Dimension.ELAPSED_PROCESS);
			performance.setComment(meter, Performance.EXPLAINS_DEGRADATION_COMMENT, explanation);
			String reportOption = System.getProperty("eclipseTest.ReportResults");
			boolean bReport = (reportOption == null) ? true : !("false".equalsIgnoreCase(reportOption));
			if (bReport)
				meter.commit();
			Performance.getDefault().assertPerformanceInRelativeBand(meter, Dimension.ELAPSED_PROCESS, -100, 5);
		} finally {
			meter.dispose();
		}
	}
}
