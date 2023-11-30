/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * MarkerViewTests are the tests for the marker view.
 *
 * @since 3.4
 */
@RunWith(JUnit4.class)
public class MarkerViewTests extends UITestCase {

	/**
	 * Create a new instance of the receiver.
	 */
	public MarkerViewTests() {
		super(MarkerViewTests.class.getSimpleName());
	}

	@Test
	public void testOpenView() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		try {
			page.showView(IPageLayout.ID_BOOKMARKS);
			page.showView(IPageLayout.ID_PROBLEM_VIEW);
			page.showView(IPageLayout.ID_TASK_LIST);
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}

	}
}
