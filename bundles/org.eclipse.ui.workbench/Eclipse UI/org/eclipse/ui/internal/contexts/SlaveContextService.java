/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#activateContext(java.lang.String)
	 */
	public IContextActivation activateContext(String contextId) {
		ContextActivation activation = new ContextActivation(contextId,
				fDefaultExpression, this);
		return doActivateContext(activation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#activateContext(java.lang.String,
	 *      org.eclipse.core.expressions.Expression)
	 */
	public IContextActivation activateContext(String contextId,
			Expression expression) {
		return activateContext(contextId, expression, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#activateContext(java.lang.String,
	 *      org.eclipse.core.expressions.Expression, boolean)
	 */
	public IContextActivation activateContext(String contextId,
			Expression expression, boolean global) {
		if (global) {
			IContextActivation activation = fParentService.activateContext(
					contextId, expression, global);
			fParentActivations.add(activation);
			return activation;
		}
		AndExpression andExpression = null;
		if (expression instanceof AndExpression) {
			andExpression = (AndExpression) expression;
		} else {
			andExpression = new AndExpression();
			if (expression!=null) {
				andExpression.add(expression);
			}
		}
		if (fDefaultExpression!=null) {
			andExpression.add(fDefaultExpression);
		}
		ContextActivation activation = new ContextActivation(contextId,
				andExpression, this);
		return doActivateContext(activation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#activateContext(java.lang.String,
	 *      org.eclipse.core.expressions.Expression, int)
	 */
	public IContextActivation activateContext(String contextId,
			Expression expression, int sourcePriorities) {
		return activateContext(contextId, expression);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#addContextManagerListener(org.eclipse.core.commands.contexts.IContextManagerListener)
	 */
	public void addContextManagerListener(IContextManagerListener listener) {
		if (!fContextManagerListeners.contains(listener)) {
			fContextManagerListeners.add(listener);
		}
		fParentService.addContextManagerListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		if (!fSourceProviders.contains(provider)) {
			fSourceProviders.add(provider);
		}
		fParentService.addSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#deactivateContext(org.eclipse.ui.contexts.IContextActivation)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#deactivateContexts(java.util.Collection)
	 */
	public void deactivateContexts(Collection activations) {
		Object[] array = activations.toArray();
		for (int i = 0; i < array.length; i++) {
			deactivateContext((IContextActivation) array[i]);
			array[i] = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#getActiveContextIds()
	 */
	public Collection getActiveContextIds() {
		return fParentService.getActiveContextIds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#getContext(java.lang.String)
	 */
	public Context getContext(String contextId) {
		return fParentService.getContext(contextId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#getDefinedContextIds()
	 */
	public Collection getDefinedContextIds() {
		return fParentService.getDefinedContextIds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#getDefinedContexts()
	 */
	public Context[] getDefinedContexts() {
		return fParentService.getDefinedContexts();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#getShellType(org.eclipse.swt.widgets.Shell)
	 */
	public int getShellType(Shell shell) {
		return fParentService.getShellType(shell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#readRegistry()
	 */
	public void readRegistry() {
		fParentService.readRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#registerShell(org.eclipse.swt.widgets.Shell,
	 *      int)
	 */
	public boolean registerShell(Shell shell, int type) {
		if (!fRegisteredShells.contains(shell)) {
			fRegisteredShells.add(shell);
		}
		return fParentService.registerShell(shell, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#removeContextManagerListener(org.eclipse.core.commands.contexts.IContextManagerListener)
	 */
	public void removeContextManagerListener(IContextManagerListener listener) {
		fContextManagerListeners.remove(listener);
		fParentService.removeContextManagerListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		fSourceProviders.remove(provider);
		fParentService.removeSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.contexts.IContextService#unregisterShell(org.eclipse.swt.widgets.Shell)
	 */
	public boolean unregisterShell(Shell shell) {
		fRegisteredShells.remove(shell);
		return fParentService.unregisterShell(shell);
	}
}
