/*******************************************************************************
 * Copyright (c) 2008, 2012 Versant Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Versant Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * If a view is set to non restorable during a session, it's part is not instantiated.
 * This tests that case, and the outcome should be that the view doesn't get 
 * instanciated in the second session.
 * 
 * @since 3.4
 */
public class NonRestorableViewTest extends TestCase {

	private static final String NON_RESTORABLE_VIEW_ID = "org.eclipse.ui.tests.session.NonRestorableView";

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.NonRestorableViewTest");
		ts.addTest(new NonRestorableViewTest("test01ActivateView"));
		ts.addTest(new NonRestorableViewTest("test02SecondOpening"));
		return ts;
	}

	public NonRestorableViewTest(String testName) {
		super(testName);
	}

	/**
	 * This is the first part  instantiates a non restorable view
	 * 
	 * @throws Throwable
	 */
	public void test01ActivateView() throws Throwable {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		IViewPart part = page.showView(NON_RESTORABLE_VIEW_ID);
		assertNotNull(part);
		assertTrue(part instanceof NonRestorableView);
	}

	/**
	 * In the second session the view shouldn't be
	 * instantiated.
	 * 
	 * @throws Throwable
	 */
	public void test02SecondOpening() throws Throwable {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		IViewReference[] views = page.getViewReferences();
		for (int i = 0; i < views.length; i++) {
			IViewReference ref = views[i];
			if (ref.getId().equals(NON_RESTORABLE_VIEW_ID)) {
				fail("Should not find this view");
			}
		}
	}
}
