/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * MarkerViewTests are the tests for the marker view.
 * 
 * @since 3.4
 * 
 */
public class MarkerViewTests extends UITestCase {

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param testName
	 */
	public MarkerViewTests(String testName) {
		super(testName);
	}

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
