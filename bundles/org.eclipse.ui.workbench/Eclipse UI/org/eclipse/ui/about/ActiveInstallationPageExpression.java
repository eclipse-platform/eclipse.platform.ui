/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.about;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;

/**
 * <p>
 * An expression that checks the active InstallationPage variable. The variable
 * name is <code>IInstallationPageSources.ACTIVE_PAGE</code>.
 * </p>
 * 
 * <em>This API is experimental and will change before 3.5 ships</em>
 * 
 * @since 3.5
 */
public final class ActiveInstallationPageExpression extends Expression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = ActiveInstallationPageExpression.class
			.getName().hashCode();

	/**
	 * The page that must be active for this expression to evaluate to
	 * <code>true</code>. If this value is <code>null</code>, then any page may
	 * be active.
	 */
	private final InstallationPage page;

	/**
	 * Constructs a new instance of <code>ActiveShellExpression</code>
	 * 
	 * @param page
	 *            The page to match with the active installation page;
	 *            <code>null</code> if it will match any active page.
	 */
	public ActiveInstallationPageExpression(final InstallationPage page) {
		this.page = page;
	}

	/**
	 * Expression information for this expression.
	 * 
	 * @since 3.5
	 */
	public final void collectExpressionInfo(final ExpressionInfo info) {
		info.addVariableNameAccess(IInstallationPageSources.ACTIVE_PAGE);
	}

	protected final int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(page);
	}

	public final boolean equals(final Object object) {
		if (object instanceof ActiveInstallationPageExpression) {
			final ActiveInstallationPageExpression that = (ActiveInstallationPageExpression) object;
			return equals(this.page, that.page);
		}

		return false;
	}

	/**
	 * Evaluates this expression. If the active page defined by the context
	 * matches the page from this expression, then this evaluates to
	 * <code>EvaluationResult.TRUE</code>.
	 * 
	 * @param context
	 *            The context from which the current state is determined; must
	 *            not be <code>null</code>.
	 * @return <code>EvaluationResult.TRUE</code> if the page is active;
	 *         <code>EvaluationResult.FALSE</code> otherwise.
	 */
	public final EvaluationResult evaluate(final IEvaluationContext context) {
		if (page != null) {
			Object value = context
					.getVariable(IInstallationPageSources.ACTIVE_PAGE);
			if (page.equals(value))
				return EvaluationResult.TRUE;
		}
		return EvaluationResult.FALSE;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ActiveInstallationPageExpression("); //$NON-NLS-1$
		buffer.append(page);
		buffer.append(')');
		return buffer.toString();
	}
}
