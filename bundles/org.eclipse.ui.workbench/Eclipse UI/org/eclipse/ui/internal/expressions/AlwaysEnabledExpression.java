/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.expressions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;

/**
 * An expression that simply returns <code>true</code> at all times. A shared
 * instance of this expression is provided.
 *
 * @since 3.3
 *
 */
public final class AlwaysEnabledExpression extends Expression {

	public static final AlwaysEnabledExpression INSTANCE = new AlwaysEnabledExpression();

	/**
	 * Not to be instantiated
	 */
	private AlwaysEnabledExpression() {
	}

	@Override
	public EvaluationResult evaluate(IEvaluationContext context) {
		return EvaluationResult.TRUE;
	}
}
