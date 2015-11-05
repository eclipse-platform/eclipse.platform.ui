/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.expressions.Expression;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.expressions.AndExpression;

/**
 * A context service which delegates almost all responsibility to the parent
 * service.
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 *
 */
public class SlaveContextService implements IContextService {

	/**
	 * The parent context service, which is never <code>null</code>.
	 */
	protected IContextService fParentService;

	/**
	 * The default expression used when {@link #activateContext(String) } is
	 * called. Contexts contributed that use this expression will only be active
	 * with this service is active.
	 */
	protected Expression fDefaultExpression;

	/**
	 * Our contexts that are currently active with the parent context service.
	 */
	protected Set fParentActivations;

	/**
	 * A map of the local activation to the parent activations. If this service
	 * is inactive, then all parent activations are <code>null</code>.
	 * Otherwise, they point to the corresponding activation in the parent
	 * service.
	 */
	protected Map fLocalActivations;

	/**
	 * A collection of context manager listeners. The listeners are not
	 * activated/deactivated, but they will be removed when this service is
	 * disposed.
	 */
	private Collection fContextManagerListeners;

	/**
	 * A collection of source providers. The listeners are not
	 * activated/deactivated, but they will be removed when this service is
	 * disposed.
	 */
	private Collection fSourceProviders;

	/**
	 * A collection of shells registered through this service. The listeners are
	 * not activated/deactivated, but they will be removed when this service is
	 * disposed.
	 */
	private Collection fRegisteredShells;

	/**
	 * Construct the new slave.
	 *
	 * @param parentService
	 *            the parent context service; must not be <code>null</code>.
	 * @param defaultExpression
	 *            A default expression to use to determine viability. It's
	 *            mainly used for conflict resolution. It can be
	 *            <code>null</code>.
	 */
	public SlaveContextService(IContextService parentService,
			Expression defaultExpression) {
		if (parentService == null) {
			throw new NullPointerException(
					"The parent context service must not be null"); //$NON-NLS-1$
		}
		fParentService = parentService;
		fDefaultExpression = defaultExpression;
		fParentActivations = new HashSet();
		fLocalActivations = new HashMap();
		fContextManagerListeners = new ArrayList();
		fSourceProviders = new ArrayList();
		fRegisteredShells = new ArrayList();
	}

	@Override
	public void deferUpdates(boolean defer) {
		fParentService.deferUpdates(defer);
	}

	@Override
	public IContextActivation activateContext(String contextId) {

		ContextActivation activation = new ContextActivation(contextId,
				fDefaultExpression, this);
		return doActivateContext(activation);
	}

	@Override
	public IContextActivation activateContext(String contextId,
			Expression expression) {
		return activateContext(contextId, expression, false);
	}

	@Override
	public IContextActivation activateContext(String contextId,
			Expression expression, boolean global) {
		if (global) {
			IContextActivation activation = fParentService.activateContext(
					contextId, expression, global);
			fParentActivations.add(activation);
			return activation;
		}

		Expression aExpression = fDefaultExpression;
		if (expression != null && fDefaultExpression != null) {
			final AndExpression andExpression = new AndExpression();
			andExpression.add(expression);
			andExpression.add(fDefaultExpression);
			aExpression = andExpression;
		} else if (expression != null) {
			aExpression = expression;
		}

		ContextActivation activation = new ContextActivation(contextId,
				aExpression, this);
		return doActivateContext(activation);
	}

	@Override
	public IContextActivation activateContext(String contextId,
			Expression expression, int sourcePriorities) {
		return activateContext(contextId, expression);
	}

	@Override
	public void addContextManagerListener(IContextManagerListener listener) {
		if (!fContextManagerListeners.contains(listener)) {
			fContextManagerListeners.add(listener);
		}
		fParentService.addContextManagerListener(listener);
	}

	@Override
	public void addSourceProvider(ISourceProvider provider) {
		if (!fSourceProviders.contains(provider)) {
			fSourceProviders.add(provider);
		}
		fParentService.addSourceProvider(provider);
	}

	@Override
	public void deactivateContext(IContextActivation activation) {
		IContextActivation parentActivation = null;
		if (fLocalActivations.containsKey(activation)) {
			parentActivation = (IContextActivation) fLocalActivations
					.remove(activation);
		} else {
			parentActivation = activation;
		}
		if (parentActivation != null) {
			fParentService.deactivateContext(parentActivation);
			fParentActivations.remove(parentActivation);
		}
	}

	@Override
	public void deactivateContexts(Collection activations) {
		Object[] array = activations.toArray();
		for (int i = 0; i < array.length; i++) {
			deactivateContext((IContextActivation) array[i]);
			array[i] = null;
		}
	}

	@Override
	public void dispose() {
		fParentService.deactivateContexts(fParentActivations);
		fParentActivations.clear();
		fLocalActivations.clear();

		// Remove any "resource", like listeners, that were associated
		// with this service.
		if (!fContextManagerListeners.isEmpty()) {
			Object[] array = fContextManagerListeners.toArray();
			for (int i = 0; i < array.length; i++) {
				removeContextManagerListener((IContextManagerListener) array[i]);
			}
			fContextManagerListeners.clear();
		}
		if (!fSourceProviders.isEmpty()) {
			Object[] array = fSourceProviders.toArray();
			for (int i = 0; i < array.length; i++) {
				removeSourceProvider((ISourceProvider) array[i]);
			}
			fSourceProviders.clear();
		}
		if (!fRegisteredShells.isEmpty()) {
			Object[] array = fRegisteredShells.toArray();
			for (int i = 0; i < array.length; i++) {
				unregisterShell((Shell) array[i]);
			}
			fRegisteredShells.clear();
		}
	}

	/**
	 * Activate the context with respect to this slave service.
	 *
	 * @param contextId
	 *            the context id
	 * @param expression
	 *            the expression to use
	 * @return the activated context
	 */
	protected IContextActivation doActivateContext(IContextActivation activation) {
		IContextActivation parentActivation = fParentService.activateContext(
				activation.getContextId(), activation.getExpression());
		fParentActivations.add(parentActivation);
		fLocalActivations.put(activation, parentActivation);
		return activation;
	}

	@Override
	public Collection getActiveContextIds() {
		return fParentService.getActiveContextIds();
	}

	@Override
	public Context getContext(String contextId) {
		return fParentService.getContext(contextId);
	}

	@Override
	public Collection getDefinedContextIds() {
		return fParentService.getDefinedContextIds();
	}

	@Override
	public Context[] getDefinedContexts() {
		return fParentService.getDefinedContexts();
	}

	@Override
	public int getShellType(Shell shell) {
		return fParentService.getShellType(shell);
	}

	@Override
	public void readRegistry() {
		fParentService.readRegistry();
	}

	@Override
	public boolean registerShell(Shell shell, int type) {
		if (!fRegisteredShells.contains(shell)) {
			fRegisteredShells.add(shell);
		}
		return fParentService.registerShell(shell, type);
	}

	@Override
	public void removeContextManagerListener(IContextManagerListener listener) {
		fContextManagerListeners.remove(listener);
		fParentService.removeContextManagerListener(listener);
	}

	@Override
	public void removeSourceProvider(ISourceProvider provider) {
		fSourceProviders.remove(provider);
		fParentService.removeSourceProvider(provider);
	}

	@Override
	public boolean unregisterShell(Shell shell) {
		fRegisteredShells.remove(shell);
		return fParentService.unregisterShell(shell);
	}
}
