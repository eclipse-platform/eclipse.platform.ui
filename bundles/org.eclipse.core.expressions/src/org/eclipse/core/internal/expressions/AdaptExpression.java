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
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.ExpressionInfo;

public class AdaptExpression extends CompositeExpression {

	private static final String ATT_TYPE= "type"; //$NON-NLS-1$
	
	private String fTypeName;
	
	public AdaptExpression(IConfigurationElement configElement) throws CoreException {
		fTypeName= configElement.getAttribute(ATT_TYPE);
		Expressions.checkAttribute(ATT_TYPE, fTypeName);
	}
	
	public AdaptExpression(String typeName) {
		Assert.isNotNull(typeName);
		fTypeName= typeName;
	}
	
	/* (non-Javadoc)
	 * @see Expression#evaluate(IVariablePool)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		if (fTypeName == null)
			return EvaluationResult.FALSE;
		Object var= context.getDefaultVariable();
		Object adapted= null;
		if (Expressions.isInstanceOf(var, fTypeName)) {
			adapted= var;
		} else {
			IAdapterManager manager= Platform.getAdapterManager();
			if (!manager.hasAdapter(var, fTypeName))
				return EvaluationResult.FALSE;
		
			adapted= manager.getAdapter(var, fTypeName);
		}
		// the adapted result is null but hasAdapter returned true. This means
		// that there is an adapter but the adapter isn't loaded yet.
		if (adapted == null) 
			return EvaluationResult.NOT_LOADED;
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
