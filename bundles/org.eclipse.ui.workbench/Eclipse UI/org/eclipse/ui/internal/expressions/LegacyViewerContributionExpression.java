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

import java.util.Collection;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * <p>
 * An expression that can control the activation of a handler derived from a
 * viewer contribution. The viewer contribution is linked to a menu with a
 * particular identifier, as well as some other criteria. This expression checks
 * the target menu id, and contains a child expression for the other criteria.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @see ISources#ACTIVE_MENU_NAME
 * @since 3.2
 */
public final class LegacyViewerContributionExpression extends
		WorkbenchWindowExpression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = LegacyViewerContributionExpression.class
			.getName().hashCode();

	/**
	 * The child expression for this viewer contribution. This value may be
	 * <code>null</code> if there are no criteria beyond the menu id.
	 */
	private final Expression expression;

	/**
	 * The identifier of the menu to which this viewer contribution applies.
	 * This value is never <code>null</code>.
	 */
	private final String targetId;

	/**
	 * Constructs a new {@link LegacyViewerContributionExpression}.
	 * 
	 * @param targetId
	 *            The identifier of the menu to which this viewer contribution
	 *            applies; never <code>null</code>.
	 * @param window
	 *            The workbench window which must be active for this expression
	 *            to evaluate to <code>true</code>; may be <code>null</code>
	 *            if the window should be disregarded.
	 * @param childExpression
	 *            The child expression for this viewer contribution; may be
	 *            <code>null</code>.
	 */
	public LegacyViewerContributionExpression(final String targetId,
			final IWorkbenchWindow window, final Expression childExpression) {
		super(window);

		if (targetId == null) {
			throw new NullPointerException("The targetId cannot be null"); //$NON-NLS-1$
		}
		this.targetId = targetId;
		this.expression = childExpression;
	}

	public final void collectExpressionInfo(final ExpressionInfo info) {
		super.collectExpressionInfo(info);
		info.addVariableNameAccess(ISources.ACTIVE_MENU_NAME);
		if (expression != null) {
			expression.collectExpressionInfo(info);
		}
	}

	protected final int computeHashCode() {
		int hashCode = HASH_INITIAL * HASH_FACTOR + hashCode(getWindow());
		hashCode = hashCode * HASH_FACTOR + hashCode(expression);
		hashCode = hashCode * HASH_FACTOR + hashCode(targetId);
		return hashCode;
	}

	public final boolean equals(final Object object) {
		if (object instanceof LegacyViewerContributionExpression) {
			final LegacyViewerContributionExpression that = (LegacyViewerContributionExpression) object;
			return equals(this.targetId, that.targetId)
					&& equals(this.expression, that.expression)
					&& equals(this.getWindow(), that.getWindow());
		}

		return false;
	}

	public final EvaluationResult evaluate(final IEvaluationContext context)
			throws CoreException {
		final EvaluationResult result = super.evaluate(context);
		if (result == EvaluationResult.FALSE) {
			return result;
		}

		final Object value = context.getVariable(ISources.ACTIVE_MENU_NAME);
		if (value instanceof String) {
			final String menuId = (String) value;
			if (targetId.equals(menuId)) {
				if (expression == null) {
					return EvaluationResult.TRUE;
				}

				return expression.evaluate(context);
			}
		} else if (value instanceof Collection) {
			final Collection menuIds = (Collection) value;
			if (menuIds.contains(targetId)) {
				if (expression == null) {
					return EvaluationResult.TRUE;
				}

				return expression.evaluate(context);
			}
		}

		return EvaluationResult.FALSE;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ViewerContributionExpression("); //$NON-NLS-1$
		buffer.append(targetId);
		buffer.append(',');
		buffer.append(expression);
		buffer.append(',');
		buffer.append(getWindow());
		buffer.append(')');
		return buffer.toString();
	}
}
