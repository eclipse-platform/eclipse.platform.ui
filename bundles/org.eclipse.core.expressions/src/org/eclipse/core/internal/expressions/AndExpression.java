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

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.IEvaluationContext;

import org.eclipse.core.runtime.CoreException;

public class AndExpression extends CompositeExpression {

	public boolean equals(final Object object) {
		if (!(object instanceof AndExpression))
			return false;

		final AndExpression that= (AndExpression)object;
		return equals(this.fExpressions, that.fExpressions);
	}

	public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
		return evaluateAnd(context);
	}
}
