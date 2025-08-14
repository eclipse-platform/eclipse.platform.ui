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

package org.eclipse.ui.tests.api.workbenchpart;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.4
 */
@RunWith(JUnit4.class)
public class LifecycleViewTest extends UITestCase {

	public LifecycleViewTest() {
		super(LifecycleViewTest.class.getSimpleName());
	}

	@Test
	public void testLifecycle() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewPart v = page.showView(LifecycleView.ID);
		assertTrue(v instanceof LifecycleView);
		LifecycleView view = (LifecycleView) v;
		processEvents();

		page.hideView(v);

		processEvents();

		assertTrue(view.callPartDispose);
		assertTrue(view.callWidgetDispose);
		assertFalse(view.callSiteDispose);
	}
}
