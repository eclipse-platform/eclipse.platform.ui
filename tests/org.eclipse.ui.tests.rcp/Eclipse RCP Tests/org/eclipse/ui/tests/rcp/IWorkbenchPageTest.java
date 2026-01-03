/*******************************************************************************
 * Copyright (c) 2004, 2026 IBM Corporation and others.
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
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.EmptyView;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the behaviour of various IWorkbenchPage methods under different
 * workbench configurations.
 */
public class IWorkbenchPageTest {


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

	/**
	 * Regression test for Bug 70080 [RCP] Reset Perspective does not work if no
	 * perspective toolbar shown (RCP).
	 */
	@Test
	public void test70080() {
		WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {

			@Override
			@Deprecated
			public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
				super.preWindowOpen(configurer);
				configurer.setShowPerspectiveBar(false);
			}

			@Override
			public void postStartup() {
				try {
					IWorkbenchWindow window = getWorkbenchConfigurer()
							.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					page.showView(EmptyView.ID);
					assertNotNull(page.findView(EmptyView.ID));
					page.resetPerspective();
					assertNull(page.findView(EmptyView.ID));
				} catch (PartInitException e) {
					fail(e.toString());
				}
			}
		};
		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_OK, code);
	}

}
