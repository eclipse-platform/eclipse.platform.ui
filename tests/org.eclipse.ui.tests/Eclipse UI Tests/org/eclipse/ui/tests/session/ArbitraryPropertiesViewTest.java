/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.PlatformUI;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * If a view is not activated during a session, it's part is not instantiated.
 * This tests that case, and the outcome should be the view has it's last
 * session state when it is finally instantiated in the workbench.
 *
 * @since 3.3
 */
public class ArbitraryPropertiesViewTest extends TestCase {

	private static final String USER_PROP = "org.eclipse.ui.tests.users";

	private static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";

	private static final String VIEW_WITH_STATE_ID = "org.eclipse.ui.tests.session.ViewWithState";

	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.ArbitraryPropertiesViewTest");
		ts.addTest(new ArbitraryPropertiesViewTest("test01ActivateView"));
		ts.addTest(new ArbitraryPropertiesViewTest("test02SecondOpening"));
		ts.addTest(new ArbitraryPropertiesViewTest("test03PartInstantiation"));
		return ts;
	}

	public ArbitraryPropertiesViewTest(String testName) {
		super(testName);
	}

	/**
	 * This is the first part of a 3 part tests. First instantiate a view and
	 * set a state.
	 */
	public void test01ActivateView() throws Throwable {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		IViewPart v = page.showView(VIEW_WITH_STATE_ID);

		// put another view in front of our view
		page.showView(PROBLEM_VIEW_ID);

		IWorkbenchPart3 wp = (IWorkbenchPart3) v;
		wp.setPartProperty(USER_PROP, "pwebster");
	}

	/**
	 * The second session doesn't activate the view, so it should not be
	 * instantiated.
	 */
	public void test02SecondOpening() throws Throwable {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		IViewReference[] views = page.getViewReferences();
		for (IViewReference ref : views) {
			if (ref.getId().equals(VIEW_WITH_STATE_ID)) {
				assertNull("The view should not be instantiated", ref
						.getPart(false));
				assertEquals("pwebster", ref.getPartProperty(USER_PROP));
			}
		}
	}

	static class PropListener implements IPropertyChangeListener {
		public int count = 0;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			count++;
		}
	}

	/**
	 * Activate the view and it's state should re-appear.
	 */
	public void test03PartInstantiation() throws Throwable {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
				.getActivePage();

		IViewReference ref = page.findViewReference(VIEW_WITH_STATE_ID);
		assertEquals("pwebster", ref.getPartProperty(USER_PROP));
		PropListener listener = new PropListener();
		ref.addPartPropertyListener(listener);

		IViewPart v = null;
		try {
			v = page.showView(VIEW_WITH_STATE_ID);
			IWorkbenchPart3 wp = (IWorkbenchPart3) v;
			assertEquals("pwebster", wp.getPartProperty(USER_PROP));
			assertEquals(0, listener.count);
		} finally {
			ref.removePartPropertyListener(listener);
		}
		// the state should not be saved between a close and
		// an open in the same session
		page.hideView(v);
		v = page.showView(VIEW_WITH_STATE_ID);
		IWorkbenchPart3 wp = (IWorkbenchPart3) v;
		assertNull(wp.getPartProperty(USER_PROP));
	}
}
