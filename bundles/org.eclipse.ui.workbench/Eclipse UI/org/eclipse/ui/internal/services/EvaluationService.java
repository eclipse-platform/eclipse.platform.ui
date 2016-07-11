/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.3
 *
 */
public final class EvaluationService implements IEvaluationService {
	public static final String DEFAULT_VAR = "org.eclipse.ui.internal.services.EvaluationService.default_var"; //$NON-NLS-1$
	private static final String RE_EVAL = "org.eclipse.ui.internal.services.EvaluationService.evaluate"; //$NON-NLS-1$
	private boolean evaluate = false;
	private ExpressionContext legacyContext;
	private IEclipseContext context;
	private IEclipseContext ratContext;
	private int notifying = 0;

	private ListenerList<IPropertyChangeListener> serviceListeners = new ListenerList<>(ListenerList.IDENTITY);
	ArrayList<ISourceProvider> sourceProviders = new ArrayList<>();
	LinkedList<EvaluationReference> refs = new LinkedList<>();
	private ISourceProviderListener contextUpdater;

	private HashSet<String> ratVariables = new HashSet<>();
	private RunAndTrack ratUpdater = new RunAndTrack() {
		@Override
		public boolean changed(IEclipseContext context) {
			context.get(RE_EVAL);
			String[] vars = ratVariables.toArray(new String[ratVariables.size()]);
			for (String var : vars) {
				Object value = context.getActive(var);
				if (value == null) {
					ratContext.remove(var);
				} else {
					ratContext.set(var, value);
				}
			}
			// This ties tool item enablement to variable changes that can
			// effect the enablement.
			getEventBroker()
					.post(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
			return true;
		}
	};

	private HashSet<String> variableFilter = new HashSet<>();
	private IEventBroker eventBroker;

	public EvaluationService(IEclipseContext c) {
		context = c;
		ratContext = context.getParent().createChild(getClass().getName());
		legacyContext = new ExpressionContext(context);
		ExpressionContext.defaultVariableConverter = new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				Object defaultVariable = context.getLocal(DEFAULT_VAR);
				if (defaultVariable != null
						&& defaultVariable != IEvaluationContext.UNDEFINED_VARIABLE) {
					return defaultVariable;
				}
				defaultVariable = context.getActive(IServiceConstants.ACTIVE_SELECTION);
				if (defaultVariable instanceof IStructuredSelection) {
					final IStructuredSelection selection = (IStructuredSelection) defaultVariable;
					return selection.toList();
				} else if ((defaultVariable instanceof ISelection)
						&& (!((ISelection) defaultVariable).isEmpty())) {
					return Collections.singleton(defaultVariable);
				}
				return null;
			}
		};
		contextUpdater = new ISourceProviderListener() {

			@Override
			public void sourceChanged(int sourcePriority, String sourceName, Object sourceValue) {
				changeVariable(sourceName, sourceValue);
			}

			@Override
			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
				Iterator i = sourceValuesByName.entrySet().iterator();
				while (i.hasNext()) {
					final Map.Entry entry = (Entry) i.next();
					changeVariable((String) entry.getKey(), entry.getValue());
				}
			}
		};
		variableFilter.addAll(Arrays.asList(new String[] { ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
				ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME, ISources.ACTIVE_EDITOR_ID_NAME,
				ISources.ACTIVE_EDITOR_INPUT_NAME, ISources.SHOW_IN_INPUT,
				ISources.SHOW_IN_SELECTION, ISources.ACTIVE_PART_NAME,
				ISources.ACTIVE_PART_ID_NAME, ISources.ACTIVE_SITE_NAME,
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_CURRENT_SELECTION_NAME }));
		context.runAndTrack(ratUpdater);
	}

	private void contextEvaluate() {
		evaluate = !evaluate;
		context.set(RE_EVAL, Boolean.valueOf(evaluate));
	}

	protected final void changeVariable(final String name, final Object value) {
		if (name == null || variableFilter.contains(name)) {
			return;
		}
		if (value == null) {
			legacyContext.removeVariable(name);
		} else {
			legacyContext.addVariable(name, value);
		}
	}
	@Override
	public void addSourceProvider(ISourceProvider provider) {
		sourceProviders.add(provider);
		provider.addSourceProviderListener(contextUpdater);
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
					&& (!ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME.equals(variableName))) {
				changeVariable(variableName, variableValue);
			}
		}
		contextEvaluate();
	}

	@Override
	public void removeSourceProvider(ISourceProvider provider) {
		provider.removeSourceProviderListener(contextUpdater);
		sourceProviders.remove(provider);

		final Map currentState = provider.getCurrentState();
		final Iterator variableItr = currentState.entrySet().iterator();
		while (variableItr.hasNext()) {
			final Map.Entry entry = (Map.Entry) variableItr.next();
			final String variableName = (String) entry.getKey();
			changeVariable(variableName, null);
		}
		contextEvaluate();
	}

	@Override
	public void dispose() {
		for (EvaluationReference ref : refs) {
			invalidate(ref, false);
		}
		refs.clear();
		serviceListeners.clear();
	}

	@Override
	public void addServiceListener(IPropertyChangeListener listener) {
		serviceListeners.add(listener);
	}

	@Override
	public void removeServiceListener(IPropertyChangeListener listener) {
		serviceListeners.remove(listener);
	}

	@Override
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property) {
		EvaluationReference ref = new EvaluationReference(ratContext, expression, listener,
				property);
		addEvaluationReference(ref);
		return ref;
	}

	@Override
	public void addEvaluationReference(IEvaluationReference ref) {
		EvaluationReference eref = (EvaluationReference) ref;
		refs.add(eref);
		boolean changed = false;
		if (eref.getExpression() != null) {
			ExpressionInfo info = new ExpressionInfo();
			eref.getExpression().collectExpressionInfo(info);
			for (String varName : info.getAccessedVariableNames()) {
				if (ratVariables.add(varName)) {
					changed = true;
				}
			}

			if (info.hasDefaultVariableAccess()
					&& ratVariables.add(IServiceConstants.ACTIVE_SELECTION)) {
				changed = true;
			}
		}
		if (changed) {
			contextEvaluate();
		}
		eref.participating = true;
		ratContext.runAndTrack(eref);
	}

	private void invalidate(IEvaluationReference ref, boolean remove) {
		if (remove) {
			refs.remove(ref);
		}
		EvaluationReference eref = (EvaluationReference) ref;
		eref.participating = false;
		eref.evaluate();
		eref.hasRun = false;
		contextEvaluate();
	}

	@Override
	public void removeEvaluationListener(IEvaluationReference ref) {
		invalidate(ref, true);
	}

	@Override
	public IEvaluationContext getCurrentState() {
		return legacyContext;
	}

	@Override
	public void requestEvaluation(String propertyName) {
		// Trigger evaluation of properties via context
		String pokeVar = propertyName + ".evaluationServiceLink"; //$NON-NLS-1$
		context.remove(pokeVar);
		context.set(pokeVar, "link"); //$NON-NLS-1$

		String[] sourceNames = new String[] { propertyName };
		startSourceChange(sourceNames);
		for (EvaluationReference ref : refs) {
			Expression expr = ref.getExpression();
			if (expr != null) {
				boolean evaluated = false;
				ExpressionInfo info = expr.computeExpressionInfo();
				String[] names = info.getAccessedPropertyNames();
				for (String name : names) {
					if (propertyName.equals(name)) {
						evaluated = true;
						ref.evaluate();
						break;
					}
				}
				if (!evaluated) {
					names = info.getAccessedVariableNames();
					for (String name : names) {
						if (propertyName.equals(name)) {
							evaluated = true;
							ref.evaluate();
							break;
						}
					}
				}
			}
		}
		endSourceChange(sourceNames);
		eventBroker.post(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
	}

	/**
	 * @param sourceNames
	 */
	private void startSourceChange(final String[] sourceNames) {
		notifying++;
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.FALSE, Boolean.TRUE);
		}
	}

	/**
	 * @param sourceNames
	 */
	private void endSourceChange(final String[] sourceNames) {
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.TRUE, Boolean.FALSE);
		}
		notifying--;
	}

	private void fireServiceChange(final String property, final Object oldValue,
			final Object newValue) {
		for (final IPropertyChangeListener listener : serviceListeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					WorkbenchPlugin.log(exception);
				}

				@Override
				public void run() throws Exception {
					listener.propertyChange(new PropertyChangeEvent(EvaluationService.this,
							property, oldValue, newValue));
				}
			});
		}
	}

	IEventBroker getEventBroker() {
		if (eventBroker == null) {
			eventBroker = context.get(IEventBroker.class);
		}
		return eventBroker;
	}
}
