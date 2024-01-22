/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.services;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;

/**
 * <p>
 * A cache of the result of an expression. This also provides the source
 * priority for the expression.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 *
 * @since 3.2
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.ISourceProvider
 */
public interface IEvaluationResultCache {

	/**
	 * Clears the cached computation of the <code>evaluate</code> method, if any.
	 * This method is only intended for internal use. It provides a mechanism by
	 * which <code>ISourceProvider</code> events can invalidate state on a
	 * <code>IEvaluationResultCache</code> instance.
	 */
	void clearResult();

	/**
	 * Returns the expression controlling the activation or visibility of this item.
	 *
	 * @return The expression associated with this item; may be <code>null</code>.
	 */
	Expression getExpression();

	/**
	 * Returns the priority that has been given to this expression.
	 *
	 * @return The priority.
	 * @see ISources
	 */
	int getSourcePriority();

	/**
	 * Evaluates the expression -- given the current state of the workbench. This
	 * method should cache its computation. The cache will be cleared by a call to
	 * <code>clearResult</code>.
	 *
	 * @param context The context in which this state should be evaluated; must not
	 *                be <code>null</code>.
	 * @return <code>true</code> if the expression currently evaluates to
	 *         <code>true</code>; <code>false</code> otherwise.
	 */
	boolean evaluate(IEvaluationContext context);

	/**
	 * Forces the cached result to be a particular value. This will <b>not</b>
	 * notify any users of the cache that it has changed.
	 *
	 * @param result The cached result to use.
	 * @since 3.3
	 */
	void setResult(boolean result);
}
