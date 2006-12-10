/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.util.Collection;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;

public class ActiveActionSetExpression extends Expression {
	private String actionSetId;

	private String[] expressionInfo;

	public ActiveActionSetExpression(String id) {
		this(id, new String[] { ISources.ACTIVE_ACTION_SETS_NAME });
	}

	public ActiveActionSetExpression(String id, String[] info) {
		actionSetId = id;
		expressionInfo = info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.Expression#collectExpressionInfo(org.eclipse.core.expressions.ExpressionInfo)
	 */
	public void collectExpressionInfo(ExpressionInfo info) {
		for (int i = 0; i < expressionInfo.length; i++) {
			info.addVariableNameAccess(expressionInfo[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.Expression#evaluate(org.eclipse.core.expressions.IEvaluationContext)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) {
		final Object variable = context
				.getVariable(ISources.ACTIVE_ACTION_SETS_NAME);
		if (variable != null) {
			if (((Collection) variable).contains(actionSetId)) {
				return EvaluationResult.TRUE;
			}
		}
		return EvaluationResult.FALSE;
	}

}