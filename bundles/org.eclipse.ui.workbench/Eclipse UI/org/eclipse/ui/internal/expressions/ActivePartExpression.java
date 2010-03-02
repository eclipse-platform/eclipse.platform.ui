/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.expressions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;

/**
 * <p>
 * An expression that is bound to a particular part instance.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class ActivePartExpression extends Expression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = ActivePartExpression.class
			.getName().hashCode();

	/**
	 * The part that must be active for this expression to evaluate to
	 * <code>true</code>. This value is never <code>null</code>.
	 */
	private final IWorkbenchPart activePart;

	/**
	 * Constructs a new instance of <code>ActivePartExpression</code>
	 * 
	 * @param activePart
	 *            The part to match with the active part; may be
	 *            <code>null</code>
	 */
	public ActivePartExpression(final IWorkbenchPart activePart) {
		if (activePart == null) {
			throw new NullPointerException("The active part must not be null"); //$NON-NLS-1$
		}
		this.activePart = activePart;
	}

	public final void collectExpressionInfo(final ExpressionInfo info) {
		info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
	}

	protected final int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(activePart);
	}

	public final boolean equals(final Object object) {
		if (object instanceof ActivePartExpression) {
			final ActivePartExpression that = (ActivePartExpression) object;
			return equals(this.activePart, that.activePart);
		}

		return false;
	}

	public final EvaluationResult evaluate(final IEvaluationContext context) {
		final Object variable = context.getVariable(ISources.ACTIVE_PART_NAME);
		if (equals(activePart, variable)) {
			return EvaluationResult.TRUE;
		}
		return EvaluationResult.FALSE;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ActivePartExpression("); //$NON-NLS-1$
		buffer.append(activePart);
		buffer.append(')');
		return buffer.toString();
	}
}
