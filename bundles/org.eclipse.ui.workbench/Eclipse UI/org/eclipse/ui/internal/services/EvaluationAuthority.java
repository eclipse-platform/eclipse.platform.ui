/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @since 3.3
 * 
 */
public class EvaluationAuthority extends ExpressionAuthority {

	/**
	 * A bucket sort of the evaluation references based on source priority. Each
	 * reference will appear only once per set, but may appear in multiple sets.
	 * If no references are defined for a particular priority level, then the
	 * array at that index will only contain <code>null</code>.
	 */
	private final Map cachesBySourceName = new HashMap();

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
		firePropertyChange(ref, null, new Boolean(result));
	}

	/**
	 * @param ref
	 * @return
	 */
	private String[] getNames(IEvaluationReference ref) {
		ExpressionInfo info = new ExpressionInfo();
		ref.getExpression().collectExpressionInfo(info);
		return info.getAccessedVariableNames();
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
		// evaluations to recompute
		for (int i = 0; i < sourceNames.length; i++) {
			Map cachesByExpression = (HashMap) cachesBySourceName
					.get(sourceNames[i]);
			if (cachesByExpression != null) {
				Iterator expressionCaches = cachesByExpression.values()
						.iterator();
				while (expressionCaches.hasNext()) {
					Set caches = (Set) expressionCaches.next();
					Iterator evalutionCache = caches.iterator();
					if (evalutionCache.hasNext()) {
						IEvaluationReference ref = (IEvaluationReference) evalutionCache
								.next();
						boolean oldValue = evaluate(ref);
						ref.clearResult();
						final boolean newValue = evaluate(ref);
						if (oldValue != newValue) {
							firePropertyChange(ref, new Boolean(oldValue),
									new Boolean(newValue));
						}
						while (evalutionCache.hasNext()) {
							ref = (IEvaluationReference) evalutionCache.next();
							// this is not as expensive as it looks
							oldValue = evaluate(ref);
							if (oldValue != newValue) {
								ref.setResult(newValue);
								firePropertyChange(ref, new Boolean(oldValue),
										new Boolean(newValue));
							}
						}
					}
				}
			}
		}
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
		firePropertyChange(ref, new Boolean(result), null);
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
}
