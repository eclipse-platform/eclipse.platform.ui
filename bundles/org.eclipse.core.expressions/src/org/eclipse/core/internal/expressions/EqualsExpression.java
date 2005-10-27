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
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.ExpressionInfo;

public class EqualsExpression extends Expression {

	private Object fExpectedValue; 
	
	public EqualsExpression(Object expectedValue) {
		Assert.isNotNull(expectedValue);
		fExpectedValue= expectedValue;
	}
	
	public EqualsExpression(IConfigurationElement element) throws CoreException {
		String value= element.getAttribute(ATT_VALUE);
		Expressions.checkAttribute(ATT_VALUE, value);
		fExpectedValue= Expressions.convertArgument(value);
	}
	
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object element= context.getDefaultVariable();
		return EvaluationResult.valueOf(element.equals(fExpectedValue));
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		info.markDefaultVariableAccessed();
	}
}