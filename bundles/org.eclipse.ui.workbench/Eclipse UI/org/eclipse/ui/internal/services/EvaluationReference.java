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

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.services.IEvaluationReference;

/**
 * @since 3.3
 * 
 */
public class EvaluationReference extends RunAndTrack implements IEvaluationReference {
	final IEclipseContext context;
	final Expression expression;
	final IPropertyChangeListener listener;
	final String property;
	final int sourcePriority;
	boolean cache;
	boolean participating = true;
	boolean postingChanges = true;

	public EvaluationReference(IEclipseContext context, Expression expression,
			IPropertyChangeListener listener, String property) {
		this.context = context;
		this.expression = expression;
		this.listener = listener;
		this.property = property;
		this.sourcePriority = SourcePriorityNameMapping.computeSourcePriority(expression);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#clearResult()
	 */
	public void clearResult() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#getExpression()
	 */
	public Expression getExpression() {
		return expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#getSourcePriority
	 * ()
	 */
	public int getSourcePriority() {
		return sourcePriority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#evaluate(org.
	 * eclipse.core.expressions.IEvaluationContext)
	 */
	public boolean evaluate(IEvaluationContext context) {
		if (expression == null) {
			cache = true;
		} else {
			try {
				cache = expression.evaluate(context) != EvaluationResult.FALSE;
			} catch (CoreException e) {
				Activator.trace(Policy.DEBUG_CMDS, "Failed to calculate active", e); //$NON-NLS-1$
			}
		}
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IEvaluationResultCache#setResult(boolean
	 * )
	 */
	public void setResult(boolean result) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.core.services.context.IRunAndTrack#notify(org.eclipse.
	 * e4.core.services.context.ContextChangeEvent)
	 */
	public boolean changed(IEclipseContext context) {
		if (!participating) {
			return false;
		}

		evaluate();
		return participating;
	}

	public void evaluate() {
		boolean value = cache;
		evaluate(new LegacyEvalContext(context));
		if (!postingChanges) {
			return;
		}

		if (value != cache) {
			getListener().propertyChange(
					new PropertyChangeEvent(this, getProperty(), Boolean.valueOf(value), Boolean
							.valueOf(cache)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IEvaluationReference#getListener()
	 */
	public IPropertyChangeListener getListener() {
		return listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IEvaluationReference#getProperty()
	 */
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
