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

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.ISources;

/**
 * <p>
 * Provides common functionality for evaluating expressions and listening to
 * {@link ISourceProvider} (i.e., the common event framework for commands).
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 * @see ISourceProvider
 * @see ISources
 * @see Expression
 * @see IEvaluationContext
 */
public abstract class ExpressionAuthority implements ISourceProviderListener {

	/**
	 * The evaluation context instance to use when evaluating expression. This
	 * context is shared, and so all calls into <code>sourceChanged</code>
	 * must happen on the event thread.
	 */
	private final IEvaluationContext context;

	/**
	 * The current state of this authority. This is a child of the
	 * {@link #context} that has been given the selection as the default
	 * variable. This value is cleared to <code>null</code> whenever the
	 * selection changes.
	 */
	private IEvaluationContext currentState = null;

	/**
	 * The collection of source providers used by this authority. This
	 * collection is consulted whenever a contribution is made. This collection
	 * only contains instances of <code>ISourceProvider</code>.
	 */
	private final Collection providers = new ArrayList();

	/**
	 * Constructs a new instance of <code>ExpressionAuthority</code>.
	 */
	protected ExpressionAuthority() {
		context = new EvaluationContext(null, this);
		context.setAllowPluginActivation(true);
	}

	/**
	 * Adds a source provider to a list of providers to check when updating.
	 * This also attaches this authority as a listener to the provider.
	 * 
	 * @param provider
	 *            The provider to add; must not be <code>null</code>.
	 */
	public final void addSourceProvider(final ISourceProvider provider) {
		provider.addSourceProviderListener(this);
		providers.add(provider);

		// Update the current state.
		final Map currentState = provider.getCurrentState();
		final Iterator variableItr = currentState.entrySet().iterator();
		while (variableItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) variableItr.next();
			final String variableName = (String) entry.getKey();
			final Object variableValue = entry.getValue();

			/*
			 * Bug 84056. If we update the active workbench window, then we risk
			 * falling back to that shell when the active shell has registered
			 * as "none".
			 */
			if ((variableName != null)
					&& (!ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME
							.equals(variableName))) {
				changeVariable(variableName, variableValue);
			}
		}
	}

	/**
	 * Removes all of the source provider listeners. Subclasses may extend, but
	 * must not override.
	 */
	public void dispose() {
		final Iterator providerItr = providers.iterator();
		while (providerItr.hasNext()) {
			final ISourceProvider provider = (ISourceProvider) providerItr
					.next();
			provider.removeSourceProviderListener(this);
		}

		providers.clear();
	}

	/**
	 * Returns whether at least one of the <code>IEvaluationResultCache</code>
	 * instances in <code>collection</code> evaluates to <code>true</code>.
	 * 
	 * @param collection
	 *            The evaluation result caches to check; must not be
	 *            <code>null</code>, but may be empty.
	 * @return <code>true</code> if there is at least one expression that
	 *         evaluates to <code>true</code>; <code>false</code>
	 *         otherwise.
	 */
	protected final boolean evaluate(final Collection collection) {
		final Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			final IEvaluationResultCache cache = (IEvaluationResultCache) iterator
					.next();
			if (evaluate(cache)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns whether the <code>IEvaluationResultCache</code> evaluates to
	 * <code>true</code>.
	 * 
	 * @param expression
	 *            The evaluation result cache to check; must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the expression evaluates to
	 *         <code>true</code>; <code>false</code> otherwise.
	 */
	protected final boolean evaluate(final IEvaluationResultCache expression) {
		final IEvaluationContext contextWithDefaultVariable = getCurrentState();
		return expression.evaluate(contextWithDefaultVariable);
	}

	/**
	 * Creates a new evaluation context based on the current evaluation context
	 * (i.e., the current state), and places the current selection as the
	 * default variable.
	 * 
	 * @return An evaluation context that can be used for evaluating
	 *         expressions; never <code>null</code>.
	 */
	public final IEvaluationContext getCurrentState() {
		if (currentState == null) {
			final Object defaultVariable = context
					.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
			final IEvaluationContext contextWithDefaultVariable;
			if (defaultVariable instanceof IStructuredSelection) {
				final IStructuredSelection selection = (IStructuredSelection) defaultVariable;
				contextWithDefaultVariable = new EvaluationContext(context,
						selection.toList());
			} else if ((defaultVariable instanceof ISelection)
					&& (!((ISelection) defaultVariable).isEmpty())) {
				contextWithDefaultVariable = new EvaluationContext(context,
						Collections.singleton(defaultVariable));
			} else {
				contextWithDefaultVariable = new EvaluationContext(context,
						Collections.EMPTY_LIST);
			}
			currentState = contextWithDefaultVariable;
		}

		return currentState;
	}

	/**
	 * Returns the variable of the given name.
	 * 
	 * @param name
	 *            The name of the variable to get; must not be <code>null</code>.
	 * @return The variable of the given name; <code>null</code> if none.
	 */
	protected final Object getVariable(final String name) {
		return context.getVariable(name);
	}

	/**
	 * Removes this source provider from the list, and detaches this authority
	 * as a listener.
	 * 
	 * @param provider
	 *            The provider to remove; must not be <code>null</code>.
	 */
	public final void removeSourceProvider(final ISourceProvider provider) {
		provider.removeSourceProviderListener(this);
		providers.remove(provider);

		final Map currentState = provider.getCurrentState();
		final Iterator variableItr = currentState.entrySet().iterator();
		while (variableItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) variableItr.next();
			final String variableName = (String) entry.getKey();
			changeVariable(variableName, null);
		}
	}

	/**
	 * Changes the variable of the given name. If the <code>value</code> is
	 * <code>null</code>, then the variable is removed.
	 * 
	 * @param name
	 *            The name of the variable to change; must not be
	 *            <code>null</code>.
	 * @param value
	 *            The new value; the variable should be removed if this is
	 *            <code>null</code>.
	 */
	protected final void changeVariable(final String name, final Object value) {
		if (value == null) {
			context.removeVariable(name);
		} else {
			context.addVariable(name, value);
		}
	}

	/**
	 * Carries out the actual source change notification. It assumed that by the
	 * time this method is called, <code>getEvaluationContext()</code> is
	 * up-to-date with the current state of the application.
	 * 
	 * @param sourcePriority
	 *            A bit mask of all the source priorities that have changed.
	 */
	protected abstract void sourceChanged(final int sourcePriority);

	/**
	 * Similar to sourceChanged(int) this notifies the subclass about the
	 * change, but using the array of source names that changed instead of the
	 * priority ... int based.
	 * <p>
	 * Clients may override this method.
	 * </p>
	 * 
	 * @param sourceNames
	 *            The array of names that changed.
	 * @since 3.3
	 */
	protected void sourceChanged(final String[] sourceNames) {
		// this is a no-op, since we're late in the game
	}

	public final void sourceChanged(final int sourcePriority,
			final Map sourceValuesByName) {
		// If the selection has changed, invalidate the current state.
		if (sourceValuesByName
				.containsKey(ISources.ACTIVE_CURRENT_SELECTION_NAME)) {
			currentState = null;
		}

		final Iterator entryItr = sourceValuesByName.entrySet().iterator();
		while (entryItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) entryItr.next();
			final String sourceName = (String) entry.getKey();
			final Object sourceValue = entry.getValue();
			updateEvaluationContext(sourceName, sourceValue);
		}
		sourceChanged(sourcePriority, (String[]) sourceValuesByName.keySet()
				.toArray(new String[0]));
	}

	public final void sourceChanged(final int sourcePriority,
			final String sourceName, final Object sourceValue) {
		// If the selection has changed, invalidate the current state.
		if (ISources.ACTIVE_CURRENT_SELECTION_NAME.equals(sourceName)) {
			currentState = null;
		}

		updateEvaluationContext(sourceName, sourceValue);
		sourceChanged(sourcePriority, new String[] { sourceName });
	}

	/**
	 * @param sourcePriority
	 * @param strings
	 */
	private void sourceChanged(int sourcePriority, String[] sourceNames) {
		sourceChanged(sourcePriority);
		sourceChanged(sourceNames);
	}

	/**
	 * Updates the evaluation context with the current state from all of the
	 * source providers.
	 */
	protected final void updateCurrentState() {
		final Iterator providerItr = providers.iterator();
		while (providerItr.hasNext()) {
			final ISourceProvider provider = (ISourceProvider) providerItr
					.next();
			final Map currentState = provider.getCurrentState();
			final Iterator variableItr = currentState.entrySet().iterator();
			while (variableItr.hasNext()) {
				final Map.Entry entry = (Map.Entry) variableItr.next();
				final String variableName = (String) entry.getKey();
				final Object variableValue = entry.getValue();
				/*
				 * Bug 84056. If we update the active workbench window, then we
				 * risk falling back to that shell when the active shell has
				 * registered as "none".
				 */
				if ((variableName != null)
						&& (!ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME
								.equals(variableName))) {
					changeVariable(variableName, variableValue);
				}
			}
		}
	}

	/**
	 * Updates this authority's evaluation context.
	 * 
	 * @param name
	 *            The name of the variable to update; must not be
	 *            <code>null</code>.
	 * @param value
	 *            The new value of the variable. If this value is
	 *            <code>null</code>, then the variable is removed.
	 */
	protected void updateEvaluationContext(final String name, final Object value) {
		if (name != null) {
			changeVariable(name, value);
		}
	}
}
