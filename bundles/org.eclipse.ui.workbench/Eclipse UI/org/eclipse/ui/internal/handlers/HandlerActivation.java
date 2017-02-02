/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Friederike Schertel <friederike@schertel.org> - Bug 478336
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.services.SourcePriorityNameMapping;

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
 * <p>
 * <b>Note:</b> this class has a natural ordering that is inconsistent with
 * equals.
 * </p>
 *
 * @since 3.1
 */
final class HandlerActivation implements IHandlerActivation {
	IEclipseContext context;
	private String commandId;
	private IHandler handler;
	E4HandlerProxy proxy;
	private Expression activeWhen;
	private boolean active;
	private int sourcePriority;
	boolean participating = true;

	public HandlerActivation(IEclipseContext context, String cmdId, IHandler handler,
			E4HandlerProxy handlerProxy, Expression expr) {
		this.context = context;
		this.commandId = cmdId;
		this.handler = handler;
		this.proxy = handlerProxy;
		this.activeWhen = expr;
		this.sourcePriority = SourcePriorityNameMapping.computeSourcePriority(activeWhen);
		proxy.activation = this;
	}

	@Override
	public void clearResult() {
	}

	@Override
	public Expression getExpression() {
		return activeWhen;
	}

	@Override
	public int getSourcePriority() {
		return sourcePriority;
	}

	@Override
	public boolean evaluate(IEvaluationContext context) {
		if (activeWhen == null) {
			active = true;
		} else {
			try {
				active = false;
				active = activeWhen.evaluate(context) != EvaluationResult.FALSE;
			} catch (CoreException e) {
				/*
				 * Swallow the exception. It simply means the variable is not
				 * valid (most frequently, that the value is null or has a
				 * complex core expression with a property tester). This kind of
				 * information is not really useful to us, so we can just treat
				 * it as false.
				 */
				if (Policy.DEBUG_CMDS) {
					Activator.trace(Policy.DEBUG_CMDS_FLAG, "Failed to calculate active", e); //$NON-NLS-1$
				}
			}
		}
		return active;
	}

	@Override
	public void setResult(boolean result) {
		active = result;
	}

	@Override
	public int compareTo(Object o) {
		HandlerActivation activation = (HandlerActivation) o;
		int difference;

		// Check the priorities
		int thisPriority = this.getSourcePriority();
		int thatPriority = activation.getSourcePriority();

		// rogue bit problem - ISources.ACTIVE_MENU
		int thisLsb = 0;
		int thatLsb = 0;

		if (((thisPriority & ISources.ACTIVE_MENU) | (thatPriority & ISources.ACTIVE_MENU)) != 0) {
			thisLsb = thisPriority & 1;
			thisPriority = (thisPriority >> 1) & 0x7fffffff;
			thatLsb = thatPriority & 1;
			thatPriority = (thatPriority >> 1) & 0x7fffffff;
		}

		difference = thisPriority - thatPriority;
		if (difference != 0) {
			return difference;
		}

		// if all of the higher bits are the same, check the
		// difference of the LSB
		difference = thisLsb - thatLsb;
		if (difference != 0) {
			return difference;
		}

		// Check depth
		final int thisDepth = this.getDepth();
		final int thatDepth = activation.getDepth();
		difference = thisDepth - thatDepth;
		return difference;
	}

	@Override
	public void clearActive() {
	}

	@Override
	public String getCommandId() {
		return commandId;
	}

	@Override
	public int getDepth() {
		return 0;
	}

	@Override
	public IHandler getHandler() {
		return handler;
	}

	@Override
	public IHandlerService getHandlerService() {
		return (IHandlerService) context.get(IHandlerService.class.getName());
	}

	@Override
	public boolean isActive(IEvaluationContext context) {
		return active;
	}

	@Override
	public String toString() {
		return "EHA: " + active + ":" + sourcePriority + ":" + commandId + ": " + proxy //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ ": " + handler + ": " + context; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
