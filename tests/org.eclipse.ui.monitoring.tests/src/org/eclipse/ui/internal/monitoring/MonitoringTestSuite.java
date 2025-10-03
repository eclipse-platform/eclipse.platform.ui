/*******************************************************************************
 * Copyright (C) 2014 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Prigogin (Google) - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 443391
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for {@code org.eclipse.ui.monitoring} plug-in.
 * The tests in {@link EventLoopMonitorThreadManualTests} are not included in this
 * suite due to their flakiness.
 */
@Suite
@SelectClasses({
	EventLoopMonitorThreadTests.class,
	FilterHandlerTests.class,
	DefaultLoggerTests.class})
public class MonitoringTestSuite {
}
