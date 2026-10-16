/*******************************************************************************
 * Copyright (c) 2025 Eclipse Platform and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Platform - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.commands;

import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * A marker interface for handlers that have enabledWhen expressions that need to be
 * evaluated before @CanExecute.
 *
 * @since 1.1
 * @noimplement This interface is not intended to be implemented by clients outside of the
 *              workbench.
 */
public interface IHandlerWithExpression {
	/**
	 * Evaluates the enabledWhen expression if present.
	 *
	 * @param context the eclipse context for evaluation
	 * @return true if no expression is defined or if the expression evaluates to true
	 */
	boolean evaluateEnabledWhen(IEclipseContext context);

	/**
	 * Returns the actual handler object that should be used for execution.
	 *
	 * @return the handler object
	 */
	Object getHandler();
}
