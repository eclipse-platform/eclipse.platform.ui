/*******************************************************************************
 * Copyright (c) 2004, 2006, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 436344
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.EmptyView;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the behaviour of various IWorkbenchPage methods under different
 * workbench configurations.
 */
public class IWorkbenchPageTest {


    private Display display = null;

	@Before
	public void setUp() throws Exception {

        assertNull(display);
        display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

	@After
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

            public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
                super.preWindowOpen(configurer);
                configurer.setShowPerspectiveBar(false);
            }

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
