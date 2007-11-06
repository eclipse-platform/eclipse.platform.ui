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

package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.4
 *
 */
public class LifecycleViewTest extends UITestCase {

	/**
	 * @param testName
	 */
	public LifecycleViewTest(String testName) {
		super(testName);
	}

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
