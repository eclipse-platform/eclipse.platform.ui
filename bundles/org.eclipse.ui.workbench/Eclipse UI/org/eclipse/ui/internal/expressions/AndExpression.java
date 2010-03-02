/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.expressions;

import java.util.Iterator;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Copied from org.eclipse.core.internal.expressions.
 */
public final class AndExpression extends CompositeExpression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = AndExpression.class.getName()
			.hashCode();

	protected final int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(fExpressions);
	}

	public final boolean equals(final Object object) {
		if (object instanceof AndExpression) {
			final AndExpression that = (AndExpression) object;
			return equals(this.fExpressions, that.fExpressions);
		}

		return false;
	}

	public final EvaluationResult evaluate(final IEvaluationContext context)
			throws CoreException {
		return evaluateAnd(context);
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("AndExpression("); //$NON-NLS-1$
		if (fExpressions != null) {
			final Iterator itr = fExpressions.iterator();
			while (itr.hasNext()) {
				final Expression expression = (Expression) itr.next();
				buffer.append(expression.toString());
				if (itr.hasNext()) {
					buffer.append(',');
				}
			}
		}
		buffer.append(')');
		return buffer.toString();
	}
}
