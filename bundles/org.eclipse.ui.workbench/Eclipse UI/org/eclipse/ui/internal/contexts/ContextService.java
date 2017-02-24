/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/
package org.eclipse.ui.internal.contexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.inject.Inject;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * Provides services related to contexts in the Eclipse workbench. This provides
 * access to contexts.
 * </p>
 *
 * @since 3.1
 */
public final class ContextService implements IContextService {

	private HashMap<IContextActivation, UpdateExpression> activationToRat = new HashMap<>();

	/**
	 * The central authority for determining which context we should use.
	 */
	private final ContextAuthority contextAuthority;

	/**
	 * The context manager that supports this service. This value is never
	 * <code>null</code>.
	 */
	private ContextManager contextManager;

	@Inject
	private EContextService contextService;

	@Inject
	private IEclipseContext eclipseContext;

	/**
	 * The persistence class for this context service.
	 */
	private final ContextPersistence contextPersistence;

	/**
	 * Constructs a new instance of <code>ContextService</code> using a
	 * context manager.
	 *
	 * @param contextManager
	 *            The context manager to use; must not be <code>null</code>.
	 */
	@Inject
	public ContextService(final ContextManager contextManager) {
		if (contextManager == null) {
			throw new NullPointerException(
					"Cannot create a context service with a null manager"); //$NON-NLS-1$
		}
		this.contextManager = contextManager;
		this.contextAuthority = new ContextAuthority(contextManager, this);
		this.contextPersistence = new ContextPersistence(contextManager);
	}

	@Override
	public void deferUpdates(boolean defer) {
		contextManager.deferUpdates(defer);
		contextService.deferUpdates(defer);
	}

	@Override
	public final IContextActivation activateContext(final String contextId) {
		return activateContext(contextId, null);
	}

	private class UpdateExpression extends RunAndTrack {
		boolean updating = true;

		private String contextId;
		private Expression expression;

		EvaluationResult cached = null;

		public UpdateExpression(String contextId, Expression expression) {
			this.contextId = contextId;
			this.expression = expression;
		}

		@Override
		public boolean changed(IEclipseContext context) {
			if (!updating) {
				return false;
			}
			ExpressionContext ctx = new ExpressionContext(eclipseContext);
			try {
				if (updating) {
					EvaluationResult result = expression.evaluate(ctx);
					if (cached != null
							&& (cached == result || (cached != EvaluationResult.FALSE && result != EvaluationResult.FALSE))) {
						return updating;
					}
					if (result != EvaluationResult.FALSE) {
						runExternalCode(() -> contextService.activateContext(contextId));
					} else if (cached != null) {
						runExternalCode(() -> contextService.deactivateContext(contextId));
					}
					cached = result;
				}
			} catch (CoreException e) {
				// contextService.deactivateContext(contextId);
				WorkbenchPlugin.log("Failed to update " + contextId, e); //$NON-NLS-1$
			}
			return updating;
		}
	}

	@Override
	public final IContextActivation activateContext(final String contextId,
			final Expression expression) {

		final IContextActivation activation = new ContextActivation(contextId,
				expression, this);
		contextAuthority.activateContext(activation);
		if (expression == null) {
			contextService.activateContext(contextId);
		} else {
			final UpdateExpression runnable = new UpdateExpression(contextId, expression);
			activationToRat.put(activation, runnable);
			eclipseContext.runAndTrack(runnable);
		}
		return activation;
	}

	@Override
	public IContextActivation activateContext(String contextId,
			Expression expression, boolean global) {
		return activateContext(contextId, expression);
	}

	@Override
	public final IContextActivation activateContext(final String contextId,
			final Expression expression, final int sourcePriority) {
		return activateContext(contextId, expression);
	}

	@Override
	public final void addContextManagerListener(
			final IContextManagerListener listener) {
		contextManager.addContextManagerListener(listener);
	}

	@Override
	public final void addSourceProvider(final ISourceProvider provider) {
		contextAuthority.addSourceProvider(provider);
	}

	@Override
	public final void deactivateContext(final IContextActivation activation) {
		if (activation != null && activation.getContextService() == this) {
			final UpdateExpression rat = activationToRat.remove(activation);
			if (rat != null) {
				rat.updating = false;
				if (rat.cached != null && rat.cached != EvaluationResult.FALSE) {
					contextService.deactivateContext(activation.getContextId());
				}
			} else {
				contextService.deactivateContext(activation.getContextId());
			}
			contextAuthority.deactivateContext(activation);
		}
	}

	@Override
	public final void deactivateContexts(final Collection activations) {
		try {
			deferUpdates(true);
			final Iterator<?> activationItr = activations.iterator();
			while (activationItr.hasNext()) {
				final IContextActivation activation = (IContextActivation) activationItr.next();
				deactivateContext(activation);
			}
		} finally {
			deferUpdates(false);
		}
	}

	@Override
	public final void dispose() {
		contextPersistence.dispose();
		contextAuthority.dispose();
	}

	@Override
	public final Collection getActiveContextIds() {
		return contextService.getActiveContextIds();
	}

	@Override
	public final Context getContext(final String contextId) {
		return contextService.getContext(contextId);
	}

	@Override
	public final Collection getDefinedContextIds() {
		return contextManager.getDefinedContextIds();
	}

	@Override
	public final Context[] getDefinedContexts() {
		return contextManager.getDefinedContexts();
	}

	@Override
	public final int getShellType(final Shell shell) {
		return contextAuthority.getShellType(shell);
	}

	@Override
	public final void readRegistry() {
		// contextPersistence.read();
	}

	@Override
	public final boolean registerShell(final Shell shell, final int type) {
		return contextAuthority.registerShell(shell, type);
	}

	@Override
	public final void removeContextManagerListener(
			final IContextManagerListener listener) {
		contextManager.removeContextManagerListener(listener);
	}

	@Override
	public final void removeSourceProvider(final ISourceProvider provider) {
		contextAuthority.removeSourceProvider(provider);
	}

	@Override
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
