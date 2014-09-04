/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for {@code org.eclipse.ui.monitoring} plug-in.
 * The tests in {@link EventLoopMonitorThreadManualTests} are not included in this
 * suite due to their flakiness.
 */
public class MonitoringTestSuite extends TestSuite {
	public static Test suite() {
		return new MonitoringTestSuite();
	}

	public MonitoringTestSuite() {
		addTestSuite(EventLoopMonitorThreadTests.class);
		addTestSuite(FilterHandlerTests.class);
		addTestSuite(DefaultLoggerTests.class);
	}
}
