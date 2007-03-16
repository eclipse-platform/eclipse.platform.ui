/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.PlatformUI;

/**
 * A common facility for parsing the <code>org.eclipse.ui/.options</code>
 * file.
 * 
 * @since 2.1
 */
public class Policy {
    public static boolean DEFAULT = false;

    public static boolean DEBUG_SWT_GRAPHICS = DEFAULT;

    public static boolean DEBUG_SWT_DEBUG = DEFAULT;

    public static boolean DEBUG_DRAG_DROP = DEFAULT;

    /**
     * Flag to log stale jobs
     */
    public static boolean DEBUG_STALE_JOBS = DEFAULT;

    /**
	 * Whether to report all events entering through the common event framework
	 * used by the commands architecture.
	 * 
	 * @see ISourceProvider
	 * @since 3.2
	 */
    public static boolean DEBUG_SOURCES = DEFAULT;

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
     * Whether to print debugging information about the execution of commands
     */
    public static boolean DEBUG_COMMANDS = DEFAULT;

    /**
     * Whether to print debugging information about the internal state of the 
     * context support within the workbench.
     */
    public static boolean DEBUG_CONTEXTS = DEFAULT;

    /**
     * Whether to print debugging information about the performance of context
     * computations.
     */
    public static boolean DEBUG_CONTEXTS_PERFORMANCE = DEFAULT;

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
     * Whether to print debugging information about the performance of handler
     * computations.
     */
    public static boolean DEBUG_HANDLERS_PERFORMANCE = DEFAULT;

    /**
     * Whether to print out verbose information about changing handlers in the
     * workbench.
     */
    public static boolean DEBUG_HANDLERS_VERBOSE = DEFAULT;
	
    /**
     * Whether to print debugging information about unexpected occurrences and
     * important state changes in the operation history.
     */
    public static boolean DEBUG_OPERATIONS = DEFAULT;

    /**
     * Whether to print out verbose information about the operation histories,
     * including all notifications sent.
     */
    public static boolean DEBUG_OPERATIONS_VERBOSE = DEFAULT;


    /**
     * Whether or not to show system jobs at all times.
     */
    public static boolean DEBUG_SHOW_ALL_JOBS = DEFAULT;
    
    /**
     * Whether or not to resolve images as they are declared.
     * 
     * @since 3.1
     */
    public static boolean DEBUG_DECLARED_IMAGES = DEFAULT;
    
    /**
     * Whether or not to print contribution-related issues.
     * 
     * @since 3.1
     */
    public static boolean DEBUG_CONTRIBUTIONS = DEFAULT;

    /**
     * Which command identifier to print handler information for.  This
     * restricts the debugging output, so a developer can focus on one command
     * at a time.
     */
    public static String DEBUG_HANDLERS_VERBOSE_COMMAND_ID = null;
    
    /**
     * Whether experimental features in the rendering of commands into menus
     * and toolbars should be enabled.  This is not guaranteed to provide a
     * working workbench.
     */
    public static boolean EXPERIMENTAL_MENU = DEFAULT;
    
    public static boolean DEBUG_MPE = DEFAULT;

    static {
        if (getDebugOption("/debug")) { //$NON-NLS-1$
            DEBUG_SWT_GRAPHICS = getDebugOption("/trace/graphics"); //$NON-NLS-1$
            DEBUG_SWT_DEBUG = getDebugOption("/debug/swtdebug"); //$NON-NLS-1$
            DEBUG_DRAG_DROP = getDebugOption("/trace/dragDrop"); //$NON-NLS-1$
            DEBUG_SOURCES = getDebugOption("/trace/sources"); //$NON-NLS-1$
            DEBUG_KEY_BINDINGS = getDebugOption("/trace/keyBindings"); //$NON-NLS-1$
            DEBUG_KEY_BINDINGS_VERBOSE = getDebugOption("/trace/keyBindings.verbose"); //$NON-NLS-1$
            DEBUG_TOOLBAR_DISPOSAL = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface/trace/toolbarDisposal")); //$NON-NLS-1$ //$NON-NLS-2$
            DEBUG_COMMANDS = getDebugOption("/trace/commands"); //$NON-NLS-1$
            DEBUG_CONTEXTS = getDebugOption("/trace/contexts"); //$NON-NLS-1$
            DEBUG_CONTEXTS_PERFORMANCE = getDebugOption("/trace/contexts.performance"); //$NON-NLS-1$
            DEBUG_CONTEXTS_VERBOSE = getDebugOption("/trace/contexts.verbose"); //$NON-NLS-1$
            DEBUG_HANDLERS = getDebugOption("/trace/handlers"); //$NON-NLS-1$
            DEBUG_HANDLERS_PERFORMANCE = getDebugOption("/trace/handlers.performance"); //$NON-NLS-1$
            DEBUG_HANDLERS_VERBOSE = getDebugOption("/trace/handlers.verbose"); //$NON-NLS-1$
            DEBUG_OPERATIONS = getDebugOption("/trace/operations"); //$NON-NLS-1$
            DEBUG_OPERATIONS_VERBOSE = getDebugOption("/trace/operations.verbose"); //$NON-NLS-1$
            DEBUG_SHOW_ALL_JOBS = getDebugOption("/debug/showAllJobs"); //$NON-NLS-1$
            DEBUG_STALE_JOBS = getDebugOption("/debug/job.stale"); //$NON-NLS-1$
            DEBUG_HANDLERS_VERBOSE_COMMAND_ID = Platform
                    .getDebugOption(PlatformUI.PLUGIN_ID
                            + "/trace/handlers.verbose.commandId"); //$NON-NLS-1$
            if ("".equals(DEBUG_HANDLERS_VERBOSE_COMMAND_ID)) { //$NON-NLS-1$
            	DEBUG_HANDLERS_VERBOSE_COMMAND_ID = null;
            }
            DEBUG_DECLARED_IMAGES = getDebugOption("/debug/declaredImages"); //$NON-NLS-1$
            DEBUG_CONTRIBUTIONS = getDebugOption("/debug/contributions"); //$NON-NLS-1$
            EXPERIMENTAL_MENU = getDebugOption("/experimental/menus"); //$NON-NLS-1$
            DEBUG_MPE = getDebugOption("/trace/multipageeditor"); //$NON-NLS-1$
        }
    }

    private static boolean getDebugOption(String option) {
        return "true".equalsIgnoreCase(Platform.getDebugOption(PlatformUI.PLUGIN_ID + option)); //$NON-NLS-1$
    }
}
