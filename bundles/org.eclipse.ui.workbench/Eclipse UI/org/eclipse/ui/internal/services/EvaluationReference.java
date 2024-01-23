/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.services.IEvaluationReference;

/**
 * @since 3.3
 */
public class EvaluationReference extends RunAndTrack implements IEvaluationReference {
	private static final int THROTTLE_MAX = 100;
	private static final int THROTTLE_VALUE = 3;
	final IEclipseContext context;
	final Expression expression;
	final IPropertyChangeListener listener;
	final String property;
	final int sourcePriority;
	boolean cache;
	boolean participating = true;
	boolean postingChanges = true;
	boolean hasRun = false;
	static LogThrottle logThrottle = new LogThrottle(THROTTLE_MAX, THROTTLE_VALUE);

	public EvaluationReference(IEclipseContext context, Expression expression, IPropertyChangeListener listener,
			String property) {
		this.context = context;
		this.expression = expression;
		this.listener = listener;
		this.property = property;
		this.sourcePriority = SourcePriorityNameMapping.computeSourcePriority(expression);
	}

	@Override
	public void clearResult() {
	}

	@Override
	public Expression getExpression() {
		return expression;
	}

	@Override
	public int getSourcePriority() {
		return sourcePriority;
	}

	@Override
	public boolean evaluate(IEvaluationContext context) {
		if (expression == null) {
			cache = true;
		} else {
			try {
				cache = expression.evaluate(context) != EvaluationResult.FALSE;
			} catch (CoreException e) {
				String message = "Failed to evaluate: " + expression; //$NON-NLS-1$
				boolean logged = logThrottle.error(message, e);
				if (!logged && Policy.DEBUG_CMDS) {
					Activator.trace(Policy.DEBUG_CMDS_FLAG, message, e);
				}
				return false;
			}
		}
		return cache;
	}

	@Override
	public void setResult(boolean result) {
		cache = result;
	}

	@Override
	public boolean changed(IEclipseContext context) {
		if (!participating) {
			return false;
		}

		evaluate();
		return participating;
	}

	public void evaluate() {
		boolean value = cache;
		evaluate(new ExpressionContext(context));
		if (!postingChanges) {
			return;
		}
		if (!hasRun) {
			getListener().propertyChange(new PropertyChangeEvent(this, getProperty(), null, Boolean.valueOf(cache)));
		} else if (!participating) {
			getListener().propertyChange(new PropertyChangeEvent(this, getProperty(), Boolean.valueOf(value), null));
		}
		if (value != cache) {
			getListener().propertyChange(
					new PropertyChangeEvent(this, getProperty(), Boolean.valueOf(value), Boolean.valueOf(cache)));
		}
		hasRun = true;
	}

	@Override
	public IPropertyChangeListener getListener() {
		return listener;
	}

	@Override
	public String getProperty() {
		return property;
	}

	public void setPostingChanges(boolean b) {
		postingChanges = b;
	}

	public boolean isPostingChanges() {
		return postingChanges;
	}
}
