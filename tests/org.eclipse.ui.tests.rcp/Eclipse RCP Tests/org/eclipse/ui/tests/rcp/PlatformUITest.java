/*******************************************************************************
 * Copyright (c) 2004, 2011, 2014 IBM Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;
import org.eclipse.ui.tests.rcp.util.WorkbenchAdvisorObserver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class PlatformUITest {


	/** Make sure workbench is not returned before it is running. */
	@Test
	public void testEarlyGetWorkbench() {
		assertFalse(PlatformUI.isWorkbenchRunning());
		assertThrows(IllegalStateException.class, () -> PlatformUI.getWorkbench());
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

		assertAll(//
				() -> assertFalse(RCPTestWorkbenchAdvisor.asyncDuringStartup,
						"Async run during startup.  See RCPTestWorkbenchAdvisor.preStartup()"),
				// the following four asserts test the various combinations of Thread +
				// DisplayAccess + a/sync exec. Anything without a call to DisplayAccess
				// should be deferred until after startup.
				() -> assertTrue(RCPTestWorkbenchAdvisor.syncWithDisplayAccess,
						"Sync from qualified thread did not run during startup.  See RCPTestWorkbenchAdvisor.preStartup()"),
				() -> assertTrue(RCPTestWorkbenchAdvisor.asyncWithDisplayAccess,
						"Async from qualified thread did not run during startup.  See RCPTestWorkbenchAdvisor.preStartup()"),
				() -> assertFalse(RCPTestWorkbenchAdvisor.syncWithoutDisplayAccess,
						"Sync from un-qualified thread ran during startup.  See RCPTestWorkbenchAdvisor.preStartup()"),
				() -> assertFalse(RCPTestWorkbenchAdvisor.asyncWithoutDisplayAccess,
						"Async from un-qualified thread ran during startup.  See RCPTestWorkbenchAdvisor.preStartup()"),
				() -> assertFalse(RCPTestWorkbenchAdvisor.displayAccessInUIThreadAllowed,
						"DisplayAccess.accessDisplayDuringStartup() in UI thread did not result in exception."));
	}

	/**
	 * Tests that, if an exception occurs on startup, the workbench returns RETURN_UNSTARTABLE
	 * and PlatformUI.isWorkbenchRunning() returns false.
	 * Regression test for bug 82286.
	 */
	@Disabled
	@Test
	public void testCreateAndRunWorkbenchWithExceptionOnStartup() {
		final Display display = PlatformUI.createDisplay();
		assertNotNull(display);

		WorkbenchAdvisorObserver wa = new WorkbenchAdvisorObserver(1) {
			@Override
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

	@Override
	public void eventLoopIdle(Display display) {
		super.eventLoopIdle(display);

		if (checkComplete) {
			return;
		}

		assertNotNull(PlatformUI.getWorkbench());
		checkComplete = true;
	}
}
