/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import org.eclipse.core.runtime.*;

public class Policy {

	//debug constants
	public static boolean DEBUG_STREAMS = false;
	public static boolean DEBUG_REFRESH_JOB = true;
	public static boolean DEBUG_BACKGROUND_EVENTS = false;
	public static boolean DEBUG_THREADING = false;

	static {
		//init debug options
		if (TeamPlugin.getPlugin().isDebugging()) {
			DEBUG_STREAMS = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/streams"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_REFRESH_JOB = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/refreshjob"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_BACKGROUND_EVENTS = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/backgroundevents"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_THREADING = "true".equalsIgnoreCase(Platform.getDebugOption(TeamPlugin.ID + "/threading"));//$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	/**
	 * Progress monitor helpers
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
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
}
