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
import static org.eclipse.ui.tests.performance.UIPerformanceTestRule.getTestProject;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @since 3.1
 */
@RunWith(Parameterized.class)
public class OpenMultipleEditorTest extends BasicPerformanceTest {

	@ClassRule
	public static final UIPerformanceTestRule uiPerformanceTestRule = new UIPerformanceTestRule();

	private final String extension;
	private final boolean closeAll;

	@Parameters(name = "{index}: closeAll: {0} - closeEach: {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "perf_basic", true }, { "perf_outline", true }, { "java", true },
				{ "perf_basic", false }, { "perf_outline", false }, { "java", false } });
	}

	public OpenMultipleEditorTest(String extension, boolean closeAll) {
		this.extension = extension;
		this.closeAll = closeAll;
	}

	@Test
	public void test() throws Throwable {
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestRule.PERSPECTIVE1);
		IWorkbenchPage activePage = window.getActivePage();

		startMeasuring();

		for (int i = 0; i < 100; i++) {
			IFile file = getTestProject().getFile(i + "." + extension);
			IDE.openEditor(activePage, file, true);
			processEvents();
		}
		if (closeAll) {
			activePage.closeAllEditors(false);
		}
		else {
			IEditorReference[] parts = activePage.getEditorReferences();
			for (IEditorReference part : parts) {
				activePage.closeEditor(part.getEditor(false), false);
			}
		}
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

}
