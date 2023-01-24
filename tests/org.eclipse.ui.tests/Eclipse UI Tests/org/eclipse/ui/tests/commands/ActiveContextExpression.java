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
 *******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.util.Collection;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.expressions.ActivePartExpression;

public class ActiveContextExpression extends Expression {
	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = ActivePartExpression.class
			.getName().hashCode();

	private String contextId;

	private String[] expressionInfo;

	public ActiveContextExpression(String id, String[] info) {
		contextId = id;
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
				.getVariable(ISources.ACTIVE_CONTEXT_NAME);
		if (variable != null) {
			if (((Collection<?>) variable).contains(contextId)) {
				return EvaluationResult.TRUE;
			}
		}
		return EvaluationResult.FALSE;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ActiveContextExpression ace) {
			return equals(contextId, ace.contextId);
		}
		return false;
	}

	@Override
	protected final int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(contextId);
	}
}
