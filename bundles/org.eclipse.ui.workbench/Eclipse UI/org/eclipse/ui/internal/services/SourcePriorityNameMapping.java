/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.ui.ISources;

/**
 * <p>
 * A static class linking the names of variables in an IEvaluationContext to the
 * priority they should be given when doing conflict resolution.
 * </p>
 * <p>
 * In the future, it will possible to define a new variable (i.e., piece of
 * application state) that you want to use inside of the
 * <code>org.eclipse.ui.contexts</code>, <code>org.eclipse.ui.handlers</code>
 * or <code>org.eclipse.ui.menus</code> extension points. As it stands right
 * now, it is not possible to run code soon enough for the
 * <code>IHandlerService</code>, <code>IMenuService</code> or
 * <code>IContextService</code> to become aware of the new variables. This
 * will likely be fixed with a new extension point.
 * </p>
 * <p>
 * TODO Move to "org.eclipse.ui" and resolve the above issue.
 * </p>
 * 
 * @since 3.2
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.contexts.IContextService
 * @see org.eclipse.ui.handlers.IHandlerService
 * @see org.eclipse.ui.menus.IMenuService
 */
public final class SourcePriorityNameMapping implements ISources {

	/**
	 * The variable name to use when boosting priority on an activation.
	 */
	public static final String LEGACY_LEGACY_NAME = "LEGACY"; //$NON-NLS-1$

	/**
	 * The value returned if there is source priority for the given name
	 * 
	 * @see SourcePriorityNameMapping#getMapping(String)
	 */
	public static final int NO_SOURCE_PRIORITY = 0;

	/**
	 * The map of source priorities indexed by name. This value is never
	 * <code>null</code>.
	 */
	private static final Map sourcePrioritiesByName = new HashMap();

	static {
		addMapping(ACTIVE_ACTION_SETS_NAME, ACTIVE_ACTION_SETS);
		addMapping(ACTIVE_CONTEXT_NAME, ACTIVE_CONTEXT);
		addMapping(ACTIVE_CURRENT_SELECTION_NAME, ACTIVE_CURRENT_SELECTION);
		addMapping(ACTIVE_EDITOR_NAME, ACTIVE_EDITOR);
		addMapping(ACTIVE_EDITOR_ID_NAME, ACTIVE_EDITOR_ID);
		addMapping(ACTIVE_MENU_NAME, ACTIVE_MENU);
		addMapping(ACTIVE_MENU_SELECTION_NAME, ACTIVE_MENU);
		addMapping(ACTIVE_MENU_EDITOR_INPUT_NAME, ACTIVE_MENU);
		addMapping(ACTIVE_FOCUS_CONTROL_ID_NAME, ACTIVE_MENU);
		addMapping(ACTIVE_FOCUS_CONTROL_NAME, ACTIVE_MENU);
		addMapping(ACTIVE_PART_NAME, ACTIVE_PART);
		addMapping(ACTIVE_PART_ID_NAME, ACTIVE_PART_ID);
		addMapping(ACTIVE_SHELL_NAME, ACTIVE_SHELL);
		addMapping(ACTIVE_SITE_NAME, ACTIVE_SITE);
		addMapping(ACTIVE_WORKBENCH_WINDOW_NAME, ACTIVE_WORKBENCH_WINDOW);
		addMapping(ACTIVE_WORKBENCH_WINDOW_SHELL_NAME,
				ACTIVE_WORKBENCH_WINDOW_SHELL);
		addMapping(ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME,
				ACTIVE_WORKBENCH_WINDOW_SUBORDINATE);
		addMapping(ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME,
				ACTIVE_WORKBENCH_WINDOW_SUBORDINATE);
		addMapping(ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME,
				ACTIVE_WORKBENCH_WINDOW_SUBORDINATE);
		addMapping(LEGACY_LEGACY_NAME, LEGACY_LEGACY);
		addMapping("workbench", WORKBENCH); //$NON-NLS-1$
	}

	/**
	 * Adds a mapping between a source name and a source priority. This method
	 * also cleans up any existing mappings using the same name or priority.
	 * There is a one-to-one relationship between name and priority.
	 * 
	 * @param sourceName
	 *            The name of the variable as it would appear in an XML
	 *            expression; must not be <code>null</code>.
	 * @param sourcePriority
	 *            The priority of the source with respect to other sources. A
	 *            higher value means that expressions including this priority
	 *            will win ties more often. It is recommended that this value is
	 *            simply a single bit shifted to a particular place.
	 * @see ISources
	 */
	public static final void addMapping(final String sourceName,
			final int sourcePriority) {
		if (sourceName == null) {
			throw new NullPointerException("The source name cannot be null."); //$NON-NLS-1$
		}

		if (!sourcePrioritiesByName.containsKey(sourceName)) {
			final Integer priority = new Integer(sourcePriority);

			sourcePrioritiesByName.put(sourceName, priority);
		}
	}

	/**
	 * Computes the source priority for the given expression. The source
	 * priority is a bit mask of all of the variables references by the
	 * expression. The default variable is considered to be
	 * {@link ISources#ACTIVE_CURRENT_SELECTION}. The source priority is used
	 * to minimize recomputations of the expression, and it can also be used for
	 * conflict resolution.
	 * 
	 * @param expression
	 *            The expression for which the source priority should be
	 *            computed; may be <code>null</code>.
	 * @return The bit mask of all the sources required for this expression;
	 *         <code>0</code> if none.
	 */
	public static final int computeSourcePriority(final Expression expression) {
		int sourcePriority = ISources.WORKBENCH;

		if (expression == null) {
			return sourcePriority;
		}

		final ExpressionInfo info = expression.computeExpressionInfo();

		// Add the default variable, if any.
		if (info.hasDefaultVariableAccess()) {
			sourcePriority |= ISources.ACTIVE_CURRENT_SELECTION;
		}

		// Add all of the reference variables.
		final String[] sourceNames = info.getAccessedVariableNames();
		for (int i = 0; i < sourceNames.length; i++) {
			final String sourceName = sourceNames[i];
			sourcePriority |= getMapping(sourceName);
		}

		return sourcePriority;
	}

	/**
	 * Gets the priority for the source with the given name.
	 * 
	 * @param sourceName
	 *            The name of the variable as it would appear in an XML
	 *            expression; should not be <code>null</code>.
	 * @return The source priority that matches, if any;
	 *         <code>NO_SOURCE_PRIORITY</code> if none is found.
	 */
	public static final int getMapping(final String sourceName) {
		final Object object = sourcePrioritiesByName.get(sourceName);
		if (object instanceof Integer) {
			return ((Integer) object).intValue();
		}

		return NO_SOURCE_PRIORITY;
	}

	/**
	 * This class should not be instantiated.
	 */
	private SourcePriorityNameMapping() {
		// This class should not be instantiated.
	}
}
