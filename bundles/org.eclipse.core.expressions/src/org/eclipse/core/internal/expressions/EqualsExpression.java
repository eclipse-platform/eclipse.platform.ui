/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.w3c.dom.Element;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class EqualsExpression extends Expression {
	/**
	 * The seed for the hash code for all equals expressions.
	 */
	private static final int HASH_INITIAL= EqualsExpression.class.getName().hashCode();

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

	public EqualsExpression(Element element) throws CoreException {
		String value= element.getAttribute(ATT_VALUE);
		Expressions.checkAttribute(ATT_VALUE, value.length() > 0 ? value : null);
		fExpectedValue= Expressions.convertArgument(value);
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object element= context.getDefaultVariable();
		return EvaluationResult.valueOf(element.equals(fExpectedValue));
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		info.markDefaultVariableAccessed();
	}

	public boolean equals(final Object object) {
		if (!(object instanceof EqualsExpression))
			return false;

		final EqualsExpression that= (EqualsExpression)object;
		return this.fExpectedValue.equals(that.fExpectedValue);
	}

	protected int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + fExpectedValue.hashCode();
	}
}
