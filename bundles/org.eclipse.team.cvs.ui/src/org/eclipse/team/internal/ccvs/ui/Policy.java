/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;


import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;

public class Policy {
    
    private static String ACTION_BUNDLE = "org.eclipse.team.internal.ccvs.ui.actions.actions"; //$NON-NLS-1$
    private static ResourceBundle actionBundle = null;
    
	public static boolean DEBUG_CONSOLE_BUFFERING = false;
	public static boolean DEBUG_HISTORY = false;

	static {
		//init debug options
		DEBUG_CONSOLE_BUFFERING= "true".equalsIgnoreCase(Platform.getDebugOption(CVSUIPlugin.ID + "/consolebuffering")); //$NON-NLS-1$ //$NON-NLS-2$
		DEBUG_HISTORY= "true".equalsIgnoreCase(Platform.getDebugOption(CVSUIPlugin.ID + "/history")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Progress monitor helpers
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
			cancelOperation();
	}
	public static void cancelOperation() {
		throw new OperationCanceledException();
	}
	public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
		if (monitor == null)
			return new NullProgressMonitor();
		return monitor;
	}	
	public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new SubProgressMonitor(monitor, ticks);
	}
	
	public static IProgressMonitor infiniteSubMonitorFor(IProgressMonitor monitor, int ticks) {
		if (monitor == null)
			return new NullProgressMonitor();
		if (monitor instanceof NullProgressMonitor)
			return monitor;
		return new InfiniteSubProgressMonitor(monitor, ticks);
	}
	
	public static ResourceBundle getActionBundle() {
        ResourceBundle tmpBundle = actionBundle;
        if (tmpBundle != null)
            return tmpBundle;
        return actionBundle = ResourceBundle.getBundle(ACTION_BUNDLE);
	}
}
