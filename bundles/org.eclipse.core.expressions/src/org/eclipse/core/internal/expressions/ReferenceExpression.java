/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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

/**
 * This class makes use of the <b>org.eclipse.core.expressions.definitions</b>
 * extension point to evaluate the current context against pre-defined
 * expressions. It provides core expression re-use.
 *
 * @since 3.3
 */
public class ReferenceExpression extends Expression {

	// consider making this a more general extension manager
	// for now it's just part of the reference expression
	private static DefinitionRegistry fgDefinitionRegistry= null;

	private static DefinitionRegistry getDefinitionRegistry() {
		if (fgDefinitionRegistry == null) {
			fgDefinitionRegistry= new DefinitionRegistry();
		}
		return fgDefinitionRegistry;
	}

	private static final String ATT_DEFINITION_ID= "definitionId"; //$NON-NLS-1$

	/**
	 * The seed for the hash code for all equals expressions.
	 */
	private static final int HASH_INITIAL= ReferenceExpression.class.getName().hashCode();

	private String fDefinitionId;

	public ReferenceExpression(String definitionId) {
		Assert.isNotNull(definitionId);
		fDefinitionId= definitionId;
	}

	public ReferenceExpression(IConfigurationElement element) throws CoreException {
		fDefinitionId= element.getAttribute(ATT_DEFINITION_ID);
		Expressions.checkAttribute(ATT_DEFINITION_ID, fDefinitionId);
	}

	public ReferenceExpression(Element element) throws CoreException {
		fDefinitionId= element.getAttribute(ATT_DEFINITION_ID);
		Expressions.checkAttribute(ATT_DEFINITION_ID, fDefinitionId.length() > 0 ? fDefinitionId : null);
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Expression expr= getDefinitionRegistry().getExpression(fDefinitionId);
		return expr.evaluate(context);
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		Expression expr;
		try {
			expr= getDefinitionRegistry().getExpression(fDefinitionId);
		} catch (CoreException e) {
			// We didn't find the expression definition. So no
			// expression info can be collected.
			return;
		}
		expr.collectExpressionInfo(info);
	}

	public boolean equals(final Object object) {
		if (!(object instanceof ReferenceExpression))
			return false;

		final ReferenceExpression that= (ReferenceExpression)object;
		return this.fDefinitionId.equals(that.fDefinitionId);
	}

	protected int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + fDefinitionId.hashCode();
	}
}