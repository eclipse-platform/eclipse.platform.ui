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

package org.eclipse.ui.tests.commands;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 *
 */
public class ActionDelegateProxyTest extends UITestCase {
	/**
	 * 
	 */
	private static final String INC_COMMAND = "org.eclipse.ui.tests.incMenuHarness";
	private static final String VIEW_ID = "org.eclipse.ui.tests.api.MenuTestHarness";

	/**
	 * @param testName
	 */
	public ActionDelegateProxyTest(String testName) {
		super(testName);
	}

	public void testViewDelegate() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IWorkbenchPage page = window.getActivePage();
		assertNull(page.findView(VIEW_ID));
		IViewPart view = page.showView(VIEW_ID);
		assertNotNull(view);
		assertTrue(view instanceof MenuContributionHarness);
		MenuContributionHarness mch = (MenuContributionHarness) view;
		assertEquals(0, mch.getCount());
		IHandlerService service = (IHandlerService) window.getService(IHandlerService.class);
		service.executeCommand(INC_COMMAND, null);
		assertEquals(1, mch.getCount());
		service.executeCommand(INC_COMMAND, null);
		assertEquals(2, mch.getCount());
		
		page.hideView(view);
		IViewPart view2 = page.showView(VIEW_ID);
		assertFalse(view==view2);
		view = view2;
		assertNotNull(view);
		assertTrue(view instanceof MenuContributionHarness);
		mch = (MenuContributionHarness) view;
		assertEquals(0, mch.getCount());
		service.executeCommand(INC_COMMAND, null);
		assertEquals(1, mch.getCount());
		service.executeCommand(INC_COMMAND, null);
		assertEquals(2, mch.getCount());
	}
}
