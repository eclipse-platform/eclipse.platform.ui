/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.EmptyView;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

/**
 * Tests the behaviour of various IWorkbenchPage methods under different
 * workbench configurations.
 */
public class IWorkbenchPageTest extends TestCase {

    public IWorkbenchPageTest(String name) {
        super(name);
    }

    private Display display = null;

    protected void setUp() throws Exception {
        super.setUp();

        assertNull(display);
        display = PlatformUI.createDisplay();
        assertNotNull(display);
    }

    protected void tearDown() throws Exception {
        assertNotNull(display);
        display.dispose();
        assertTrue(display.isDisposed());

        super.tearDown();
    }

    /**
     * Regression test for Bug 70080 [RCP] Reset Perspective does not work if no
     * perspective toolbar shown (RCP).
     */
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
