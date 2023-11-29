/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
package org.eclipse.ui.tests.session;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * If a view is not activated during a session, it's part is not instantiated.
 * This tests that case, and the outcome should be the view has it's last
 * session state when it is finally instantiated in the workbench.
 *
 * @since 3.1.1
 */
public class Bug98800Test extends TestCase {
	private static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";

	private static final String VIEW_WITH_STATE_ID = "org.eclipse.ui.tests.session.ViewWithState";

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.Bug98800Test");
		ts.addTest(new Bug98800Test("testActivateView"));
		ts.addTest(new Bug98800Test("testSecondOpening"));
		ts.addTest(new Bug98800Test("testSavedMemento"));
		return ts;
	}

	private final IWorkbenchPage fPage;

	public Bug98800Test(String testName) {
		super(testName);
		fPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();
	}

	/**
	 * This is the first part of a 3 part tests.  First instantiate a view
	 * and set a state.
	 */
	public void testActivateView() throws Throwable {
		IViewPart v = fPage.showView(VIEW_WITH_STATE_ID);

		// put another view in front of our view
		fPage.showView(PROBLEM_VIEW_ID);

		// set a state so it can be saved
		ViewWithState view = (ViewWithState) v;
		view.fState = 10;
	}

	/**
	 * The second session doesn't activate the view, so it should not
	 * be instantiated.
	 */
	public void testSecondOpening() throws Throwable {
		IViewReference[] views = fPage.getViewReferences();
		for (IViewReference ref : views) {
			if (ref.getId().equals(VIEW_WITH_STATE_ID)) {
				assertNull("The view should not be instantiated", ref
						.getPart(false));
			}
		}
	}

	/**
	 * Activate the view and it's state should re-appear.
	 */
	public void testSavedMemento() throws Throwable {
		IViewPart v = fPage.showView(VIEW_WITH_STATE_ID);
		ViewWithState view = (ViewWithState) v;
		assertEquals(
				"the view state should have made it through a session without instantiation",
				10, view.fState);

		// the state should not be saved between a close and
		// an open in the same session
		fPage.hideView(v);
		v = fPage.showView(VIEW_WITH_STATE_ID);
		view = (ViewWithState) v;
		assertEquals("The view state should be reset", 0, view.fState);
	}
}
