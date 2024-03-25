/*******************************************************************************
 * Copyright (c) 2004, 2006,2014 IBM Corporation and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ActionBarConfigurerTest {


	private Display display = null;

	@BeforeEach
	public void setUp() throws Exception {
		assertNull(display);
		display = PlatformUI.createDisplay();
		assertNotNull(display);
	}

	@AfterEach
	public void tearDown() throws Exception {
		assertNotNull(display);
		display.dispose();
		assertTrue(display.isDisposed());
	}

	@Test
	public void testDefaults() {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

			@Override
			public void fillActionBars(IWorkbenchWindow window,
					IActionBarConfigurer actionBarConfig, int flags) {
				super.fillActionBars(window, actionBarConfig, flags);

				assertNotNull(actionBarConfig.getMenuManager());
				assertNotNull(actionBarConfig.getStatusLineManager());
				assertNotNull(actionBarConfig.getCoolBarManager());
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}
}
