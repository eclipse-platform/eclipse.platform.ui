/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.services.IEvaluationService;

/**
 * <p>
 * Provides services related to activating and deactivating handlers within the
 * workbench.
 * </p>
 * 
 * @since 3.1
 */
public final class HandlerService implements IHandlerService {

	static {
		Command.DEBUG_HANDLERS = Policy.DEBUG_HANDLERS_VERBOSE;
		Command.DEBUG_HANDLERS_COMMAND_ID = Policy.DEBUG_HANDLERS_VERBOSE_COMMAND_ID;
	}

	/**
	 * The command service for this handler service. This value is never
	 * <code>null</code>.
	 */
	private final ICommandService commandService;

	/**
	 * The central authority for determining which handler we should use.
	 */
	private final HandlerAuthority handlerAuthority;

	/**
	 * The class providing persistence for this service.
	 */
	private final HandlerPersistence handlerPersistence;

	/**
	 * Constructs a new instance of <code>CommandService</code> using a
	 * command manager.
	 * 
	 * @param commandService
	 *            The command service to use; must not be <code>null</code>.
	 * @param evaluationService
	 *            The evaluation service to use; must not be <code>null</code>.
	 */
	public HandlerService(final ICommandService commandService,
			final IEvaluationService evaluationService) {
		if (commandService == null) {
			throw new NullPointerException(
					"A handler service requires a command service"); //$NON-NLS-1$
		}
		this.commandService = commandService;
		this.handlerAuthority = new HandlerAuthority(commandService);
		this.handlerPersistence = new HandlerPersistence(this,
				evaluationService);
	}

	public final IHandlerActivation activateHandler(
			final IHandlerActivation childActivation) {
		final String commandId = childActivation.getCommandId();
		final IHandler handler = childActivation.getHandler();
		final Expression expression = childActivation.getExpression();
		final int depth = childActivation.getDepth() + 1;
		final IHandlerActivation localActivation = new HandlerActivation(
				commandId, handler, expression, depth, this);
		handlerAuthority.activateHandler(localActivation);
		return localActivation;
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler) {
		return activateHandler(commandId, handler, null);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression) {
		return activateHandler(commandId, handler, expression, false);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression,
			final boolean global) {
		final IHandlerActivation activation = new HandlerActivation(commandId,
				handler, expression, IHandlerActivation.ROOT_DEPTH, this);
		handlerAuthority.activateHandler(activation);
		return activation;
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression,
			final int sourcePriority) {
		return activateHandler(commandId, handler, expression);
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		handlerAuthority.addSourceProvider(provider);
	}

	public final ExecutionEvent createExecutionEvent(final Command command,
			final Event event) {
		return new ExecutionEvent(command, Collections.EMPTY_MAP, event, getCurrentState());
	}

	public ExecutionEvent createExecutionEvent(
			final ParameterizedCommand command, final Event event) {
		return new ExecutionEvent(command.getCommand(), command
				.getParameterMap(), event, getCurrentState());
	}

	public final void deactivateHandler(final IHandlerActivation activation) {
		if (activation.getHandlerService() == this) {
			handlerAuthority.deactivateHandler(activation);
		}
	}

	public final void deactivateHandlers(final Collection activations) {
		final Iterator activationItr = activations.iterator();
		while (activationItr.hasNext()) {
			final IHandlerActivation activation = (IHandlerActivation) activationItr
					.next();
			deactivateHandler(activation);
		}
	}

	public final void dispose() {
		handlerAuthority.dispose();
		handlerPersistence.dispose();
	}

