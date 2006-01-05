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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.misc.Policy;

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
	 * @param commandManager
	 *            The command manager to use; must not be <code>null</code>.
	 */
	public HandlerService(final CommandManager commandManager) {
		this.handlerAuthority = new HandlerAuthority(commandManager);
		this.handlerPersistence = new HandlerPersistence(this);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler) {
		return activateHandler(commandId, handler, null);
	}

	public final IHandlerActivation activateHandler(final String commandId,
			final IHandler handler, final Expression expression) {
		final IHandlerActivation activation = new HandlerActivation(commandId,
				handler, expression, this);
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

	public final IEvaluationContext getCurrentState() {
		return handlerAuthority.getCurrentState();
	}

	public final void readRegistry() {
		handlerPersistence.read();
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		handlerAuthority.removeSourceProvider(provider);
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
}
