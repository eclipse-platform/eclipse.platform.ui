/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Aleksandar Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.Test;

/**
 * Tests which main menu stays in the model of a workbench window when the
 * window is closed, and is thus persisted.
 */
public class WorkbenchWindowMainMenuTest {

	private static final String MAIN_MENU_ID = "org.eclipse.ui.tests.api.workbenchWindowMainMenu";

	@Test
	public void testApplicationModelMainMenuIsKept() {
		Workbench workbench = (Workbench) PlatformUI.getWorkbench();
		EModelService modelService = workbench.getService(EModelService.class);
		MApplication application = workbench.getApplication();

		MMenu mainMenu = modelService.createModelElement(MMenu.class);
		mainMenu.setElementId(MAIN_MENU_ID);
		MTrimmedWindow window = modelService.createModelElement(MTrimmedWindow.class);
		window.setMainMenu(mainMenu);
		IPerspectiveDescriptor descriptor = workbench.getPerspectiveRegistry()
				.findPerspectiveWithId(EmptyPerspective.PERSP_ID);

		IWorkbenchWindow workbenchWindow = null;
		try {
			workbenchWindow = workbench.openWorkbenchWindow(workbench.getDefaultPageInput(), descriptor, window, true);
			assertSame("the main menu of the application model must be used", mainMenu, window.getMainMenu());

			assertTrue("the window must close", workbenchWindow.close());
			workbenchWindow = null;
			assertSame("the main menu of the application model must be kept", mainMenu, window.getMainMenu());
		} finally {
			if (workbenchWindow != null) {
				workbenchWindow.close();
			}
			application.getChildren().remove(window);
		}
	}

	@Test
	public void testWorkbenchMainMenuIsRemoved() throws Exception {
		Workbench workbench = (Workbench) PlatformUI.getWorkbench();
		MApplication application = workbench.getApplication();

		IWorkbenchWindow workbenchWindow = workbench.openWorkbenchWindow(EmptyPerspective.PERSP_ID,
				workbench.getDefaultPageInput());
		MWindow window = ((WorkbenchWindow) workbenchWindow).getModel();
		try {
			MMenu mainMenu = window.getMainMenu();
			assertNotNull("the workbench window must create a main menu", mainMenu);
			assertEquals("the main menu must be the one of the workbench window", IWorkbenchConstants.MAIN_MENU_ID,
					mainMenu.getElementId());

			assertTrue("the window must close", workbenchWindow.close());
			workbenchWindow = null;
			assertNull("the main menu created by the workbench window must be removed", window.getMainMenu());
		} finally {
			if (workbenchWindow != null) {
				workbenchWindow.close();
			}
			application.getChildren().remove(window);
		}
	}

}