	public final Object executeCommand(final ParameterizedCommand command,
			final Event trigger) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		return command.executeWithChecks(trigger, getCurrentState());
	}

	public final Object executeCommand(final String commandId,
			final Event trigger) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		final Command command = commandService.getCommand(commandId);
		final ExecutionEvent event = new ExecutionEvent(command,
				Collections.EMPTY_MAP, trigger, getCurrentState());
		return command.executeWithChecks(event);
	}

	public final IEvaluationContext getCurrentState() {
		return handlerAuthority.getCurrentState();
	}

	public final void readRegistry() {
		handlerPersistence.read();
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		handlerAuthority.removeSourceProvider(provider);
	}

	public final void setHelpContextId(final IHandler handler,
			final String helpContextId) {
		commandService.setHelpContextId(handler, helpContextId);
	}

	/**
	 * <p>
	 * Bug 95792. A mechanism by which the key binding architecture can force an
	 * update of the handlers (based on the active shell) before trying to
	 * execute a command. This mechanism is required for GTK+ only.
	 * </p>
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 */
	public final void updateShellKludge() {
		handlerAuthority.updateShellKludge();
	}

	/**
	 * <p>
	 * Bug 95792. A mechanism by which the key binding architecture can force an
	 * update of the handlers (based on the active shell) before trying to
	 * execute a command. This mechanism is required for GTK+ only.
	 * </p>
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 * 
	 * @param shell
	 *            The shell that should be considered active; must not be
	 *            <code>null</code>.
	 */
	public final void updateShellKludge(final Shell shell) {
		final Shell currentActiveShell = handlerAuthority.getActiveShell();
		if (currentActiveShell != shell) {
			handlerAuthority.sourceChanged(ISources.ACTIVE_SHELL,
					ISources.ACTIVE_SHELL_NAME, shell);
		}
	}

	/**
	 * Currently this is a an internal method to help locate a handler.
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 * 
	 * @param commandId
	 *            the command id to check
	 * @param context
	 *            the context to use for activations
	 * @since 3.3
	 */
	public final IHandler findHandler(String commandId,
			IEvaluationContext context) {
		return handlerAuthority.findHandler(commandId, context);
	}

	/**
	 * Normally the context returned from getCurrentState() still tracks the
	 * application state. This method creates a copy and fills it in with the
	 * variables that we know about. Currently it does not fill in the active
	 * selection.
	 * <p>
	 * DO NOT CALL THIS METHOD. It is experimental in 3.3.
	 * </p>
	 * 
	 * @return an evaluation context with no parent.
	 * @since 3.3
	 */
	public final IEvaluationContext getContextSnapshot() {
		return handlerAuthority.getContextSnapshot();
	}
	
	/**
	 * Normally the context returned from getCurrentState() still tracks the
	 * application state. This method creates a copy and fills it in with all the
	 * variables that we know about.
	 * <p>
	 * DO NOT CALL THIS METHOD. It is experimental in 3.3.
	 * </p>
	 * 
	 * @return an evaluation context with no parent.
	 * @since 3.3
	 */
	public final IEvaluationContext getFullContextSnapshot() {
		return handlerAuthority.getFullContextSnapshot();
	}	

	/**
	 * Execute the command using the provided context. It takes care of finding
	 * the correct active handler given the context, and executes with that
	 * handler.
	 * <p>
	 * It currently cannot effect the enablement of the handler.
	 * </p>
	 * <p>
	 * DO NOT CALL THIS METHOD. It is experimental in 3.3.
	 * </p>
	 * 
	 * @param command
	 *            the parameterized command to execute
	 * @param trigger
	 *            the SWT event trigger ... can be null
	 * @param context
	 *            the evaluation context to run against.
	 * @return
	 * @throws ExecutionException
	 * @throws NotDefinedException
	 * @throws NotEnabledException
	 * @throws NotHandledException
	 * @since 3.3
	 * @see #getContextSnapshot()
	 */
	public final Object executeCommandInContext(
			final ParameterizedCommand command, final Event trigger,
			IEvaluationContext context) throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		IHandler oldHandler = command.getCommand().getHandler();

		IHandler handler = findHandler(command.getId(), context);
		boolean enabled = true;
		if (handler instanceof HandlerProxy) {
			enabled = ((HandlerProxy) handler).getProxyEnabled();
		}

		try {
			command.getCommand().setHandler(handler);
			if (handler instanceof HandlerProxy) {
				((HandlerProxy) handler).setEnabledFor(context);
			}

			return command.executeWithChecks(trigger, context);
		} finally {
			if (handler instanceof HandlerProxy) {
				((HandlerProxy) handler).setProxyEnabled(enabled);
			}
			command.getCommand().setHandler(oldHandler);
		}
	}

	/**
	 * @return Returns the handlerPersistence.
	 */
	public HandlerPersistence getHandlerPersistence() {
		return handlerPersistence;
	}
}
