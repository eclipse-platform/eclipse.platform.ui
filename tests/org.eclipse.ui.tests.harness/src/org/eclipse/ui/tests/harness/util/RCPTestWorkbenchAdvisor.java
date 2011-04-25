/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

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
	 * 
	 * Enables the RCP application to runwithout a workbench window
	 * 
	 * @param runWithoutWindow
	 * 
	 */
	public RCPTestWorkbenchAdvisor(boolean windowlessApp) {
		this.windowlessApp = windowlessApp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
	 */
	public String getInitialWindowPerspectiveId() {
		return EmptyPerspective.PERSP_ID;
	}

	public void eventLoopIdle(final Display display) {
		// Bug 107369: RCP test suite hangs on GTK
		if (idleBeforeExit != -1 && --idleBeforeExit <= 0)
			PlatformUI.getWorkbench().close();

		// bug 73184: On the mac the parent eventLoopIdle will put the display
		// to sleep
		// until there are events (e.g., mouse jiggled).
// if (!Util.isCarbon)
// super.eventLoopIdle(display);

		if (idleBeforeExit == -1)
			return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
	 */
	public void preStartup() {
		super.preStartup();
		final Display display = Display.getCurrent();
		if (display != null) {
			display.asyncExec(new Runnable() {

				public void run() {
					if (isSTARTED())
						asyncDuringStartup = Boolean.FALSE;
					else
						asyncDuringStartup = Boolean.TRUE;
				}
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

	/**
	 * @param display
	 */
	private void setupSyncDisplayThread(final boolean callDisplayAccess, final Display display) {
		Thread syncThread = new Thread() {
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run() {
				if (callDisplayAccess)
					DisplayAccess.accessDisplayDuringStartup();
				try {
					display.syncExec(new Runnable() {
						public void run() {
							synchronized (RCPTestWorkbenchAdvisor.class) {
								if (callDisplayAccess)
									syncWithDisplayAccess = !isSTARTED() ? Boolean.TRUE
											: Boolean.FALSE;
								else
									syncWithoutDisplayAccess = !isSTARTED() ? Boolean.TRUE
											: Boolean.FALSE;
							}
						}
					});
				} catch (SWTException e) {
					// this can happen because we shut down the workbench just
					// as soon as we're initialized - ie: when we're trying to
					// run this runnable in the deferred case.
				}
			}
		};
		syncThread.setDaemon(true);
		syncThread.start();
	}

	/**
	 * @param display
	 */
	private void setupAsyncDisplayThread(final boolean callDisplayAccess, final Display display) {
		Thread asyncThread = new Thread() {
			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			public void run() {
				if (callDisplayAccess)
					DisplayAccess.accessDisplayDuringStartup();
				display.asyncExec(new Runnable() {
					public void run() {
						synchronized (RCPTestWorkbenchAdvisor.class) {
							if (callDisplayAccess)
								asyncWithDisplayAccess = !isSTARTED() ? Boolean.TRUE
										: Boolean.FALSE;
							else
								asyncWithoutDisplayAccess = !isSTARTED() ? Boolean.TRUE
										: Boolean.FALSE;
						}
					}});
			}
		};
		asyncThread.setDaemon(true);
		asyncThread.start();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#postStartup()
	 */
	public void postStartup() {
		super.postStartup();
		synchronized (RCPTestWorkbenchAdvisor.class) {
			started = true;
		}
	}
}
