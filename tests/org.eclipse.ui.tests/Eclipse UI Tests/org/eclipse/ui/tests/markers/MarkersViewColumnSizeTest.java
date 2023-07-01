/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import junit.framework.TestSuite;

/**
 * @since 3.4
 *
 */
@RunWith(JUnit4.class)
public class MarkersViewColumnSizeTest extends UITestCase {

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.markers.MarkersViewColumnSizeTest");
		ts.addTest(new MarkersViewColumnSizeTest("testColumnCreate"));
		ts.addTest(new MarkersViewColumnSizeTest("testColumnRestore"));
		return ts;
	}

	public MarkersViewColumnSizeTest() {
		super(MarkersViewColumnSizeTest.class.getSimpleName());
	}

	public MarkersViewColumnSizeTest(String name) {
		super(name);
	}


	@Test
	public void testColumnCreate() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			assertTrue("Could not get a workbench window", false);
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			assertTrue("Could not get a workbench page", false);
		}

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

	@Test
	public void testColumnRestore() {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			assertTrue("Could not get a workbench window", false);
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			assertTrue("Could not get a workbench page", false);
		}

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
