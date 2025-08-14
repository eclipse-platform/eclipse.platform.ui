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

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenCloseWindowTest extends BasicPerformanceTest {

	private final String id;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { EmptyPerspective.PERSP_ID2 }, { UIPerformanceTestSetup.PERSPECTIVE1 },
				{ "org.eclipse.ui.resourcePerspective" }, { "org.eclipse.jdt.ui.JavaPerspective" },
				{ "org.eclipse.debug.ui.DebugPerspective" } });
	}

	public OpenCloseWindowTest(String id) {
		super("testOpenCloseWindows:" + id, BasicPerformanceTest.NONE);
		this.id = id;
	}

	@Test
	public void test() throws Throwable {
		tagIfNecessary("UI - Open/Close Window", Dimension.ELAPSED_PROCESS);

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
