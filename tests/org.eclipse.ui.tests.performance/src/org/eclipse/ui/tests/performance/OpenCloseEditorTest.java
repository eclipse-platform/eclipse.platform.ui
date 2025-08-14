/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenCloseEditorTest extends BasicPerformanceTest {

	private final String extension;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "perf_basic", BasicPerformanceTest.NONE },
				{ "perf_outline", BasicPerformanceTest.NONE }, { "java", BasicPerformanceTest.LOCAL } });
	}


	public OpenCloseEditorTest(String extension, int tagging) {
		super("testOpenAndCloseEditors:" + extension, tagging);
		this.extension = extension;
	}

	@Test
	public void test() throws Throwable {
		final IFile file = getProject().getFile("1." + extension);
		assertTrue(file.exists());

		IWorkbenchWindow window = openTestWindow(UIPerformanceTestSetup.PERSPECTIVE1);
		final IWorkbenchPage activePage = window.getActivePage();

		exercise(() -> {
			startMeasuring();
			for (int j = 0; j < 10; j++) {
				IEditorPart part;
				try {
					part = IDE.openEditor(activePage, file, true);
				} catch (PartInitException e) {
					throw new AssertionError("Can't open editor for " + file.getName());
				}
				processEvents();
				activePage.closeEditor(part, false);
				processEvents();

			}
			stopMeasuring();
		});

		tagIfNecessary("UI - Open/Close Editor", Dimension.ELAPSED_PROCESS);
		commitMeasurements();
		assertPerformance();
	}
}
