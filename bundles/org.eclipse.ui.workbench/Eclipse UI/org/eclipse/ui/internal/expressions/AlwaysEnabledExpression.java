/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.Expression#evaluate(org.eclipse.core.expressions.IEvaluationContext)
	 */
	public EvaluationResult evaluate(IEvaluationContext context) {
		return EvaluationResult.TRUE;
	}
}
