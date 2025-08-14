/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test perspective switching.
 */
@RunWith(Parameterized.class)
public class PerspectiveSwitchTest extends BasicPerformanceTest {

	private final String id1;
	private final String id2;
	private final String activeEditor;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { // Test switching between the two most commonly used perspectives in the
												// SDK
				// (this is the most important
				// perspective switch test, but it is easily affected by changes in JDT, etc.)
				{ "org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.debug.ui.DebugPerspective", "1.java" },

				{ UIPerformanceTestSetup.PERSPECTIVE1, UIPerformanceTestSetup.PERSPECTIVE2, "1.perf_basic" },

				// Test switching between a perspective with lots of actions and a perspective
				// with none
				{ "org.eclipse.jdt.ui.JavaPerspective", "org.eclipse.ui.tests.util.EmptyPerspective", "1.perf_basic" },

				{ "org.eclipse.ui.resourcePerspective", "org.eclipse.jdt.ui.JavaPerspective", "1.java" } });
	}

	public PerspectiveSwitchTest(String id1, String id2, String activeEditor) {
		super("testPerspectiveSwitch:" + id1 + "," + id2 + ",editor " + activeEditor, BasicPerformanceTest.NONE);
		this.id1 = id1;
		this.id2 = id2;
		this.activeEditor = activeEditor;
	}

	/**
	 * Test perspective switching performance.
	 */
	@Test
	public void test() throws CoreException, WorkbenchException {
		// Get the two perspectives to switch between.
		final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
		final IPerspectiveDescriptor perspective1 = registry.findPerspectiveWithId(id1);
		final IPerspectiveDescriptor perspective2 = registry.findPerspectiveWithId(id2);

		// Don't fail if we reference an unknown perspective ID. This can be
		// a normal occurrance since the test suites reference JDT perspectives, which
		// might not exist. Just skip the test.
		if (perspective1 == null) {
			System.out.println("Unknown perspective ID: " + id1);
			return;
		}

		if (perspective2 == null) {
			System.out.println("Unknown perspective ID: " + id2);
			return;
		}

		// Open the two perspectives and the file, in a new window.
		// Do this outside the loop so as not to include
		// the initial time to open, just switching.
		IWorkbenchWindow window = openTestWindow(id1);
		final IWorkbenchPage page = window.getActivePage();
		assertNotNull(page);
		page.setPerspective(perspective2);

		// IFile aFile = getProject().getFile("1." +
		// EditorPerformanceSuite.EDITOR_FILE_EXTENSIONS[0]);
		IFile aFile = getProject().getFile(activeEditor);
		assertTrue(aFile.exists());

		IDE.openEditor(page, aFile, true);

		tagIfNecessary("UI - Perspective Switch", Dimension.ELAPSED_PROCESS);

		exercise(() -> {
			processEvents();

			startMeasuring();
			page.setPerspective(perspective1);
			processEvents();
			page.setPerspective(perspective2);
			processEvents();
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
	}
}
