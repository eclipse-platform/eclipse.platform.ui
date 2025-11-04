/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.DisplayAccess;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * This advisor adds the ability to exit the workbench after it has started up.
 * This is done with a call to close from within the advisor's event idle loop.
 * The number of times the idle is called before exiting can be configured. Test
 * cases should subclass this advisor and add their own callback methods if
 * needed.
 *
 * @since 3.1
 */
public class RCPTestWorkbenchAdvisor extends WorkbenchAdvisor {

	public static Boolean asyncDuringStartup = null;

	// the following fields are set by the threads that attempt sync/asyncs
	// during startup.
	public static volatile Boolean syncWithDisplayAccess = null;
	public static volatile Boolean asyncWithDisplayAccess = null;
	public static volatile Boolean syncWithoutDisplayAccess = null;
	public static volatile Boolean asyncWithoutDisplayAccess = null;

	private static boolean started = false;

	// Use a Phaser to ensure async operations are scheduled before postStartup
	// Each thread registers itself and arrives when the operation is scheduled
	private static final Phaser asyncPhaser = new Phaser(1); // 1 for the main thread

	public static boolean isSTARTED() {
		synchronized (RCPTestWorkbenchAdvisor.class) {
			return started;
		}
	}

	/** Default value of -1 causes the option to be ignored. */
	private int idleBeforeExit = -1;

	private boolean windowlessApp = false;

	/**
	 * Traps whether or not calls to displayAccess in the UI thread resulted in
	 * an exception. Should be false.
	 */
	public static boolean displayAccessInUIThreadAllowed;

	public RCPTestWorkbenchAdvisor() {
		// default value means the advisor will not trigger the workbench to
		// close
		this.idleBeforeExit = -1;
	}

	public RCPTestWorkbenchAdvisor(int idleBeforeExit) {
		this.idleBeforeExit = idleBeforeExit;
	}

	/**
	 * Enables the RCP application to runwithout a workbench window
	 */
	public RCPTestWorkbenchAdvisor(boolean windowlessApp) {
		this.windowlessApp = windowlessApp;
	}

	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);

		// The RCP tests are currently run in the context of the Platform
		// product, which specifies
		// the Resource perspective as the default, and also reports progress on
		// startup.
		// We don't want either in effect when running the RCP tests.
		// Also disable intro.
		IPreferenceStore prefs = PlatformUI.getPreferenceStore();
		prefs
				.setValue(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID,
						"");
		prefs.setValue(IWorkbenchPreferenceConstants.SHOW_PROGRESS_ON_STARTUP,
				false);
		prefs.setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, false);

		if(windowlessApp) {
			configurer.setSaveAndRestore(true);
			configurer.setExitOnLastWindowClose(false);
		}

	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return EmptyPerspective.PERSP_ID;
	}

	@Override
	public void eventLoopIdle(final Display display) {
		// Bug 107369: RCP test suite hangs on GTK
		if (idleBeforeExit != -1 && --idleBeforeExit <= 0)
			PlatformUI.getWorkbench().close();

		if (idleBeforeExit == -1)
			return;
	}

	@Override
	public void preStartup() {
		super.preStartup();
		final Display display = Display.getCurrent();

		if (display != null) {
			display.asyncExec(() -> {
				asyncDuringStartup = !isSTARTED();
			});
		}

		// start a bunch of threads that are going to do a/sync execs. For some
		// of them, call DisplayAccess.accessDisplayDuringStartup. For others,
		// dont. Those that call this method should have their runnables invoked
		// prior to the method isSTARTED returning true.

		setupAsyncDisplayThread(true, display);
		setupSyncDisplayThread(true, display);
		setupAsyncDisplayThread(false, display);
		setupSyncDisplayThread(false, display);

		try {
			DisplayAccess.accessDisplayDuringStartup();
			displayAccessInUIThreadAllowed = true;
		}
		catch (IllegalStateException e) {
			displayAccessInUIThreadAllowed = false;
		}
	}

	private void setupSyncDisplayThread(final boolean callDisplayAccess, final Display display) {
		// Register this thread with the phaser
		asyncPhaser.register();
		Thread syncThread = new Thread() {
			@Override
			public void run() {
				if (callDisplayAccess)
					DisplayAccess.accessDisplayDuringStartup();
				try {
					display.syncExec(() -> {
						if (callDisplayAccess) {
							syncWithDisplayAccess = !isSTARTED();
						} else {
							syncWithoutDisplayAccess = !isSTARTED();
						}
					});
				} catch (SWTException e) {
					// this can happen because we shut down the workbench just
					// as soon as we're initialized - ie: when we're trying to
					// run this runnable in the deferred case.
				} finally {
					// Signal that this operation has completed
					asyncPhaser.arriveAndDeregister();
				}
			}
		};
		syncThread.setDaemon(true);
		syncThread.start();
	}

	private void setupAsyncDisplayThread(final boolean callDisplayAccess, final Display display) {
		// Register this thread with the phaser
		asyncPhaser.register();
		Thread asyncThread = new Thread() {
			@Override
			public void run() {
				if (callDisplayAccess)
					DisplayAccess.accessDisplayDuringStartup();
				try {
					display.asyncExec(() -> {
						if (callDisplayAccess) {
							asyncWithDisplayAccess = !isSTARTED();
						} else {
							asyncWithoutDisplayAccess = !isSTARTED();
						}
					});
				} finally {
					// Signal that this operation has been scheduled (not necessarily executed yet)
					// This avoids deadlock since we're not waiting for execution on the UI thread
					asyncPhaser.arriveAndDeregister();
				}
			}
		};
		asyncThread.setDaemon(true);
		asyncThread.start();
	}

	@Override
	public void postStartup() {
		super.postStartup();

		// Wait for all async/sync operations to be scheduled (not blocking UI thread)
		try {
			// Wait up to 5 seconds for all operations to be scheduled
			// The main thread arrives and deregisters, waiting for all other registered threads
			asyncPhaser.awaitAdvanceInterruptibly(asyncPhaser.arrive(), 5, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			// Log warning but don't throw - we need to mark as started to avoid breaking subsequent tests
			System.err.println("WARNING: Not all async/sync operations were scheduled within timeout");
			e.printStackTrace();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.err.println("WARNING: Interrupted while waiting for async/sync operations");
		}

		// Pump the event loop to ensure async runnables execute before marking as started
		// This prevents the original race condition where async variables might not be set yet
		// Wait until the variables that should be set during startup are actually set to TRUE
		UITestUtil.processEventsUntil(() -> Boolean.TRUE.equals(syncWithDisplayAccess) && Boolean.TRUE.equals(asyncWithDisplayAccess), 5000);
		// Process any remaining events to allow variables that should NOT be set during startup
		// to accidentally execute (to detect regression)
		UITestUtil.processEvents();

		synchronized (RCPTestWorkbenchAdvisor.class) {
			started = true;
		}
	}
}
