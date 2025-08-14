/*******************************************************************************
 * Copyright (c) 2019 1C-Soft LLC.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev <pisv@1c.ru> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.session.ViewWithState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Bug543609Test {

	private static final String VIEW_WITH_STATE_ID = "org.eclipse.ui.tests.session.ViewWithState";

	private IWorkbenchPage fPage;

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void doSetUp() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		fPage = window.getActivePage();
	}

	@Test
	public void testViewWithState() throws Exception {
		ViewWithState view = (ViewWithState) fPage.showView(VIEW_WITH_STATE_ID);
		int savedState = ++view.fState;
		fPage.hideView(view);
		ViewWithState view2 = (ViewWithState) fPage.showView(VIEW_WITH_STATE_ID);
		assertNotSame(view, view2);
		assertEquals(savedState, view2.fState);
	}
}
