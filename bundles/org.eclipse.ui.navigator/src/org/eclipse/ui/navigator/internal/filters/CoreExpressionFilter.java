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

package org.eclipse.ui.navigator.internal.filters;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * @since 3.2
 * 
 */
public class CoreExpressionFilter extends ViewerFilter {

	private Expression filterExpression;

	/**
	 * Creates a filter which hides all elements that match the given
	 * expression.
	 * 
	 * @param aFilterExpression
	 *            An expression to hide elements in the viewer.
	 */
	public CoreExpressionFilter(Expression aFilterExpression) {
		filterExpression = aFilterExpression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		try {
			return (filterExpression.evaluate(new EvaluationContext(null,
					element)) != EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}
		return true;
	}

}
