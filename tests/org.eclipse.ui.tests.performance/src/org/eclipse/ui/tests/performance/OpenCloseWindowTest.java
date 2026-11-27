/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;

import java.util.stream.Stream;

import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OpenCloseWindowTest {

	@RegisterExtension
	static UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	static Stream<Arguments> data() {
		return Stream.of(Arguments.of(EmptyPerspective.PERSP_ID2), Arguments.of(UIPerformanceTestRule.PERSPECTIVE1),
				Arguments.of("org.eclipse.ui.resourcePerspective"), Arguments.of("org.eclipse.jdt.ui.JavaPerspective"),
				Arguments.of("org.eclipse.debug.ui.DebugPerspective"));
	}

	@ParameterizedTest(name = "{index}: {0}")
	@MethodSource("data")
	void test(String id, TestInfo testInfo) throws Throwable {
		Performance perf = Performance.getDefault();
		String scenarioId = this.getClass().getName() + "." + testInfo.getDisplayName();
		PerformanceMeter meter = perf.createPerformanceMeter(scenarioId);
		try {
			exercise(() -> {
				processEvents();
				EditorTestHelper.calmDown(500, 30000, 500);

				meter.start();
				IWorkbenchWindow window = openTestWindow(id);
				processEvents();
				window.close();
				processEvents();
				meter.toString();
			});

			meter.commit();
			perf.assertPerformance(meter);
		} finally {
			meter.dispose();
		}
	}
}
