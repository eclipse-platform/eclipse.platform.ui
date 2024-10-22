/******************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ShowViewMenu;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Before;
import org.junit.Test;

public class ShowViewMenuTest {

	private IWorkbenchWindow workbenchWindow;

	@Before
	public void doSetUp() throws Exception {
		// open a workbench window with the empty perspective, since it defines
		// no show view shortcuts, it is suitable for the two single show view
		// action tests
		workbenchWindow = UITestCase.openTestWindow();
	}

	/***********************************
	 * Tests for Bug 56368 starts here *
	 ***********************************/

	@Test
	public void testMenuOnlyHasShowViewAction() {
		Menu swtMenu = new Menu(workbenchWindow.getShell());
		ShowViewMenu showViewMenu = new ShowViewMenu(workbenchWindow, "id"); //$NON-NLS-1$
		showViewMenu.fill(swtMenu, 0);

		// as the separator is not shown if there is only the 'Show View...'
		// action, the item count should simply be one
		assertEquals("Only the 'Other...' action should be available", 1, swtMenu.getItemCount());
	}

	@Test
	public void testFastViewMenuVariantOnlyHasShowViewAction() {
		Menu swtMenu = new Menu(workbenchWindow.getShell());
		ShowViewMenu showViewMenu = new ShowViewMenu(workbenchWindow,
				"id", true); //$NON-NLS-1$
		showViewMenu.fill(swtMenu, 0);

		// as the separator is not shown if there is only the 'Show View...'
		// action, the item count should simply be one
		assertEquals("Only the 'Other...' action should be available", 1, swtMenu.getItemCount());
	}

	/*********************************
	 * Tests for Bug 56368 ends here *
	 *********************************/

}
