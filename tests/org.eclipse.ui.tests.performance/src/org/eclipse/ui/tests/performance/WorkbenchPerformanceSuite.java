/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.performance.layout.PerspectiveWidgetFactory;
import org.eclipse.ui.tests.performance.layout.ResizeTest;

import junit.framework.Test;
import junit.framework.TestSuite;

class WorkbenchPerformanceSuite extends TestSuite {

	private static String RESOURCE_PERSPID = "org.eclipse.ui.resourcePerspective";
	// Note: to test perspective switching properly, we need perspectives with lots
	// of
	// associated actions.
	// NOTE - do not change the order of the IDs below. the PerspectiveSwitchTest
	// has a
	// fingerprint test for performance that releys on this not changing.
	public static final String[] PERSPECTIVE_IDS = { EmptyPerspective.PERSP_ID2, UIPerformanceTestSetup.PERSPECTIVE1,
			RESOURCE_PERSPID, "org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.debug.ui.DebugPerspective" };

	// Perspective ID to use for the resize window fingerprint test
	public static String resizeFingerprintTest = RESOURCE_PERSPID;

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new WorkbenchPerformanceSuite();
	}

	public WorkbenchPerformanceSuite() {
		addResizeScenarios();
	}


	private void addResizeScenarios() {
		for (String id : PERSPECTIVE_IDS) {
			addTest(new ResizeTest(new PerspectiveWidgetFactory(id),
					id.equals(resizeFingerprintTest) ? BasicPerformanceTest.LOCAL : BasicPerformanceTest.NONE,
					"UI - Workbench Window Resize"));
		}
	}
}
