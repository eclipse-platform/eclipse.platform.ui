/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.expressions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * <p>
 * An expression that evaluates to {@link EvaluationResult#TRUE} when the active
 * workbench window matches the window held by this expression.
 * </p>
 *
 * @since 3.2
 */
public class WorkbenchWindowExpression extends Expression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = WorkbenchWindowExpression.class.getName().hashCode();

	/**
	 * The workbench window that must be active for this expression to evaluate to
	 * <code>true</code>. If this value is <code>null</code>, then any workbench
	 * window may be active.
	 */
	private final IWorkbenchWindow window;

	/**
	 * Constructs a new instance.
	 *
	 * @param window The workbench window which must be active for this expression
	 *               to evaluate to <code>true</code>; may be <code>null</code> if
	 *               this expression is always <code>true</code>.
	 */
	public WorkbenchWindowExpression(final IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void collectExpressionInfo(final ExpressionInfo info) {
		if (window != null) {
			info.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
		}
	}

	@Override
	protected int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(window);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof WorkbenchWindowExpression) {
			final WorkbenchWindowExpression that = (WorkbenchWindowExpression) object;
			return equals(this.window, that.window);
		}

		return false;
	}

	@Override
	public EvaluationResult evaluate(final IEvaluationContext context) throws CoreException {
		if (window != null) {
			Object value = context.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
			if (window.equals(value)) {
				return EvaluationResult.TRUE;
			}
		}

		return EvaluationResult.FALSE;
	}

	/**
	 * Returns the workbench window to which this expression applies.
	 *
	 * @return The workbench window to which this expression applies; may be
	 *         <code>null</code>.
	 */
	protected final IWorkbenchWindow getWindow() {
		return window;
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("WorkbenchWindowExpression("); //$NON-NLS-1$
		buffer.append(window);
		buffer.append(')');
		return buffer.toString();
	}
}
