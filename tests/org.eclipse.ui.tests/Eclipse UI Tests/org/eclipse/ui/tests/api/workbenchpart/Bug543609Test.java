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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.session.ViewWithState;

public class Bug543609Test extends UITestCase {

	private static final String VIEW_WITH_STATE_ID = "org.eclipse.ui.tests.session.ViewWithState";

	private IWorkbenchPage fPage;

	public Bug543609Test(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		IWorkbenchWindow window = openTestWindow();
		fPage = window.getActivePage();
	}

	public void testViewWithState() throws Exception {
		ViewWithState view = (ViewWithState) fPage.showView(VIEW_WITH_STATE_ID);
		int savedState = ++view.fState;
		fPage.hideView(view);
		ViewWithState view2 = (ViewWithState) fPage.showView(VIEW_WITH_STATE_ID);
		assertNotSame(view, view2);
		assertEquals(savedState, view2.fState);
	}
}
