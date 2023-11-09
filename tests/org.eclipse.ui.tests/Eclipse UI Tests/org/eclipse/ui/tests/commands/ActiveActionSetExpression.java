/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	private final String actionSetId;

	private final String[] expressionInfo;

	public ActiveActionSetExpression(String id) {
		this(id, new String[] { ISources.ACTIVE_ACTION_SETS_NAME });
	}

	public ActiveActionSetExpression(String id, String[] info) {
		actionSetId = id;
		expressionInfo = info;
	}

	@Override
	public void collectExpressionInfo(ExpressionInfo info) {
		for (String element : expressionInfo) {
			info.addVariableNameAccess(element);
		}
	}

	@Override
	public EvaluationResult evaluate(IEvaluationContext context) {
		final Object variable = context
				.getVariable(ISources.ACTIVE_ACTION_SETS_NAME);
		if (variable != null) {
			if (((Collection<?>) variable).contains(actionSetId)) {
				return EvaluationResult.TRUE;
			}
		}
		return EvaluationResult.FALSE;
	}

}