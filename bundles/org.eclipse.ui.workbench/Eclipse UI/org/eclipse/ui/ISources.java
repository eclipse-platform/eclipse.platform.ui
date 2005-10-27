/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui;

/**
 * <p>
 * A source is type of event change that can occur within the workbench. For
 * example, the active workbench window can change, so it is considered a
 * source. Workbench services can track changes to these sources, and thereby
 * try to resolve conflicts between a variety of possible options. This is most
 * commonly used for things like handlers and contexts.
 * </p>
 * <p>
 * This interface defines the source that are known to the workbench at
 * compile-time. These sources can be combined in a bit-wise fashion. So, for
 * example, a <code>ACTIVE_PART | ACTIVE_CONTEXT</code> source includes change
 * to both the active context and the active part.
 * </p>
 * <p>
 * The values assigned to each source indicates its relative priority. The
 * higher the value, the more priority the source is given in resolving
 * conflicts. Another way to look at this is that the higher the value, the more
 * "local" the source is to what the user is currently doing. This is similar
 * to, but distinct from the concept of components. The nesting support provided
 * by components represent only one source (<code>ACTIVE_SITE</code>) that
 * the workbench understands.
 * </p>
 * <p>
 * Note that for backward compatibility, we must reserve the lowest three bits
 * for <code>Priority</code> instances using the old
 * <code>HandlerSubmission</code> mechanism. This mechanism was used in
 * Eclipse 3.0.
 * </p>
 * <p>
 * There are unused bits. This is intentional, and is intended to allow clients
 * space to define their own priorities. The workbench will not add further
 * priorities in the future without declaring it as a breaking change. If you
 * want to define your own sources, then you must create a
 * <code>ISourceProvider</code> and register it with a workbench service.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @see org.eclipse.ui.ISourceProvider
 * @since 3.1
 */
public interface ISources {

	/**
	 * The priority given to default handlers and handlers that are active
	 * across the entire workbench.
	 */
	public static final int WORKBENCH = 0;

	/**
	 * The priority given when the activation is defined by a handler submission
	 * with a legacy priority.
	 */
	public static final int LEGACY_LEGACY = 1;

	/**
	 * The priority given when the activation is defined by a handler submission
	 * with a low priority.
	 */
	public static final int LEGACY_LOW = 1 << 1;

	/**
	 * The priority given when the activation is defined by a handler submission
	 * with a medium priority.
	 */
	public static final int LEGACY_MEDIUM = 1 << 2;

	/**
	 * The priority given when the source includes a particular context.
	 */
	public static final int ACTIVE_CONTEXT = 1 << 6;

	/**
	 * The variable name for the active contexts. This is for use with the
	 * <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_CONTEXT_NAME = "activeContexts"; //$NON-NLS-1$

	/**
	 * The priority given when the source includes the currently active shell.
	 */
	public static final int ACTIVE_SHELL = 1 << 10;

	/**
	 * The variable name for the active shell. This is for use with the
	 * <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_SHELL_NAME = "activeShell"; //$NON-NLS-1$

	/**
	 * The priority given when the source includes the currently active
	 * workbench window.
	 */
	public static final int ACTIVE_WORKBENCH_WINDOW = 1 << 14;

	/**
	 * The variable name for the active workbench window. This is for use with
	 * the <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_WORKBENCH_WINDOW_NAME = "activeWorkbenchWindow"; //$NON-NLS-1$

	/**
	 * The priority given when the source includes the active editor.
	 */
	public static final int ACTIVE_EDITOR = 1 << 18;

	/**
	 * The variable name for the active editor. This is for use with the
	 * <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_EDITOR_NAME = "activeEditor"; //$NON-NLS-1$

	/**
	 * The priority given when the source includes the active part.
	 */
	public static final int ACTIVE_PART = 1 << 22;

	/**
	 * The variable name for the active part. This is for use with the
	 * <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_PART_NAME = "activePartId"; //$NON-NLS-1$

	/**
	 * The priority given when the source includes the active workbench site. In
	 * the case of nesting components, one should be careful to only activate
	 * the most nested component.
	 */
	public static final int ACTIVE_SITE = 1 << 26;

	/**
	 * The variable name for the active workbench site. This is for use with the
	 * <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_SITE_NAME = "activeSite"; //$NON-NLS-1$

	/**
	 * The priority given when the source includes the current selection.
	 */
	public static final int ACTIVE_CURRENT_SELECTION = 1 << 30;

	/**
	 * The variable name for the active selection. This is for use with the
	 * <code>ISourceProvider</code> and <code>IEvaluationContext</code>.
	 */
	public static final String ACTIVE_CURRENT_SELECTION_NAME = "selection"; //$NON-NLS-1$
}
