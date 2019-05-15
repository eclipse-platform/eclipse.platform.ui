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

import org.eclipse.core.resources.IFile;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

/**
 * @since 3.1
 */
public class OpenMultipleEditorTest extends BasicPerformanceTest {

	private String extension;
	private boolean closeAll;

	/**
	 * @param testName
	 */
	public OpenMultipleEditorTest(String extension, boolean closeAll, int tagging) {
		super ("testOpenMultipleEditors:" + extension + (closeAll ? "[closeAll]" : "[closeEach]"), tagging);
		this.extension = extension;
		this.closeAll = closeAll;
	}

	@Override
	protected void runTest() throws Throwable {
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestSetup.PERSPECTIVE1);
		IWorkbenchPage activePage = window.getActivePage();

		tagIfNecessary("UI - Open Multiple Editors",Dimension.ELAPSED_PROCESS);

		startMeasuring();

		for (int i = 0; i < 100; i++) {
			IFile file = getProject().getFile(i + "." + extension);
			IDE.openEditor(activePage, file, true);
			processEvents();
		}
		if (closeAll) {
			activePage.closeAllEditors(false);
		}
		else {
			IEditorPart [] parts = activePage.getEditors();
			for (IEditorPart part : parts) {
				activePage.closeEditor(part, false);
			}
		}
		stopMeasuring();
		commitMeasurements();
		assertPerformance();
	}

}
