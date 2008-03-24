/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.expressions.AndExpression;

/**
 * A handler service which delegates almost all responsibility to the parent
 * service. It is only responsible for disposing of locally activated handlers
 * when it is disposed.
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public class SlaveHandlerService implements IHandlerService {

	/**
	 * The default expression to use when
	 * {@link #activateHandler(String, IHandler)} is called. Handlers
	 * contributed using that method will only be active when this service is
	 * active. However, this expression will be used for conflict resolution.
	 */
	protected final Expression defaultExpression;

	/**
	 * A collection of source providers. The listeners are not
	 * activated/deactivated, but they will be removed when this service is
	 * disposed.
	 */
	private Collection fSourceProviders = new ArrayList();

	/**
	 * A map of the local activation to the parent activations. If this service
	 * is inactive, then all parent activations are <code>null</code>.
	 * Otherwise, they point to the corresponding activation in the parent
	 * service.
	 */
	protected final Map localActivationsToParentActivations = new HashMap();

	/**
	 * The parent handler service to which all requests are ultimately routed.
	 * This value is never <code>null</code>.
	 */
	protected final IHandlerService parent;

	/**
	 * The activations registered with the parent handler service. This value is
	 * never <code>null</code>.
	 */
	protected final Set parentActivations = new HashSet();

	/**
	 * Constructs a new instance.
	 * 
	 * @param parentHandlerService
	 *            The parent handler service for this slave; must not be
	 *            <code>null</code>.
	 * @param defaultExpression
	 *            The default expression to use if none is provided. This is
	 *            primarily used for conflict resolution. This value may be
	 *            <code>null</code>.
	 */
	public SlaveHandlerService(final IHandlerService parentHandlerService,
			final Expression defaultExpression) {
		if (parentHandlerService == null) {
			throw new NullPointerException(
					"The parent handler service cannot be null"); //$NON-NLS-1$
		}

		this.defaultExpression = defaultExpression;
		this.parent = parentHandlerService;
	}

	public final IHandlerActivation activateHandler(
			final IHandlerActivation childActivation) {
		final String commandId = childActivation.getCommandId();
		final IHandler handler = childActivation.getHandler();
		final Expression childExpression = childActivation.getExpression();
		final AndExpression expression;
		if (childExpression instanceof AndExpression) {
			expression = (AndExpression) childExpression;
		} else {
			expression = new AndExpression();
			if (childExpression != null) {
				expression.add(childExpression);
			}
		}
		if (defaultExpression != null) {
			expression.add(defaultExpression);
		}
		final int depth = childActivation.getDepth() + 1;
		final IHandlerActivation localActivation = new HandlerActivation(
				commandId, handler, expression, depth, this);

		return doActivation(localActivation);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler) {
		final IHandlerActivation localActivation = new HandlerActivation(
				commandId, handler, defaultExpression,
				IHandlerActivation.ROOT_DEPTH, this);
		return doActivation(localActivation);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression) {
		return activateHandler(commandId, handler, expression, false);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression,
			final boolean global) {
		if (global) {
			final IHandlerActivation activation = parent.activateHandler(
					commandId, handler, expression, global);
			parentActivations.add(activation);
			return activation;
		}

		final AndExpression andExpression;
		if (expression instanceof AndExpression) {
			andExpression = (AndExpression) expression;
		} else {
			andExpression = new AndExpression();
			if (expression != null) {
				andExpression.add(expression);
			}
		}
		if (defaultExpression != null) {
			andExpression.add(defaultExpression);
		}
		final IHandlerActivation localActivation = new HandlerActivation(
				commandId, handler, andExpression,
				IHandlerActivation.ROOT_DEPTH, this);
		return doActivation(localActivation);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression,
			final int sourcePriorities) {
		return activateHandler(commandId, handler, expression);
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		if (!fSourceProviders.contains(provider)) {
			fSourceProviders.add(provider);
		}
		parent.addSourceProvider(provider);
	}

	public final ExecutionEvent createExecutionEvent(final Command command,
			final Event event) {
		return parent.createExecutionEvent(command, event);
	}

	public final ExecutionEvent createExecutionEvent(
			final ParameterizedCommand command, final Event event) {
		return parent.createExecutionEvent(command, event);
	}

	public final void deactivateHandler(final IHandlerActivation activation) {
		final IHandlerActivation parentActivation;
		if (localActivationsToParentActivations.containsKey(activation)) {
			parentActivation = (IHandlerActivation) localActivationsToParentActivations
					.remove(activation);
		} else {
			parentActivation = activation;
		}

		if (parentActivation != null) {
			parent.deactivateHandler(parentActivation);
			parentActivations.remove(parentActivation);
		}
	}

	public final void deactivateHandlers(final Collection activations) {
		Object[] array = activations.toArray();
		for (int i = 0; i < array.length; i++) {
			deactivateHandler((IHandlerActivation) array[i]);
			array[i] = null;
		}
	}

	public final void dispose() {
		parent.deactivateHandlers(parentActivations);
		parentActivations.clear();
		localActivationsToParentActivations.clear();

		// Remove any "resource", like listeners, that were associated
		// with this service.
		if (!fSourceProviders.isEmpty()) {
			Object[] array = fSourceProviders.toArray();
			for (int i = 0; i < array.length; i++) {
				removeSourceProvider((ISourceProvider) array[i]);
			}
			fSourceProviders.clear();
		}
	}

	protected IHandlerActivation doActivation(
			final IHandlerActivation localActivation) {
		final IHandlerActivation parentActivation;
		parentActivation = parent.activateHandler(localActivation);
		parentActivations.add(parentActivation);
		localActivationsToParentActivations.put(localActivation,
				parentActivation);
		return localActivation;
	}

	public final Object executeCommand(final ParameterizedCommand command,
			final Event event) throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException {
		return parent.executeCommand(command, event);
	}

	public final Object executeCommand(final String commandId, final Event event)
			throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException {
		return parent.executeCommand(commandId, event);
	}

	public final IEvaluationContext getCurrentState() {
		return parent.getCurrentState();
	}

	public final void readRegistry() {
		parent.readRegistry();
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		fSourceProviders.remove(provider);
		parent.removeSourceProvider(provider);
	}

	public final void setHelpContextId(final IHandler handler,
			final String helpContextId) {
		parent.setHelpContextId(handler, helpContextId);
	}

	Expression getDefaultExpression() {
		return defaultExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.handlers.IHandlerService#createContextSnapshot(boolean)
	 */
	public IEvaluationContext createContextSnapshot(boolean includeSelection) {
		return parent.createContextSnapshot(includeSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.handlers.IHandlerService#executeCommandInContext(org.eclipse.core.commands.ParameterizedCommand, org.eclipse.swt.widgets.Event, org.eclipse.core.expressions.IEvaluationContext)
	 */
	public Object executeCommandInContext(ParameterizedCommand command,
			Event event, IEvaluationContext context) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		return parent.executeCommandInContext(command, event, context);
	}
}

