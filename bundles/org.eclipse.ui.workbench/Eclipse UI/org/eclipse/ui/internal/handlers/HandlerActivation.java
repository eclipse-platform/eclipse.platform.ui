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
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;

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
public final class HandlerActivation implements IHandlerActivation {

	/**
	 * The identifier for the command which the activated handler handles. This
	 * value is never <code>null</code>.
	 */
	private final String commandId;

	/**
	 * The previous computed evaluation result. If no evaluation result is
	 * available, then this value is <code>null</code>.
	 */
	private EvaluationResult evaluationResult = null;

	/**
	 * The expression to evaluate when trying to determine whether this handler
	 * activation should try to be active. This value may be <code>null</code>
	 * if this activation should always be active.
	 */
	private final Expression expression;

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
	 * The priority that has been given to this handler activation.
	 */
	private final int sourcePriority;

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
	 * @param sourcePriority
	 *            The priority that should be given to this handler activation
	 * @param handlerService
	 *            The handler service from which the handler activation was
	 *            requested; must not be <code>null</code>.
	 * @see ISources
	 */
	public HandlerActivation(final String commandId, final IHandler handler,
			final Expression expression, final int sourcePriority,
			final IHandlerService handlerService) {
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
		this.expression = expression;
		this.sourcePriority = sourcePriority;
		this.handlerService = handlerService;
	}

	public final void clearActive() {
		evaluationResult = null;
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

	public final int getSourcePriority() {
		return sourcePriority;
	}

	public final boolean isActive(final IEvaluationContext context) {
		if (expression == null) {
			return true;
		}

		if (evaluationResult == null) {
			try {
				evaluationResult = expression.evaluate(context);
			} catch (final CoreException e) {
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, e.getMessage(), e);
				WorkbenchPlugin.log("Could not evaluate an expression", status); //$NON-NLS-1$
				return false;
			}
		}

		return evaluationResult == EvaluationResult.TRUE;
	}
	
	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		buffer.append("HandlerActivation(commandId="); //$NON-NLS-1$
		buffer.append(commandId);
		buffer.append(",handler="); //$NON-NLS-1$
		buffer.append(handler);
		buffer.append(",expression="); //$NON-NLS-1$
		buffer.append(expression);
		buffer.append(",sourcePriority="); //$NON-NLS-1$
		buffer.append(sourcePriority);
		buffer.append(",evaluationResult="); //$NON-NLS-1$
		buffer.append(evaluationResult);
		buffer.append(')');

		return buffer.toString();
	}
}
