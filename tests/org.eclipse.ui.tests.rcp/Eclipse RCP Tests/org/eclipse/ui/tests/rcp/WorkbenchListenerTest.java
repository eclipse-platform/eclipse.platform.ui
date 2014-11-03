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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for IWorkbenchListener API and implementation.
 */
public class WorkbenchListenerTest {


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
     * Brings the workbench up and tries to shut it down twice, the first time with a veto
     * from the IWorkbenchListener.  Tests for the correct sequence of notifications.
     */
	@Test
	public void testPreAndPostShutdown() {
    	final boolean[] proceed = new boolean[1];
		final List<String> operations = new ArrayList<String>();
        WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {
        	@Override
			public void postStartup() {
        		IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        		workbench.addWorkbenchListener(new IWorkbenchListener() {
					@Override
					public boolean preShutdown(IWorkbench workbench, boolean forced) {
						operations.add(PRE_SHUTDOWN);
						return proceed[0];
					}
					@Override
					public void postShutdown(IWorkbench workbench) {
						operations.add(POST_SHUTDOWN);
					}
				});
        		proceed[0] = false;
        		assertEquals(false, workbench.close());
        		proceed[0] = true;
        		assertEquals(true, workbench.close());
        	}
        };

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);

        assertEquals(3, operations.size());
        assertEquals(WorkbenchAdvisorObserver.PRE_SHUTDOWN, operations.get(0));
        assertEquals(WorkbenchAdvisorObserver.PRE_SHUTDOWN, operations.get(1));
        assertEquals(WorkbenchAdvisorObserver.POST_SHUTDOWN, operations.get(2));
    }


}
