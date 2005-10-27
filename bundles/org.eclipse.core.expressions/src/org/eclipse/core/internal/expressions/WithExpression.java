/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.ExpressionInfo;

public class WithExpression extends CompositeExpression {

	private String fVariable;
	private static final String ATT_VARIABLE= "variable";  //$NON-NLS-1$
	
	public WithExpression(IConfigurationElement configElement) throws CoreException {
		fVariable= configElement.getAttribute(ATT_VARIABLE);
		Expressions.checkAttribute(ATT_VARIABLE, fVariable);
	}
	
	public WithExpression(String variable) {
		fVariable= variable;
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object variable= context.getVariable(fVariable);
		if (variable == null) {
			throw new CoreException(new ExpressionStatus(
				ExpressionStatus.VARIABLE_NOT_DEFINED,
				Messages.format(ExpressionMessages.WithExpression_variable_not_defined, fVariable))); 
		}
		return evaluateAnd(new EvaluationContext(context, variable));
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		ExpressionInfo other= new ExpressionInfo();
		super.collectExpressionInfo(other);
		if (other.hasDefaultVariableAccess()) {
			info.addVariableNameAccess(fVariable);
		}
		info.mergeExceptDefaultVariable(other);
	}	
}