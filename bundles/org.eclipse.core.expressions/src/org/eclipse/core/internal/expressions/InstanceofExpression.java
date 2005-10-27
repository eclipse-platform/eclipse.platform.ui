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

public class InstanceofExpression extends Expression {

	private String fTypeName;
	
	public InstanceofExpression(IConfigurationElement element) throws CoreException {
		fTypeName= element.getAttribute(ATT_VALUE);
		Expressions.checkAttribute(ATT_VALUE, fTypeName);
	}
	
	public InstanceofExpression(String typeName) {
		Assert.isNotNull(typeName);
		fTypeName= typeName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.refactoring.participants.Expression#evaluate(java.lang.Object)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) {
		Object element= context.getDefaultVariable();
		return EvaluationResult.valueOf(Expressions.isInstanceOf(element, fTypeName));
	}
	
	public void collectExpressionInfo(ExpressionInfo info) {
		info.markDefaultVariableAccessed();
	}
	
	//---- Debugging ---------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "<instanceof value=\"" + fTypeName + "\"/>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}