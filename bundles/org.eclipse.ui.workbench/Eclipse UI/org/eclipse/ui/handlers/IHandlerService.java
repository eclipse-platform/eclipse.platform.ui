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

package org.eclipse.ui.handlers;

import java.util.Collection;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.services.IServiceWithSources;

/**
 * <p>
 * Provides services related to activating and deactivating handlers within the
 * workbench.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @since 3.1
 */
public interface IHandlerService extends IServiceWithSources {

	/**
	 * <p>
	 * Activates the given handler within the context of this service. If this
	 * service was retrieved from the workbench, then this handler will be
	 * active globally. If the service was retrieved from a nested component,
	 * then the handler will only be active within that component.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 */
	public IHandlerActivation activateHandler(String commandId, IHandler handler);

	/**
	 * <p>
	 * Activates the given handler within the context of this service. The
	 * handler becomes active when <code>expression</code> evaluates to
	 * <code>true</code>.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this handler will really become active. The expression may be
	 *            <code>null</code> if the handler should always be active.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @since 3.2
	 */
	public IHandlerActivation activateHandler(String commandId,
			IHandler handler, Expression expression);

	/**
	 * <p>
	 * Activates the given handler within the context of this service. The
	 * handler becomes active when <code>expression</code> evaluates to
	 * <code>true</code>.
	 * </p>
	 * <p>
	 * Also, it is guaranteed that the handlers submitted through a particular
	 * service will be cleaned up when that services is destroyed. So, for
	 * example, a service retrieved from a <code>IWorkbenchPartSite</code>
	 * would deactivate all of its handlers when the site is destroyed.
	 * </p>
	 * 
	 * @param commandId
	 *            The identifier for the command which this handler handles;
	 *            must not be <code>null</code>.
	 * @param handler
	 *            The handler to activate; must not be <code>null</code>.
	 * @param expression
	 *            This expression must evaluate to <code>true</code> before
	 *            this handler will really become active. The expression may be
	 *            <code>null</code> if the handler should always be active.
	 * @param sourcePriorities
	 *            The source priorities for the expression.
	 * @return A token which can be used to later cancel the activation. Only
	 *         someone with access to this token can cancel the activation. The
	 *         activation will automatically be cancelled if the context from
	 *         which this service was retrieved is destroyed.
	 * 
	 * @see org.eclipse.ui.ISources
	 * @deprecated Use
	 *             {@link IHandlerService#activateHandler(String, IHandler, Expression)}
	 *             instead.
	 */
	public IHandlerActivation activateHandler(String commandId,
			IHandler handler, Expression expression, int sourcePriorities);

	/**
	 * Deactivates the given handler within the context of this service. If the
	 * handler was activated with a different service, then it must be
	 * deactivated from that service instead. It is only possible to retract a
	 * handler activation with this method. That is, you must have the same
	 * <code>IHandlerActivation</code> used to activate the handler.
	 * 
	 * @param activation
	 *            The token that was returned from a call to
	 *            <code>activateHandler</code>; must not be <code>null</code>.
	 */
	public void deactivateHandler(IHandlerActivation activation);

	/**
	 * Deactivates the given handlers within the context of this service. If the
	 * handler was activated with a different service, then it must be
	 * deactivated from that service instead. It is only possible to retract a
	 * handler activation with this method. That is, you must have the same
	 * <code>IHandlerActivation</code> used to activate the handler.
	 * 
	 * @param activations
	 *            The tokens that were returned from a call to
	 *            <code>activateHandler</code>. This collection must only
	 *            contain instances of <code>IHandlerActivation</code>. The
	 *            collection must not be <code>null</code>.
	 */
	public void deactivateHandlers(Collection activations);

	/**
	 * Returns an evaluation context representing the current state of the
	 * world.
	 * 
	 * @return the current state of the application; never <code>null</code>.
	 */
	public IEvaluationContext getCurrentState();

	/**
	 * Checks to see if there would be a conflict if a handler is added with the
	 * given expression. The expression is compared against the expressions for
	 * other handlers.
	 * 
	 * @param commandId
	 *            The identifier of the command which should be checked for
	 *            potential conflicts; must not be <code>null</code>.
	 * @param expression
	 *            The expression to check against the existing handler
	 *            activations; may be <code>null</code>.
	 * @return <code>true</code> if there might be a conflict;
	 *         <code>false</code> otherwise.
	 * @since 3.2
	 */
	public boolean isConflict(String commandId, Expression expression);

	/**
	 * <p>
	 * Reads the handler information from the registry. This will overwrite any
	 * of the existing information in the handler service. This method is
	 * intended to be called during start-up. When this method completes, this
	 * handler service will reflect the current state of the registry.
	 * </p>
	 */
	public void readRegistry();
}
