/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import java.util.HashMap;

import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;

/**
 * This class is used for monitoring performance events.  Each performance
 * event has an associated option in the org.eclipse.ui plugin's .options file
 * that specifies an maximum acceptable duration for that event.
 * 
 * @see org.eclipse.core.runtime.PerformanceStats
 */
public class UIStats {
	
	 private static HashMap operations = new HashMap();

    public static final int CREATE_PART = 0;

    public static final int CREATE_PART_CONTROL = 1;

    public static final int INIT_PART = 2;

    public static final int CREATE_PERSPECTIVE = 3;

    public static final int RESTORE_WORKBENCH = 4;

    public static final int START_WORKBENCH = 5;

    public static final int CREATE_PART_INPUT = 6;

    public static final int ACTIVATE_PART = 7;

    public static final int BRING_PART_TO_TOP = 8;

    public static final int NOTIFY_PART_LISTENERS = 9;

    public static final int SWITCH_PERSPECTIVE = 10;
    
    public static final int NOTIFY_PAGE_LISTENERS = 11;

    public static final int NOTIFY_PERSPECTIVE_LISTENERS = 12;

    public static final int UI_JOB = 13;
	
	public static final int CONTENT_TYPE_LOOKUP = 14;

    /**
     * Change this value when you add a new event constant.
     */
    public static final int LAST_VALUE = CONTENT_TYPE_LOOKUP;

    private static boolean debug[] = new boolean[LAST_VALUE+1];

    private static String[] events = new String[LAST_VALUE+1];

    static {
        events[CREATE_PART] = PlatformUI.PLUGIN_ID + "/perf/part.create"; //$NON-NLS-1$
        events[CREATE_PART_INPUT] = PlatformUI.PLUGIN_ID + "/perf/part.input"; //$NON-NLS-1$
        events[CREATE_PART_CONTROL] = PlatformUI.PLUGIN_ID + "/perf/part.control"; //$NON-NLS-1$
        events[INIT_PART] = PlatformUI.PLUGIN_ID + "/perf/part.init"; //$NON-NLS-1$
        events[CREATE_PERSPECTIVE] = PlatformUI.PLUGIN_ID + "/perf/perspective.create"; //$NON-NLS-1$
        events[SWITCH_PERSPECTIVE] = PlatformUI.PLUGIN_ID + "/perf/perspective.switch"; //$NON-NLS-1$
        events[RESTORE_WORKBENCH] = PlatformUI.PLUGIN_ID + "/perf/workbench.restore"; //$NON-NLS-1$
        events[START_WORKBENCH] = PlatformUI.PLUGIN_ID + "/perf/workbench.start"; //$NON-NLS-1$
        events[ACTIVATE_PART] = PlatformUI.PLUGIN_ID + "/perf/part.activate"; //$NON-NLS-1$
        events[BRING_PART_TO_TOP] = PlatformUI.PLUGIN_ID + "/perf/part.activate"; //$NON-NLS-1$
        events[NOTIFY_PART_LISTENERS] = PlatformUI.PLUGIN_ID + "/perf/part.listeners"; //$NON-NLS-1$
        events[NOTIFY_PAGE_LISTENERS] = PlatformUI.PLUGIN_ID + "/perf/page.listeners"; //$NON-NLS-1$
        events[NOTIFY_PERSPECTIVE_LISTENERS] = PlatformUI.PLUGIN_ID + "/perf/perspective.listeners"; //$NON-NLS-1$
        events[UI_JOB] = PlatformUI.PLUGIN_ID + "/perf/uijob"; //$NON-NLS-1$
		events[CONTENT_TYPE_LOOKUP] = PlatformUI.PLUGIN_ID + "/perf/contentTypes"; //$NON-NLS-1$

        for (int i = 0; i <= LAST_VALUE; i++) {
        	//don't log any performance events if the general performance stats is disabled
        	if (events[i] != null && PerformanceStats.ENABLED) {
				debug[i] = PerformanceStats.isEnabled(events[i]);
			}
        }
    }

    /**
     * Returns whether tracing of the given debug event is turned on.
     * 
     * @param event The event id
     * @return <code>true</code> if tracing of this event is turned on,
     * and <code>false</code> otherwise.
     */
    public static boolean isDebugging(int event) {
        return debug[event];
    }
    
    /**
     * Indicates the start of a performance event
     * 
     * @param event The event id
     * @param label The event label
     */
    public static void start(int event, String label) {
        if (debug[event]) {
			operations.put(event + label, new Long(System.currentTimeMillis()));
		}
    }

    /**
     * Indicates the end of a performance operation
     * 
     * @param event The event id
     * @param blame An object that is responsible for the event that occurred,
     * or that uniquely describes the event that occurred
     * @param label The event label
     */
   	public static void end(int event, Object blame, String label) {
        if (debug[event]) {
            Long startTime = (Long) operations.remove(event + label);
            if (startTime == null) {
				return;
			}
            final long elapsed = System.currentTimeMillis() - startTime.longValue();
//			System.out.println("Time - " + //$NON-NLS-1$
//                    elapsed + events[event] + label);
            PerformanceStats.getStats(events[event], blame).addRun(elapsed, label);
        }
    }
   	
   	/**
   	 * Special hook to signal that application startup is complete and the event
   	 * loop has started running.
   	 */
   	public static void startupComplete() {
   		// We use a runtime debug option here for backwards compatibility (bug 96672)
		// Note that this value is only relevant if the workspace chooser is not used.
   		String option = Platform.getDebugOption(Platform.PI_RUNTIME + "/debug"); //$NON-NLS-1$
		if (option == null || !"true".equalsIgnoreCase(option)) { //$NON-NLS-1$
			return;
		}
		String startString = System.getProperty("eclipse.startTime"); //$NON-NLS-1$
		if (startString == null) {
			return;
		}
		try {
			long start = Long.parseLong(startString);
			long end = System.currentTimeMillis();
			System.out.println("Startup complete: " + (end - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NumberFormatException e) {
			//this is just debugging code -- ok to swallow exception
		}
   	}
}
