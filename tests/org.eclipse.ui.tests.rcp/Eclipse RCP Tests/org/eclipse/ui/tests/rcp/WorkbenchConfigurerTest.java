/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

public class WorkbenchConfigurerTest extends TestCase {

    public WorkbenchConfigurerTest(String name) {
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

    public void testDefaults() {
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {

            private IWorkbenchConfigurer configurer;

            public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                configurer = c;

                assertNotNull(c.getWorkbench());
                assertFalse(c.getSaveAndRestore());
                assertNotNull(c.getWorkbenchWindowManager());
            }

            public void postShutdown() {
                super.postShutdown();

                // *** This should be checked on all of the advisor callbacks
                //     but assume that if its still set in the last one, then it
                //     must have been set for all of them.
                assertFalse(configurer.emergencyClosing());
            }
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);
    }

    public void testEmergencyClose() {
        WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(2) {

            private IWorkbenchConfigurer configurer;

            public void initialize(IWorkbenchConfigurer c) {
                super.initialize(c);
                configurer = c;

                assertNotNull(c.getWorkbench());
                assertFalse(c.getSaveAndRestore());
                assertNotNull(c.getWorkbenchWindowManager());
            }

            // emergencyClose as soon as possible
            public void eventLoopIdle(Display disp) {
                super.eventLoopIdle(disp);
                configurer.emergencyClose();
            }

            public void postShutdown() {
                super.postShutdown();

                // *** This should be checked on all of the advisor callbacks
                //     but assume that if its still set in the last one, then it
                //     must have been set for all of them.
                assertTrue(configurer.emergencyClosing());
            }
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_EMERGENCY_CLOSE, code);
    }
}
