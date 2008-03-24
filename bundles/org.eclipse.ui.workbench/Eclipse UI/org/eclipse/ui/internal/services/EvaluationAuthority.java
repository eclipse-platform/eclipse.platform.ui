/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.util.Tracing;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.services.IEvaluationReference;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.3
 * 
 */
public class EvaluationAuthority extends ExpressionAuthority {

	/**
	 * 
	 */
	private static final String COMPONENT = "EVALUATION"; //$NON-NLS-1$

	/**
	 * A bucket sort of the evaluation references based on source priority. Each
	 * reference will appear only once per set, but may appear in multiple sets.
	 * If no references are defined for a particular priority level, then the
	 * array at that index will only contain <code>null</code>.
	 */
	private final Map cachesBySourceName = new HashMap();
	private ListenerList serviceListeners = new ListenerList();
	private int notifying = 0;

	// private final Map cachesByExpression = new HashMap();

	public void addEvaluationListener(IEvaluationReference ref) {
		// we update the source priority bucket sort of activations.
		String[] sourceNames = getNames(ref);
		for (int i = 0; i < sourceNames.length; i++) {
			Map cachesByExpression = (HashMap) cachesBySourceName
					.get(sourceNames[i]);
			if (cachesByExpression == null) {
				cachesByExpression = new HashMap(1);
				cachesBySourceName.put(sourceNames[i], cachesByExpression);
			}
			final Expression expression = ref.getExpression();
			Set caches = (Set) cachesByExpression.get(expression);
			if (caches == null) {
				caches = new HashSet();
				cachesByExpression.put(expression, caches);
			}
			caches.add(ref);
		}

		boolean result = evaluate(ref);
		firePropertyChange(ref, null, valueOf(result));
	}

	private Boolean valueOf(boolean result) {
		return result ? Boolean.TRUE : Boolean.FALSE;
	}

	private String[] getNames(IEvaluationReference ref) {
		ExpressionInfo info = new ExpressionInfo();
		ref.getExpression().collectExpressionInfo(info);
		ArrayList allNames = new ArrayList(Arrays.asList(info
				.getAccessedVariableNames()));
		if (info.hasDefaultVariableAccess()) {
			allNames.add(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		}
		allNames.addAll(Arrays.asList(info.getAccessedPropertyNames()));
		return (String[]) allNames.toArray(new String[allNames.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.ExpressionAuthority#sourceChanged(int)
	 */
	protected void sourceChanged(int sourcePriority) {
		// no-op, we want the other one
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.ExpressionAuthority#sourceChanged(java.lang.String[])
	 */
	protected void sourceChanged(String[] sourceNames) {
		startSourceChange(sourceNames);
		try {
			// evaluations to recompute
			for (int i = 0; i < sourceNames.length; i++) {
				Map cachesByExpression = (HashMap) cachesBySourceName
						.get(sourceNames[i]);
				if (cachesByExpression != null) {
					Collection v = cachesByExpression.values();
					Set[] expressionCaches = (Set[]) v
							.toArray(new Set[v.size()]);
					for (int j = 0; j < expressionCaches.length; j++) {
						if (expressionCaches[j].size() > 0) {
							EvaluationReference[] refs = (EvaluationReference[]) expressionCaches[j]
									.toArray(new EvaluationReference[expressionCaches[j]
											.size()]);
							refsWithSameExpression(refs);
						}
					}
				}
			}
		} finally {
			endSourceChange(sourceNames);
		}
	}

	/**
	 * This will evaluate all refs with the same expression.
	 * 
	 * @param refs
	 */
	private void refsWithSameExpression(EvaluationReference[] refs) {
		int k = 0;
		while (k < refs.length && !refs[k].isPostingChanges()) {
			k++;
		}
		if (k >= refs.length) {
			return;
		}
		EvaluationReference ref = refs[k];
		boolean oldValue = evaluate(ref);
		ref.clearResult();
		final boolean newValue = evaluate(ref);
		if (oldValue != newValue) {
			firePropertyChange(ref, valueOf(oldValue), valueOf(newValue));
		}
		for (k++; k < refs.length; k++) {
			ref = refs[k];
			// this is not as expensive as it looks
			if (ref.isPostingChanges()) {
				oldValue = evaluate(ref);
				if (oldValue != newValue) {
					ref.setResult(newValue);
					firePropertyChange(ref, valueOf(oldValue),
							valueOf(newValue));
				}
			}
		}
	}

	/**
	 * @param sourceNames
	 */
	private void startSourceChange(final String[] sourceNames) {
		if (Policy.DEBUG_SOURCES) {
			Tracing.printTrace(COMPONENT, "start source changed: " //$NON-NLS-1$
					+ Arrays.asList(sourceNames));
		}
		notifying++;
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.FALSE,
					Boolean.TRUE);
		}
	}

	/**
	 * @param sourceNames
	 */
	private void endSourceChange(final String[] sourceNames) {
		if (Policy.DEBUG_SOURCES) {
			Tracing.printTrace(COMPONENT, "end source changed: " //$NON-NLS-1$
					+ Arrays.asList(sourceNames));
		}
		if (notifying == 1) {
			fireServiceChange(IEvaluationService.PROP_NOTIFYING, Boolean.TRUE,
					Boolean.FALSE);
		}
		notifying--;
	}

	/**
	 * @param ref
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		// Next we update the source priority bucket sort of activations.
		String[] sourceNames = getNames(ref);
		for (int i = 0; i < sourceNames.length; i++) {
			Map cachesByExpression = (HashMap) cachesBySourceName
					.get(sourceNames[i]);
			if (cachesByExpression != null) {
				Set caches = (Set) cachesByExpression.get(ref.getExpression());
				if (caches != null) {
					caches.remove(ref);
					if (caches.isEmpty()) {
						cachesByExpression.remove(ref.getExpression());
					}
				}
				if (cachesByExpression.isEmpty()) {
					cachesBySourceName.remove(sourceNames[i]);
				}
			}
		}
		boolean result = evaluate(ref);
		firePropertyChange(ref, valueOf(result), null);
	}

	/**
	 * @param ref
	 * @param oldValue
	 * @param newValue
	 */
	private void firePropertyChange(IEvaluationReference ref, Object oldValue,
			Object newValue) {
		ref.getListener().propertyChange(
				new PropertyChangeEvent(ref, ref.getProperty(), oldValue,
						newValue));
	}

	private void fireServiceChange(final String property,
			final Object oldValue, final Object newValue) {
		Object[] listeners = serviceListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					WorkbenchPlugin.log(exception);
				}

				public void run() throws Exception {
					listener.propertyChange(new PropertyChangeEvent(
							EvaluationAuthority.this, property, oldValue,
							newValue));
				}
			});
		}
	}

	/**
	 * @param listener
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		serviceListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		serviceListeners.remove(listener);
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
	final void updateShellKludge() {
		updateCurrentState();
		sourceChanged(new String[] { ISources.ACTIVE_SHELL_NAME });
	}

	/**
	 * Returns the currently active shell.
	 * 
	 * @return The currently active shell; may be <code>null</code>.
	 */
	final Shell getActiveShell() {
		return (Shell) getVariable(ISources.ACTIVE_SHELL_NAME);
	}

}
