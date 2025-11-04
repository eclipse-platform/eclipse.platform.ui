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

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.jupiter.api.extension.ExtendWith;

@RunWith(Parameterized.class)
@ExtendWith(CloseTestWindowsExtension.class)
public class OpenCloseWindowTest extends PerformanceTestCaseJunit4 {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	

	private final String id;

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { EmptyPerspective.PERSP_ID2 }, { UIPerformanceTestRule.PERSPECTIVE1 },
				{ "org.eclipse.ui.resourcePerspective" }, { "org.eclipse.jdt.ui.JavaPerspective" },
				{ "org.eclipse.debug.ui.DebugPerspective" } });
	}

	public OpenCloseWindowTest(String id) {
		this.id = id;
	}

	@Test
	public void test() throws Throwable {
		exercise(() -> {
			processEvents();
			EditorTestHelper.calmDown(500, 30000, 500);

			startMeasuring();
			IWorkbenchWindow window = openTestWindow(id);
			processEvents();
			window.close();
			processEvents();
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
	}
}
