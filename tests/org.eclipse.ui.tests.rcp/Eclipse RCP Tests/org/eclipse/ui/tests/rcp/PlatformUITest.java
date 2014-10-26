/*******************************************************************************
 * Copyright (c) 2004, 2011, 2014 IBM Corporation and others.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.Ignore;
import org.junit.Test;

public class PlatformUITest {


    /** Make sure workbench is not returned before it is running. */
	@Test(expected = IllegalStateException.class)
	public void testEarlyGetWorkbench() {
        assertFalse(PlatformUI.isWorkbenchRunning());
		PlatformUI.getWorkbench();
    }

	@Test
	public void testCreateDisplay() {
        Display disp = PlatformUI.createDisplay();
        assertNotNull(disp);
        assertFalse(disp.isDisposed());
        disp.dispose();
        assertTrue(disp.isDisposed());
    }

	@Test
	public void testCreateAndRunWorkbench() {
        final Display display = PlatformUI.createDisplay();
        assertNotNull(display);

        CheckForWorkbench wa = new CheckForWorkbench(2);

        int code = PlatformUI.createAndRunWorkbench(display, wa);
        assertEquals(PlatformUI.RETURN_OK, code);
        assertTrue(wa.checkComplete);
        display.dispose();
        assertTrue(display.isDisposed());

		assertEquals("Async run during startup.  See RCPTestWorkbenchAdvisor.preStartup()",
				Boolean.FALSE,
				RCPTestWorkbenchAdvisor.asyncDuringStartup);

	    // the following four asserts test the various combinations of Thread +
		// DisplayAccess + a/sync exec. Anything without a call to DisplayAccess
		// should be deferred until after startup.
		assertEquals(
				"Sync from qualified thread did not run during startup.  See RCPTestWorkbenchAdvisor.preStartup()",
				Boolean.TRUE,
				RCPTestWorkbenchAdvisor.syncWithDisplayAccess);
    	assertEquals(
				"Async from qualified thread did not run during startup.  See RCPTestWorkbenchAdvisor.preStartup()",
				Boolean.TRUE,
				RCPTestWorkbenchAdvisor.asyncWithDisplayAccess);
		assertEquals(
				"Sync from un-qualified thread ran during startup.  See RCPTestWorkbenchAdvisor.preStartup()",
				Boolean.FALSE,
				RCPTestWorkbenchAdvisor.syncWithoutDisplayAccess);
       	assertEquals(
				"Async from un-qualified thread ran during startup.  See RCPTestWorkbenchAdvisor.preStartup()",
				Boolean.FALSE,
				RCPTestWorkbenchAdvisor.asyncWithoutDisplayAccess);
       	
		assertFalse(
				"DisplayAccess.accessDisplayDuringStartup() in UI thread did not result in exception.",
				RCPTestWorkbenchAdvisor.displayAccessInUIThreadAllowed);
    }
    
    /**
     * Tests that, if an exception occurs on startup, the workbench returns RETURN_UNSTARTABLE 
     * and PlatformUI.isWorkbenchRunning() returns false. 
     * Regression test for bug 82286.
     */
	@Ignore
	@Test
	public void testCreateAndRunWorkbenchWithExceptionOnStartup() {
		final Display display = PlatformUI.createDisplay();
		assertNotNull(display);

		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			public void preStartup() {
				throw new IllegalArgumentException("Thrown deliberately by PlatformUITest");
			}
		};

		int code = PlatformUI.createAndRunWorkbench(display, wa);
		assertEquals(PlatformUI.RETURN_UNSTARTABLE, code);
		assertFalse(PlatformUI.isWorkbenchRunning());
		display.dispose();
		assertTrue(display.isDisposed());
	}
}

class CheckForWorkbench extends WorkbenchAdvisorObserver {

    public boolean checkComplete = false;

    public CheckForWorkbench(int idleBeforeExit) {
        super(idleBeforeExit);
    }

    public void eventLoopIdle(Display display) {
        super.eventLoopIdle(display);

        if (checkComplete)
            return;

		assertNotNull(PlatformUI.getWorkbench());
        checkComplete = true;
    }
}
