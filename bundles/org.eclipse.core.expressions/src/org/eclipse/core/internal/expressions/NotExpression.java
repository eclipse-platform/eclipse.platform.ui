/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.ExpressionInfo;

public class NotExpression extends Expression {

	private Expression fExpression;

	public NotExpression(Expression expression) {
		Assert.isNotNull(expression);
		fExpression= expression;
	}
	
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		return fExpression.evaluate(context).not();
	}
	
	public void collectExpressionInfo(ExpressionInfo info) {
		fExpression.collectExpressionInfo(info);
	}
}