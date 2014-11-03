/*******************************************************************************
 * Copyright (c) 2004, 2006,2014 IBM Corporation and others.
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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActionBarConfigurerTest {


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
