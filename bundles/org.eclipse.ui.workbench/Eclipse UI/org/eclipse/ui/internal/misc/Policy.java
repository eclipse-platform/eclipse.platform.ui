/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;

public class Policy {
    public static boolean DEFAULT = false;

    // @issue this is an IDE specific debug option
    public static boolean DEBUG_OPEN_ERROR_DIALOG = DEFAULT;

    public static boolean DEBUG_SWT_GRAPHICS = DEFAULT;

    public static boolean DEBUG_SWT_DEBUG = DEFAULT;

    public static boolean DEBUG_PART_CREATE = DEFAULT;

    public static boolean DEBUG_PART_ACTIVATE = DEFAULT;

    public static boolean DEBUG_PART_LISTENERS = DEFAULT;

    public static boolean DEBUG_PAGE_LISTENERS = DEFAULT;
    
    public static boolean DEBUG_PERSPECTIVE_LISTENERS = DEFAULT;
    
    public static boolean DEBUG_PERSPECTIVE = DEFAULT;

    public static boolean DEBUG_RESTORE_WORKBENCH = DEFAULT;

    public static boolean DEBUG_START_WORKBENCH = DEFAULT;

    public static boolean DEBUG_DRAG_DROP = DEFAULT;

    /**
     * Flag to log stale jobs
     */
    public static boolean DEBUG_STALE_JOBS = DEFAULT;

    public static boolean DEBUG_INCLUDE_TIMINGS = DEFAULT;
    
    /**
     * Whether to print information about key bindings that are successfully
     * recognized within the system (as the keys are pressed).
     */
    public static boolean DEBUG_KEY_BINDINGS = DEFAULT;

    /**
     * Whether to print information about every key seen by the system.
     */
    public static boolean DEBUG_KEY_BINDINGS_VERBOSE = DEFAULT;

    /**
     * Whether to print extra information about error conditions dealing with
     * cool bars in the workbench, and their disposal.
     */
    public static boolean DEBUG_TOOLBAR_DISPOSAL = DEFAULT;

    /**
     * Whether to print debugging information about the internal state of the 
     * context support within the workbench.
     */
    public static boolean DEBUG_CONTEXTS = DEFAULT;

    /**
     * Whether to print even more debugging information about the internal state
     * of the context support within the workbench.
     */
    public static boolean DEBUG_CONTEXTS_VERBOSE = DEFAULT;

    /**
     * Whether to print debugging information about the internal state of the
     * command support (in relation to handlers) within the workbench.
     */
    public static boolean DEBUG_HANDLERS = DEFAULT;

    /**
     * Whether to print out verbose information about changing handlers in the
     * workbench.
     */
    public static boolean DEBUG_HANDLERS_VERBOSE = DEFAULT;

    /**
     * Whether to print out a warning about UI jobs that take
     * longer than 100ms.
     */
    public static boolean DEBUG_LONG_UI_WARNING = DEFAULT;
    
    /**
     * Whether or not to show system jobs at all times.
     */
    public static boolean DEBUG_SHOW_SYSTEM_JOBS = DEFAULT;
    

    /**
     * Which command identifier to print handler information for.  This
     * restricts the debugging output, so a developer can focus on one command
     * at a time.
     */
    public static String DEBUG_HANDLERS_VERBOSE_COMMAND_ID = null;

    static {
        if (getDebugOption("/debug")) { //$NON-NLS-1$
            DEBUG_OPEN_ERROR_DIALOG = getDebugOption("/debug/internalerror/openDialog"); //$NON-NLS-1$
            DEBUG_SWT_GRAPHICS = getDebugOption("/trace/graphics"); //$NON-NLS-1$
            DEBUG_SWT_DEBUG = getDebugOption("/debug/swtdebug"); //$NON-NLS-1$
            DEBUG_PART_CREATE = getDebugOption("/trace/part.create"); //$NON-NLS-1$
            DEBUG_PERSPECTIVE = getDebugOption("/trace/perspective"); //$NON-NLS-1$
            DEBUG_RESTORE_WORKBENCH = getDebugOption("/trace/workbench.restore"); //$NON-NLS-1$
            DEBUG_START_WORKBENCH = getDebugOption("/trace/workbench.start"); //$NON-NLS-1$
            DEBUG_PART_ACTIVATE = getDebugOption("/trace/part.activate"); //$NON-NLS-1$
            DEBUG_PART_LISTENERS = getDebugOption("/trace/part.listeners"); //$NON-NLS-1$
            DEBUG_PAGE_LISTENERS = getDebugOption("/trace/page.listeners"); //$NON-NLS-1$
            DEBUG_PERSPECTIVE_LISTENERS = getDebugOption("/trace/perspective.listeners"); //$NON-NLS-1$
            DEBUG_DRAG_DROP = getDebugOption("/trace/dragDrop"); //$NON-NLS-1$
            DEBUG_KEY_BINDINGS = getDebugOption("/trace/keyBindings"); //$NON-NLS-1$
            DEBUG_KEY_BINDINGS_VERBOSE = getDebugOption("/trace/keyBindings.verbose"); //$NON-NLS-1$
            DEBUG_TOOLBAR_DISPOSAL = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface/trace/toolbarDisposal")); //$NON-NLS-1$ //$NON-NLS-2$
            DEBUG_CONTEXTS = getDebugOption("/trace/contexts"); //$NON-NLS-1$
            DEBUG_CONTEXTS_VERBOSE = getDebugOption("/trace/contexts.verbose"); //$NON-NLS-1$
            DEBUG_HANDLERS = getDebugOption("/trace/handlers"); //$NON-NLS-1$
            DEBUG_HANDLERS_VERBOSE = getDebugOption("/trace/handlers.verbose"); //$NON-NLS-1$
            DEBUG_LONG_UI_WARNING = getDebugOption("/debug/uijob.longwarning"); //$NON-NLS-1$
            DEBUG_SHOW_SYSTEM_JOBS = getDebugOption("/debug/showSystemJobs"); //$NON-NLS-1$
            DEBUG_STALE_JOBS = getDebugOption("/debug/job.stale"); //$NON-NLS-1$
            DEBUG_HANDLERS_VERBOSE_COMMAND_ID = Platform
                    .getDebugOption(PlatformUI.PLUGIN_ID
                            + "/trace/handlers.verbose.commandId"); //$NON-NLS-1$
            DEBUG_INCLUDE_TIMINGS = getDebugOption("/debug/include.timings"); //$NON-NLS-1$
        }
    }

    private static boolean getDebugOption(String option) {
        return "true".equalsIgnoreCase(Platform.getDebugOption(PlatformUI.PLUGIN_ID + option)); //$NON-NLS-1$
    }
}