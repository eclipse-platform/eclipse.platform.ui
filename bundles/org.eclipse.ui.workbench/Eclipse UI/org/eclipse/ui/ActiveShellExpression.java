/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * An expression that checks the active shell variable. The variable name is
 * <code>ISources.ACTIVE_SHELL_NAME</code> and falls back to
 * <code>ISources.ACTIVE_WORKBENCH_WINDOW</code>. That is, if the active
 * shell doesn't match, then it will be allowed to match the active workbench
 * window.
 * </p>
 * 
 * @since 3.1
 */
public final class ActiveShellExpression extends Expression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = ActiveShellExpression.class
			.getName().hashCode();

	/**
	 * The sources value to use with this expression.
	 */
	public static final int SOURCES = ISources.ACTIVE_SHELL
			| ISources.ACTIVE_WORKBENCH_WINDOW;

	/**
	 * The shell that must be active for this expression to evaluate to
	 * <code>true</code>. If this value is <code>null</code>, then any
	 * shell may be active.
	 */
	private final Shell activeShell;

	/**
	 * Constructs a new instance of <code>ActiveShellExpression</code>
	 * 
	 * @param activeShell
	 *            The shell to match with the active shell; <code>null</code>
	 *            if it will match any active shell.
	 */
	public ActiveShellExpression(final Shell activeShell) {
		this.activeShell = activeShell;
	}

	/**
	 * Expression information for this expression.  Namely active shell and 
	 * active workbench window name.
	 * 
	 * @since 3.2
	 */
	public final void collectExpressionInfo(final ExpressionInfo info) {
		info.addVariableNameAccess(ISources.ACTIVE_SHELL_NAME);
		info.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
	}

	protected final int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(activeShell);
	}

	public final boolean equals(final Object object) {
		if (object instanceof ActiveShellExpression) {
			final ActiveShellExpression that = (ActiveShellExpression) object;
			return equals(this.activeShell, that.activeShell);
		}

		return false;
	}

	/**
	 * Evaluates this expression. If the active shell defined by the context
	 * matches the shell from this expression, then this evaluates to
	 * <code>EvaluationResult.TRUE</code>. Similarly, if the active workbench
	 * window shell defined by the context matches the shell from this
	 * expression, then this evaluates to <code>EvaluationResult.TRUE</code>.
	 * 
	 * @param context
	 *            The context from which the current state is determined; must
	 *            not be <code>null</code>.
	 * @return <code>EvaluationResult.TRUE</code> if the shell is active;
	 *         <code>EvaluationResult.FALSE</code> otherwise.
	 */
	public final EvaluationResult evaluate(final IEvaluationContext context) {
		if (activeShell != null) {
			Object value = context.getVariable(ISources.ACTIVE_SHELL_NAME);
			if (!activeShell.equals(value)) {
				value = context
						.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME);
				if (!activeShell.equals(value)) {
					return EvaluationResult.FALSE;
				}
			}
		}

		return EvaluationResult.TRUE;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ActiveShellExpression("); //$NON-NLS-1$
		buffer.append(activeShell);
		buffer.append(')');
		return buffer.toString();
	}
}
