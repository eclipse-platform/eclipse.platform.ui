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

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.ExpressionInfo;


public class CountExpression extends Expression {

	private static final int ANY_NUMBER=	5;
	private static final int EXACT=			4;
	private static final int ONE_OR_MORE=	3;
	private static final int NONE_OR_ONE= 	2;
	private static final int NONE= 			1;
	private static final int UNKNOWN= 		0;
	
	private int fMode;
	private int fSize;
	
	public CountExpression(IConfigurationElement configElement) {
		String size = configElement.getAttribute(ATT_VALUE);
		initializeSize(size);
	}
	
	public CountExpression(String size) {
		initializeSize(size);
	}
	
	private void initializeSize(String size) {
		if (size == null)
			size= "*"; //$NON-NLS-1$
		if (size.equals("*")) //$NON-NLS-1$
			fMode= ANY_NUMBER;
		else if (size.equals("?")) //$NON-NLS-1$
			fMode= NONE_OR_ONE;
		else if (size.equals("!")) //$NON-NLS-1$
			fMode= NONE;
		else if (size.equals("+")) //$NON-NLS-1$
			fMode= ONE_OR_MORE;
		else {
			try {
				fSize= Integer.parseInt(size);
				fMode= EXACT;
			} catch (NumberFormatException e) {
				fMode= UNKNOWN;
			}
		}
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		Object var= context.getDefaultVariable();
		Expressions.checkCollection(var, this);
		Collection collection= (Collection)var;
		int size= collection.size();
		switch (fMode) {
			case UNKNOWN:
				return EvaluationResult.FALSE;
			case NONE:
				return EvaluationResult.valueOf(size == 0);
			case NONE_OR_ONE:
				return EvaluationResult.valueOf(size == 0 || size == 1);
			case ONE_OR_MORE:
				return EvaluationResult.valueOf(size >= 1);
			case EXACT:
				return EvaluationResult.valueOf(fSize == size);
			case ANY_NUMBER:
				return EvaluationResult.TRUE;
		}
		return EvaluationResult.FALSE;
	}

	public void collectExpressionInfo(ExpressionInfo info) {
		info.markDefaultVariableAccessed();
	}
}