/*******************************************************************************
 * Copyright (c) 2025 Eclipse Platform and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Platform - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.addons;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ReferenceExpression;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.IHandlerWithExpression;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MExpression;

/**
 * A wrapper that associates a handler with its enabledWhen expression from the model.
 * This allows expression-based enablement to be checked in addition to @CanExecute.
 *
 * @since 1.4
 */
public class HandlerEnabledWhenWrapper implements IHandlerWithExpression {
	private final Object handler;
	private final MHandler handlerModel;

	public HandlerEnabledWhenWrapper(Object handler, MHandler handlerModel) {
		this.handler = handler;
		this.handlerModel = handlerModel;
	}

	@Override
	public Object getHandler() {
		return handler;
	}

	public MHandler getHandlerModel() {
		return handlerModel;
	}

	@Override
	public boolean evaluateEnabledWhen(IEclipseContext context) {
		MExpression enabledWhen = handlerModel.getEnabledWhen();
		if (enabledWhen == null) {
			return true;
		}

		if (enabledWhen instanceof MCoreExpression coreExpression) {
			return evaluateCoreExpression(coreExpression, context);
		}

		// For other expression types, default to enabled
		return true;
	}

	private boolean evaluateCoreExpression(MCoreExpression coreExpression, IEclipseContext context) {
		Expression expr = null;
		if (coreExpression.getCoreExpression() instanceof Expression) {
			expr = (Expression) coreExpression.getCoreExpression();
		} else if (coreExpression.getCoreExpressionId() != null && !coreExpression.getCoreExpressionId().isEmpty()) {
			expr = new ReferenceExpression(coreExpression.getCoreExpressionId());
			coreExpression.setCoreExpression(expr);
		}

		if (expr == null) {
			return true;
		}

		try {
			ExpressionContext exprContext = new ExpressionContext(context);
			EvaluationResult result = expr.evaluate(exprContext);
			return result != EvaluationResult.FALSE;
		} catch (Exception e) {
			// Log error and default to disabled
			return false;
		}
	}
}
