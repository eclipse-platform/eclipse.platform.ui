/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.contexts;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.expressions.Expression;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * <p>
 * Provides services related to contexts in the Eclipse workbench. This provides
 * access to contexts.
 * </p>
 * 
 * @since 3.1
 */
public final class ContextService implements IContextService {

	/**
	 * The central authority for determining which context we should use.
	 */
	private final ContextAuthority contextAuthority;

	/**
	 * The context manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private final ContextManager contextManager;

	/**
	 * The persistence class for this context service.
	 */
	private final ContextPersistence contextPersistence = new ContextPersistence();

	/**
	 * Constructs a new instance of <code>ContextService</code> using a
	 * context manager.
	 * 
	 * @param contextManager
	 *            The context manager to use; must not be <code>null</code>.
	 */
	public ContextService(final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"Cannot create a context service with a null manager"); //$NON-NLS-1$
		}
		this.contextManager = contextManager;
		this.contextAuthority = new ContextAuthority(contextManager, this);
	}

	public final IContextActivation activateContext(final String contextId) {
		return activateContext(contextId, null);
	}

	public final IContextActivation activateContext(final String contextId,
			final Expression expression) {
		final IContextActivation activation = new ContextActivation(contextId,
				expression, this);
		contextAuthority.activateContext(activation);
		return activation;
	}

	public final IContextActivation activateContext(final String contextId,
			final Expression expression, final int sourcePriority) {
		return activateContext(contextId, expression);
	}

	public final void addContextManagerListener(
			final IContextManagerListener listener) {
		contextManager.addContextManagerListener(listener);
	}

	public final void addSourceProvider(final ISourceProvider provider) {
		contextAuthority.addSourceProvider(provider);
	}

	public final void deactivateContext(final IContextActivation activation) {
		if (activation.getContextService() == this) {
			contextAuthority.deactivateContext(activation);
		}
	}

	public final void deactivateContexts(final Collection activations) {
		final Iterator activationItr = activations.iterator();
		while (activationItr.hasNext()) {
			final IContextActivation activation = (IContextActivation) activationItr
					.next();
			deactivateContext(activation);
		}
	}

	public final Collection getActiveContextIds() {
		return contextManager.getActiveContextIds();
	}

	public final Context getContext(final String contextId) {
		return contextManager.getContext(contextId);
	}

	public final Collection getDefinedContextIds() {
		return contextManager.getDefinedContextIds();
	}

	public final Context[] getDefinedContexts() {
		return contextManager.getDefinedContexts();
	}

	public final int getShellType(final Shell shell) {
		return contextAuthority.getShellType(shell);
	}

	public final void readRegistry() {
		contextPersistence.read(contextManager);
	}

	public final boolean registerShell(final Shell shell, final int type) {
		return contextAuthority.registerShell(shell, type);
	}

	public final void removeContextManagerListener(
			final IContextManagerListener listener) {
		contextManager.addContextManagerListener(listener);
	}

	public final void removeSourceProvider(final ISourceProvider provider) {
		contextAuthority.removeSourceProvider(provider);
	}

	public final boolean unregisterShell(final Shell shell) {
		return contextAuthority.unregisterShell(shell);
	}

	/**
	 * <p>
	 * Bug 95792. A mechanism by which the key binding architecture can force an
	 * update of the contexts (based on the active shell) before trying to
	 * execute a command. This mechanism is required for GTK+ only.
	 * </p>
	 * <p>
	 * DO NOT CALL THIS METHOD.
	 * </p>
	 */
	public final void updateShellKludge() {
		contextAuthority.updateShellKludge();
	}

	/**
	 * <p>
	 * Bug 95792. A mechanism by which the key binding architecture can force an
	 * update of the contexts (based on the active shell) before trying to
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
		final Shell currentActiveShell = contextAuthority.getActiveShell();
		if (currentActiveShell != shell) {
			contextAuthority.sourceChanged(ISources.ACTIVE_SHELL,
					ISources.ACTIVE_SHELL_NAME, shell);
		}
	}
}
