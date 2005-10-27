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

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.sources.EvaluationResultCache;

/**
 * <p>
 * A token representing the activation of a handler. This token can later be
 * used to cancel that activation. Without this token, then handler will only
 * become inactive if the component in which the handler was activated is
 * destroyed.
 * </p>
 * <p>
 * This caches the command id and the handler, so that they can later be
 * identified.
 * </p>
 * 
 * @since 3.1
 */
final class HandlerActivation extends EvaluationResultCache implements
		IHandlerActivation {

	/**
	 * The identifier for the command which the activated handler handles. This
	 * value is never <code>null</code>.
	 */
	private final String commandId;

	/**
	 * The handler that has been activated. This value may be <code>null</code>.
	 */
	private final IHandler handler;

	/**
	 * The handler service from which this handler activation was request. This
	 * value is never <code>null</code>.
	 */
	private final IHandlerService handlerService;

	/**
	 * Constructs a new instance of <code>HandlerActivation</code>.
	 * 
	 * @param commandId
	 *            The identifier for the command which the activated handler
	 *            handles. This value must not be <code>null</code>.
	 * @param handler `
	 *            The handler that has been activated. This value may be
	 *            <code>null</code>.
	 * @param expression
	 *            The expression that must evaluate to <code>true</code>
	 *            before this handler is active. This value may be
	 *            <code>null</code> if it is always active.
	 * @param handlerService
	 *            The handler service from which the handler activation was
	 *            requested; must not be <code>null</code>.
	 * @see ISources
	 */
	HandlerActivation(final String commandId, final IHandler handler,
			final Expression expression, final IHandlerService handlerService) {
		super(expression);

		if (commandId == null) {
			throw new NullPointerException(
					"The command identifier for a handler activation cannot be null"); //$NON-NLS-1$
		}

		if (handlerService == null) {
			throw new NullPointerException(
					"The handler service for an activation cannot be null"); //$NON-NLS-1$
		}

		this.commandId = commandId;
		this.handler = handler;
		this.handlerService = handlerService;
	}

	public final void clearActive() {
		clearResult();
	}

	public final String getCommandId() {
		return commandId;
	}

	public final IHandler getHandler() {
		return handler;
	}

	public final IHandlerService getHandlerService() {
		return handlerService;
	}

	public final boolean isActive(final IEvaluationContext context) {
		return evaluate(context);
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();

		buffer.append("HandlerActivation(commandId="); //$NON-NLS-1$
		buffer.append(commandId);
		buffer.append(",handler="); //$NON-NLS-1$
		buffer.append(handler);
		buffer.append(",sourcePriority="); //$NON-NLS-1$
		buffer.append(getSourcePriority());
		buffer.append(')');

		return buffer.toString();
	}
}
