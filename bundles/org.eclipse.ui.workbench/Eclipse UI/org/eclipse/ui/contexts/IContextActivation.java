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

package org.eclipse.ui.contexts;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.internal.services.IEvaluationResultCache;

/**
 * <p>
 * A token representing the activation of a context. This token can later be
 * used to cancel that activation. Without this token, then context will only
 * become inactive if the component in which the context was activated is
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
public interface IContextActivation extends IEvaluationResultCache {

	/**
	 * Clears the cached computation of the <code>isActive</code> method, if
	 * any. This method is only intended for internal use. It provides a
	 * mechanism by which <code>ISourceProvider</code> events can invalidate
	 * state on a <code>IContextActivation</code> instance.
	 * 
	 * @deprecated Use {@link IEvaluationResultCache#clearResult()} instead.
	 */
	public void clearActive();

	/**
	 * Returns the identifier of the context that is being activated.
	 * 
	 * @return The context identifier; never <code>null</code>.
	 */
	public String getContextId();

	/**
	 * Returns the context service from which this activation was requested.
	 * This is used to ensure that an activation can only be retracted from the
	 * same service which issued it.
	 * 
	 * @return The context service; never <code>null</code>.
	 */
	public IContextService getContextService();

	/**
	 * Returns whether this context activation is currently active -- given the
	 * current state of the workbench. This method should cache its computation.
	 * The cache will be cleared by a call to <code>clearActive</code>.
	 * 
	 * @param context
	 *            The context in which this state should be evaluated; must not
	 *            be <code>null</code>.
	 * @return <code>true</code> if the activation is currently active;
	 *         <code>false</code> otherwise.
	 * @deprecated Use
	 *             {@link IEvaluationResultCache#evaluate(IEvaluationContext)}
	 *             instead.
	 */
	public boolean isActive(IEvaluationContext context);
}
