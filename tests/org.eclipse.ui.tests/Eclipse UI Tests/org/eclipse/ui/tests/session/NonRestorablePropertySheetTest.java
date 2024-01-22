/*******************************************************************************
 * Copyright (c) 2008, 2012 Versant Corp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.session;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * The secondary property sheets should be closed so there aren't restored in
 * the next workbench session.
 *
 * @since 3.4
 */
public class NonRestorablePropertySheetTest extends TestCase {

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.NonRestorablePropertySheetTest");
		ts.addTest(new NonRestorablePropertySheetTest("test01ActivateView"));
		ts.addTest(new NonRestorablePropertySheetTest("test02SecondOpening"));
		return ts;
	}

	public NonRestorablePropertySheetTest(String testName) {
		super(testName);
	}

	/**
	 * This is the first part instantiates a bunch of property sheets
	 */
	public void test01ActivateView() throws PartInitException {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		IViewPart part = page.showView(IPageLayout.ID_PROP_SHEET);
		assertNotNull(part);
		assertTrue(part instanceof PropertySheet);

		for (int j = 0; j < 3; j++) {
			try {
				page.showView(IPageLayout.ID_PROP_SHEET, "#" + j,
						IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				fail(e.getMessage());
			}
		}
		assertTrue(countPropertySheetViews(page) == 4);
	}

	/**
	 * In the second session the property sheet views with secondary ids
	 * shouldn't be instantiated.
	 */
	public void test02SecondOpening() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		assertTrue(countPropertySheetViews(page) == 1);
	}

	// simple counts how many property sheet instances are open
	private int countPropertySheetViews(final IWorkbenchPage page) {
		int count = 0;
		IViewReference[] views = page.getViewReferences();
		for (IViewReference ref : views) {
			if (ref.getId().equals(IPageLayout.ID_PROP_SHEET)) {
				count++;
			}
		}
		return count;
	}

}
