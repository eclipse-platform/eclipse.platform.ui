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
package org.eclipse.team.internal.ccvs.core;


import java.io.PrintStream;
import java.lang.reflect.Field;

import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;

public class Policy {
	public static PrintStream recorder;
	
	//debug constants
	public static boolean DEBUG_METAFILE_CHANGES = false;
	public static boolean DEBUG_CVS_PROTOCOL = false;
	public static boolean DEBUG_THREADING = false;
	public static boolean DEBUG_DIRTY_CACHING = false;
	public static boolean DEBUG_SYNC_CHANGE_EVENTS = false;

	static {
		//init debug options
		if (CVSProviderPlugin.getPlugin().isDebugging()) {
			DEBUG_METAFILE_CHANGES = "true".equalsIgnoreCase(Platform.getDebugOption(CVSProviderPlugin.ID + "/metafiles"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_CVS_PROTOCOL = "true".equalsIgnoreCase(Platform.getDebugOption(CVSProviderPlugin.ID + "/cvsprotocol"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_THREADING = "true".equalsIgnoreCase(Platform.getDebugOption(CVSProviderPlugin.ID + "/threading"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_DIRTY_CACHING = "true".equalsIgnoreCase(Platform.getDebugOption(CVSProviderPlugin.ID + "/dirtycaching"));//$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_SYNC_CHANGE_EVENTS = "true".equalsIgnoreCase(Platform.getDebugOption(CVSProviderPlugin.ID + "/syncchangeevents"));//$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Progress monitor helpers
	 */
	public static void checkCanceled(IProgressMonitor monitor) {
		if (monitor.isCanceled())
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
	
	public static boolean isDebugProtocol() {
	    return DEBUG_CVS_PROTOCOL || recorder != null;
	}
	
	public static void printProtocolLine(String line) {
	    printProtocol(line, true);
	}

    public static void printProtocol(String string, boolean newLine) {
        if (DEBUG_CVS_PROTOCOL) {
	        System.out.print(string);
	        if (newLine) {
	            System.out.println();
	        }
        }
        if (recorder != null) {
            recorder.print(string);
            if (newLine) {
                recorder.println();
            }
        }
    }
    
    public static String getMessage(String key) {
        try {
            Field f = CVSMessages.class.getDeclaredField(key);
            Object o = f.get(null);
            if (o instanceof String)
                return (String)o;
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }
}
