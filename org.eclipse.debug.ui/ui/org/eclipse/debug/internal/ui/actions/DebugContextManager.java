/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.actions;

import java.util.Collections;
import java.util.List;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.EnabledSubmission;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;

/**
 * Manages the debug context in response to active debug sessions.
 * When something is being debugged, the context is activated. 
 * When all debugging stops, the context is deactivated.
 * 
 * @since 3.0
 */
public class DebugContextManager implements ILaunchesListener2 {
	
	
	public static final String DEBUG_CONTEXT = "org.eclipse.debug.ui.debugging"; //$NON-NLS-1$
	
	// whether the debug context is currently on
	private boolean fDebugging = false;
	
	// debug context submission
	private List fDebugSubmission = Collections.singletonList(new EnabledSubmission((String) null, (Shell)null, (IWorkbenchPartSite)null, DEBUG_CONTEXT));

	// singleton
	private static DebugContextManager contextServiceManager;
	
	public static DebugContextManager getDefault() {
		if (contextServiceManager == null) {
			contextServiceManager = new DebugContextManager();
		}
		return contextServiceManager;
	}
	
	private DebugContextManager() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}
	
	/**
	 * Returns whether the debug scope is currently on.
	 * 
	 * @return whether the debug scope is currently on
	 */
	public boolean isDebugging() {
		return fDebugging;
	}
		
	/**
	 * Sets whether the debug scope is currently on.
	 * 
	 * @param debugging whether the debug scope is currently on
	 */
	private void setDebugging(boolean debugging) {
		if (debugging != fDebugging) {
			fDebugging = debugging;
			DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchContextSupport contextSupport = PlatformUI.getWorkbench().getContextSupport();
					if (fDebugging) {
						contextSupport.addEnabledSubmissions(fDebugSubmission);
					} else {
						contextSupport.removeEnabledSubmissions(fDebugSubmission);
					}
				}
			});

		}
	}
	
	public synchronized void launchesAdded(ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			if (launches[i].getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
				setDebugging(true);
				return;
			}
		}
	}
		
	public void launchesRemoved(ILaunch[] launches) {
	}	
	
	public void launchesChanged(ILaunch[] launches) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener2#launchTerminated(org.eclipse.debug.core.ILaunch)
	 */
	public synchronized void launchesTerminated(ILaunch[] launches) {
		boolean debugLaunchTerminated = false;
		for (int i = 0; i < launches.length; i++) {
			if (launches[i].getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
				debugLaunchTerminated= true;
				break;
			}
		}
		if (debugLaunchTerminated) {
			// if nothing left in debug mode, turn debugging off
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunch[] remainingLaunches = manager.getLaunches();
			for (int i = 0; i < remainingLaunches.length; i++) {
				ILaunch l = remainingLaunches[i];
				if (ILaunchManager.DEBUG_MODE.equals(l.getLaunchMode()) && !l.isTerminated()) {
					// still debugging
					return;
				}
			}
			setDebugging(false);
		}
	}

}
