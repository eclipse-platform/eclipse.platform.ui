/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.markers;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.4
 * 
 */
public class MarkersViewColumnSizeTest extends UITestCase {

	/**
	 * @param testName
	 */
	public MarkersViewColumnSizeTest() {
		super("MarkersViewColumnSizeTest");
	}

	public void testColumnCreate() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null)
			assertTrue("Could not get a workbench window", false);
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			assertTrue("Could not get a workbench page", false);

		MarkersTestMarkersView problemView;
		try {
			problemView = (MarkersTestMarkersView) page
					.showView("org.eclipse.ui.tests.markerTests");
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}

		problemView.setColumnWidths(100);

	}
	
	public void testColumnRestore() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null)
			assertTrue("Could not get a workbench window", false);
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			assertTrue("Could not get a workbench page", false);

		MarkersTestMarkersView problemView;
		try {
			problemView = (MarkersTestMarkersView) page
					.showView("org.eclipse.ui.tests.markerTests");
		} catch (PartInitException e) {
			assertTrue(e.getLocalizedMessage(), false);
			return;
		}

		assertTrue("Column sizes not restored", problemView
				.checkColumnSizes(100));
	}
}
