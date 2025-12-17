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

import java.util.concurrent.CountDownLatch;

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

	public Boolean asyncDuringStartup = null;

	// the following fields are set by the threads that attempt sync/asyncs
	// during startup.
	public volatile Boolean syncWithDisplayAccess = null;
	public volatile Boolean asyncWithDisplayAccess = null;
	public volatile Boolean syncWithoutDisplayAccess = null;
	public volatile Boolean asyncWithoutDisplayAccess = null;

	private boolean started = false;

	// CountDownLatch to wait for async/sync operations with DisplayAccess to complete
	// We need to wait for 2 operations: asyncWithDisplayAccess and syncWithDisplayAccess
	private CountDownLatch displayAccessLatch = null;

	public synchronized boolean isStarted() {
		return started;
	}

	/** Default value of -1 causes the option to be ignored. */
	private int idleBeforeExit = -1;

	private boolean windowlessApp = false;

	/**
	 * Traps whether or not calls to displayAccess in the UI thread resulted in
	 * an exception. Should be false.
	 */
	public boolean displayAccessInUIThreadAllowed;

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

		// Initialize the latch to wait for 2 operations with DisplayAccess
		displayAccessLatch = new CountDownLatch(2);

		if (display != null) {
			display.asyncExec(() -> {
				asyncDuringStartup = !isStarted();
			});
		}

		// start a bunch of threads that are going to do a/sync execs. For some
		// of them, call DisplayAccess.accessDisplayDuringStartup. For others,
		// dont. Those that call this method should have their runnables invoked
		// prior to the method isStarted returning true.

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
		Thread syncThread = new Thread() {
			@Override
			public void run() {
				if (callDisplayAccess)
					DisplayAccess.accessDisplayDuringStartup();
				try {
					display.syncExec(() -> {
						if (callDisplayAccess) {
							syncWithDisplayAccess = !isStarted();
							// Count down after the runnable executes
							if (displayAccessLatch != null) {
								displayAccessLatch.countDown();
							}
						} else {
							syncWithoutDisplayAccess = !isStarted();
						}
					});
				} catch (SWTException e) {
					// this can happen because we shut down the workbench just
					// as soon as we're initialized - ie: when we're trying to
					// run this runnable in the deferred case.
					if (callDisplayAccess && displayAccessLatch != null) {
						displayAccessLatch.countDown();
					}
				}
			}
		};
		syncThread.setDaemon(true);
		syncThread.start();
	}

	private void setupAsyncDisplayThread(final boolean callDisplayAccess, final Display display) {
		Thread asyncThread = new Thread() {
			@Override
			public void run() {
				if (callDisplayAccess)
					DisplayAccess.accessDisplayDuringStartup();
				display.asyncExec(() -> {
					if (callDisplayAccess) {
						asyncWithDisplayAccess = !isStarted();
						// Count down after the runnable executes
						if (displayAccessLatch != null) {
							displayAccessLatch.countDown();
						}
					} else {
						asyncWithoutDisplayAccess = !isStarted();
					}
				});
			}
		};
		asyncThread.setDaemon(true);
		asyncThread.start();
	}

	@Override
	public void postStartup() {
		super.postStartup();

		// Wait for async/sync operations with DisplayAccess to complete execution
		if (displayAccessLatch != null) {
			long limit = System.currentTimeMillis() + 10000;
			while (displayAccessLatch.getCount() > 0 && System.currentTimeMillis() < limit) {
				if (!Display.getCurrent().readAndDispatch()) {
					Display.getCurrent().sleep();
				}
			}
			if (displayAccessLatch.getCount() > 0) {
				System.err.println("WARNING: Timeout waiting for async/sync operations with DisplayAccess");
			}
		}

		// Now mark as started - operations with DisplayAccess should have completed
		// Operations without DisplayAccess should still be pending (deferred)
		synchronized (this) {
			started = true;
		}
	}
}