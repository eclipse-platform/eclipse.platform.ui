/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class WorkingSetFilterActionGroupTest {

	@RegisterExtension
	public CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	private IWorkingSetManager workingSetManager;

	private IWorkingSet workingSet;

	@BeforeEach
	public void setUp() {
		workingSetManager = getWorkbench().getWorkingSetManager();
		workingSet = workingSetManager.createWorkingSet("WorkingSetFilterActionGroupTest", new IAdaptable[0]);
		workingSetManager.addWorkingSet(workingSet);
		workingSetManager.addRecentWorkingSet(workingSet);
	}

	@AfterEach
	public void tearDown() {
		workingSetManager.removeWorkingSet(workingSet);
	}

	@Test
	public void testMenuRefillReusesWorkingSetImage() {
		IWorkbenchWindow window = openTestWindow();
		WorkingSetFilterActionGroup group = new WorkingSetFilterActionGroup(window.getShell(), null);
		MenuManager menuManager = new MenuManager();
		group.fillContextMenu(menuManager);
		Menu menu = menuManager.createContextMenu(window.getShell());
		try {
			menuManager.update(false);
			MenuItem item = getWorkingSetItem(menu);
			assertNotNull(item);
			Image image = item.getImage();
			assertNotNull(image);

			// the dynamic working set items are rebuilt on every update
			menuManager.update(false);
			assertTrue(item.isDisposed());
			MenuItem refilledItem = getWorkingSetItem(menu);
			assertNotNull(refilledItem);
			assertSame(image, refilledItem.getImage());
			assertFalse(image.isDisposed());
		} finally {
			menu.dispose();
			menuManager.dispose();
			group.dispose();
		}
	}

	private MenuItem getWorkingSetItem(Menu menu) {
		for (MenuItem item : menu.getItems()) {
			if (item.getText().endsWith(" " + workingSet.getLabel())) {
				return item;
			}
		}
		return null;
	}
}
