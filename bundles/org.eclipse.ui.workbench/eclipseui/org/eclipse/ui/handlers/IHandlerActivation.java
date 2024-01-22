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

package org.eclipse.ui.handlers;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.internal.services.IEvaluationResultCache;

/**
 * <p>
 * A token representing the activation of a handler. This token can later be
 * used to cancel that activation. Without this token, then handler will only
 * become inactive if the component in which the handler was activated is
 * destroyed.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 *
 * @since 3.1
 * @see org.eclipse.ui.ISources
 * @see org.eclipse.ui.ISourceProvider
 */
public interface IHandlerActivation extends IEvaluationResultCache, Comparable {

	/**
	 * The depth at which the root exists.
	 *
	 * @since 3.2
	 */
	int ROOT_DEPTH = 1;

	/**
	 * Clears the cached computation of the <code>isActive</code> method, if any.
	 * This method is only intended for internal use. It provides a mechanism by
	 * which <code>ISourceProvider</code> events can invalidate state on a
	 * <code>IHandlerActivation</code> instance.
	 *
	 * @deprecated Use {@link IEvaluationResultCache#clearResult()} instead.
	 */
	@Deprecated
	void clearActive();

	/**
	 * Returns the identifier of the command whose handler is being activated.
	 *
	 * @return The command identifier; never <code>null</code>.
	 */
	String getCommandId();

	/**
	 * Returns the depth at which this activation was created within the services
	 * hierarchy. The root of the hierarchy is at a depth of <code>1</code>. This is
	 * used as the final tie-breaker in the event that no other method can be used
	 * to determine a winner.
	 *
	 * @return The depth at which the handler was inserted into the services
	 *         hierarchy; should be a positive integer.
	 * @since 3.2
	 */
	int getDepth();

	/**
	 * Returns the handler that should be activated.
	 *
	 * @return The handler; may be <code>null</code>.
	 */
	IHandler getHandler();

	/**
	 * Returns the handler service from which this activation was requested. This is
	 * used to ensure that an activation can only be retracted from the same service
	 * which issued it.
	 *
	 * @return The handler service; never <code>null</code>.
	 */
	IHandlerService getHandlerService();

	/**
	 * Returns whether this handler activation is currently active -- given the
	 * current state of the workbench. This method should cache its computation. The
	 * cache will be cleared by a call to <code>clearActive</code>.
	 *
	 * @param context The context in which this state should be evaluated; must not
	 *                be <code>null</code>.
	 * @return <code>true</code> if the activation is currently active;
	 *         <code>false</code> otherwise.
	 * @deprecated Use {@link IEvaluationResultCache#evaluate(IEvaluationContext)}
	 *             instead.
	 */
	@Deprecated
	boolean isActive(IEvaluationContext context);
}
