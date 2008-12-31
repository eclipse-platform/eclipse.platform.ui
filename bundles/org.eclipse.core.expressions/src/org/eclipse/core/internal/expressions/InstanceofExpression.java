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

public class InstanceofExpression extends Expression {
	/**
	 * The seed for the hash code for all instance of expressions.
	 */
	private static final int HASH_INITIAL= InstanceofExpression.class.getName().hashCode();

	private String fTypeName;

	public InstanceofExpression(IConfigurationElement element) throws CoreException {
		fTypeName= element.getAttribute(ATT_VALUE);
		Expressions.checkAttribute(ATT_VALUE, fTypeName);
	}

	public InstanceofExpression(Element element) throws CoreException {
		fTypeName= element.getAttribute(ATT_VALUE);
		Expressions.checkAttribute(ATT_VALUE, fTypeName.length() > 0 ? fTypeName : null);
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

	public boolean equals(final Object object) {
		if (!(object instanceof InstanceofExpression))
			return false;

		final InstanceofExpression that= (InstanceofExpression) object;
		return this.fTypeName.equals(that.fTypeName);
	}

	protected int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + fTypeName.hashCode();
	}

	//---- Debugging ---------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "<instanceof value=\"" + fTypeName + "\"/>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
