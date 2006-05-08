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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;

/**
 * Tests for IWorkbenchListener API and implementation.
 */
public class WorkbenchListenerTest extends TestCase {

    public WorkbenchListenerTest(String name) {
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
     * Brings the workbench up and tries to shut it down twice, the first time with a veto
     * from the IWorkbenchListener.  Tests for the correct sequence of notifications.
     */
    public void testPreAndPostShutdown() {
    	final boolean[] proceed = new boolean[1];
    	final List operations = new ArrayList();
        WorkbenchAdvisor wa = new WorkbenchAdvisorObserver(1) {
        	public void postStartup() {
        		IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
        		workbench.addWorkbenchListener(new IWorkbenchListener() {
					public boolean preShutdown(IWorkbench workbench, boolean forced) {
						operations.add(PRE_SHUTDOWN);
						return proceed[0];
					}
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
