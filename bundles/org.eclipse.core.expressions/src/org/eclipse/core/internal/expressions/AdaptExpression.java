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
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class AdaptExpression extends CompositeExpression {

	private static final String ATT_TYPE= "type"; //$NON-NLS-1$

	/**
	 * The seed for the hash code for all adapt expressions.
	 */
	private static final int HASH_INITIAL= AdaptExpression.class.getName().hashCode();

	private String fTypeName;

	public AdaptExpression(IConfigurationElement configElement) throws CoreException {
		fTypeName= configElement.getAttribute(ATT_TYPE);
		Expressions.checkAttribute(ATT_TYPE, fTypeName);
	}

	public AdaptExpression(Element element) throws CoreException {
		fTypeName= element.getAttribute(ATT_TYPE);
		Expressions.checkAttribute(ATT_TYPE, fTypeName.length() > 0 ? fTypeName : null);
	}

	public AdaptExpression(String typeName) {
		Assert.isNotNull(typeName);
		fTypeName= typeName;
	}

	public boolean equals(final Object object) {
		if (!(object instanceof AdaptExpression))
			return false;

		final AdaptExpression that= (AdaptExpression)object;
		return this.fTypeName.equals(that.fTypeName)
				&& equals(this.fExpressions, that.fExpressions);
	}

	protected int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(fExpressions)
			* HASH_FACTOR + fTypeName.hashCode();
	}

	/* (non-Javadoc)
	 * @see Expression#evaluate(IVariablePool)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		if (fTypeName == null)
			return EvaluationResult.FALSE;
		Object var= context.getDefaultVariable();
		Object adapted= null;
		IAdapterManager manager= Platform.getAdapterManager();
		if (Expressions.isInstanceOf(var, fTypeName)) {
			adapted= var;
		} else {
			if (!manager.hasAdapter(var, fTypeName))
				return EvaluationResult.FALSE;

			adapted= manager.getAdapter(var, fTypeName);
		}
		// the adapted result is null but hasAdapter returned true check
		// if the adapter is loaded.
		if (adapted == null) {
			if (manager.queryAdapter(var, fTypeName) == IAdapterManager.NOT_LOADED) {
				return EvaluationResult.NOT_LOADED;
			} else {
				return EvaluationResult.FALSE;
			}
		}
		return evaluateAnd(new DefaultVariable(context, adapted));
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		// Although the default variable is passed to the children of this
		// expression as an instance of the adapted type it is OK to only
		// mark a default variable access.
		info.markDefaultVariableAccessed();
		super.collectExpressionInfo(info);
	}
}
