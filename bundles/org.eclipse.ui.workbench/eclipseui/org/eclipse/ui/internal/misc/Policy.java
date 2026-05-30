/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
package org.eclipse.ui.internal.misc;

import org.eclipse.ui.ISourceProvider;

/**
 * A common facility for parsing the <code>org.eclipse.ui/.options</code> file.
 *
 * @since 2.1
 */
public class Policy {

	public static final String DEBUG_SWT_GRAPHICS_FLAG = "/trace/graphics"; //$NON-NLS-1$
	public static final String DEBUG_SWT_DEBUG_FLAG = "/debug/swtdebug"; //$NON-NLS-1$
	public static final String DEBUG_SWT_DEBUG_GLOBAL_FLAG = "/debug/swtdebugglobal"; //$NON-NLS-1$
	public static final String DEBUG_DRAG_DROP_FLAG = "/trace/dragDrop"; //$NON-NLS-1$
	public static final String DEBUG_SOURCES_FLAG = "/trace/sources"; //$NON-NLS-1$
	public static final String DEBUG_KEY_BINDINGS_FLAG = "/trace/keyBindings"; //$NON-NLS-1$
	public static final String DEBUG_KEY_BINDINGS_VERBOSE_FLAG = "/trace/keyBindings.verbose"; //$NON-NLS-1$
	public static final String DEBUG_TOOLBAR_DISPOSAL_FLAG = "/trace/toolbarDisposal"; //$NON-NLS-1$
	public static final String DEBUG_COMMANDS_FLAG = "/trace/commands"; //$NON-NLS-1$
	public static final String DEBUG_CONTEXTS_FLAG = "/trace/contexts"; //$NON-NLS-1$
	public static final String DEBUG_CONTEXTS_PERFORMANCE_FLAG = "/trace/contexts.performance"; //$NON-NLS-1$
	public static final String DEBUG_CONTEXTS_VERBOSE_FLAG = "/trace/contexts.verbose"; //$NON-NLS-1$
	public static final String DEBUG_HANDLERS_FLAG = "/trace/handlers"; //$NON-NLS-1$
	public static final String DEBUG_HANDLERS_PERFORMANCE_FLAG = "/trace/handlers.performance"; //$NON-NLS-1$
	public static final String DEBUG_HANDLERS_VERBOSE_FLAG = "/trace/handlers.verbose"; //$NON-NLS-1$
	public static final String DEBUG_OPERATIONS_FLAG = "/trace/operations"; //$NON-NLS-1$
	public static final String DEBUG_OPERATIONS_VERBOSE_FLAG = "/trace/operations.verbose"; //$NON-NLS-1$
	public static final String DEBUG_SHOW_ALL_JOBS_FLAG = "/debug/showAllJobs"; //$NON-NLS-1$
	public static final String DEBUG_STALE_JOBS_FLAG = "/debug/job.stale"; //$NON-NLS-1$
	public static final String DEBUG_DECLARED_IMAGES_FLAG = "/debug/declaredImages"; //$NON-NLS-1$
	public static final String DEBUG_CONTRIBUTIONS_FLAG = "/debug/contributions"; //$NON-NLS-1$
	public static final String DEBUG_HANDLERS_VERBOSE_COMMAND_ID_FLAG = ""; //$NON-NLS-1$
	public static final String EXPERIMENTAL_MENU_FLAG = "/experimental/menus"; //$NON-NLS-1$
	public static final String DEBUG_MPE_FLAG = "/trace/multipageeditor"; //$NON-NLS-1$
	public static final String DEBUG_WORKING_SETS_FLAG = "/debug/workingSets"; //$NON-NLS-1$

	public static boolean DEFAULT = false;

	public static boolean DEBUG_SWT_GRAPHICS = DEFAULT;

	public static boolean DEBUG_SWT_DEBUG = DEFAULT;

	public static boolean DEBUG_SWT_DEBUG_GLOBAL = DEFAULT;

	public static boolean DEBUG_DRAG_DROP = DEFAULT;

	/**
	 * Flag to log stale jobs
	 */
	public static boolean DEBUG_STALE_JOBS = DEFAULT;

	/**
	 * Whether to report all events entering through the common event framework used
	 * by the commands architecture.
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
	 * Whether to print extra information about error conditions dealing with cool
	 * bars in the workbench, and their disposal.
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
	 * Whether to print even more debugging information about the internal state of
	 * the context support within the workbench.
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
	 * Which command identifier to print handler information for. This restricts the
	 * debugging output, so a developer can focus on one command at a time.
	 */
	public static String DEBUG_HANDLERS_VERBOSE_COMMAND_ID = null;

	/**
	 * Whether experimental features in the rendering of commands into menus and
	 * toolbars should be enabled. This is not guaranteed to provide a working
	 * workbench.
	 */
	public static boolean EXPERIMENTAL_MENU = DEFAULT;

	public static boolean DEBUG_MPE = DEFAULT;

	/**
	 * Whether or not additional working set logging will occur.
	 *
	 * @since 3.4
	 */
	public static boolean DEBUG_WORKING_SETS = DEFAULT;
}
