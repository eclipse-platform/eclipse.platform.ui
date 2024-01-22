/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * <p>
 * An expression representing the <code>targetId</code> of the legacy view
 * contributions.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public final class LegacyViewContributionExpression extends WorkbenchWindowExpression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = LegacyViewContributionExpression.class.getName().hashCode();

	/**
	 * The identifier for the part that must be active for this expression to
	 * evaluate to <code>true</code>. This value is never <code>null</code>.
	 */
	private final String activePartId;

	/**
	 * Constructs a new instance of <code>LegacyViewContributionExpression</code>
	 *
	 * @param activePartId The identifier of the part to match with the active part;
	 *                     may be <code>null</code>
	 * @param window       The workbench window in which this handler should be
	 *                     active. This value is never <code>null</code>.
	 */
	public LegacyViewContributionExpression(final String activePartId, final IWorkbenchWindow window) {
		super(window);

		if (activePartId == null) {
			throw new NullPointerException("The targetId for a view contribution must not be null"); //$NON-NLS-1$
		}
		this.activePartId = activePartId;
	}

	@Override
	public void collectExpressionInfo(final ExpressionInfo info) {
		super.collectExpressionInfo(info);
		info.addVariableNameAccess(ISources.ACTIVE_PART_ID_NAME);
	}

	@Override
	protected int computeHashCode() {
		int hashCode = HASH_INITIAL * HASH_FACTOR + hashCode(getWindow());
		return hashCode * HASH_FACTOR + hashCode(activePartId);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof LegacyViewContributionExpression) {
			final LegacyViewContributionExpression that = (LegacyViewContributionExpression) object;
			return equals(this.activePartId, that.activePartId) && equals(this.getWindow(), that.getWindow());
		}

		return false;
	}

	@Override
	public EvaluationResult evaluate(final IEvaluationContext context) throws CoreException {
		final EvaluationResult result = super.evaluate(context);
		if (result == EvaluationResult.FALSE) {
			return result;
		}

		final Object variable = context.getVariable(ISources.ACTIVE_PART_ID_NAME);
		if (equals(activePartId, variable)) {
			return EvaluationResult.TRUE;
		}
		return EvaluationResult.FALSE;
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("LegacyViewContributionExpression("); //$NON-NLS-1$
		buffer.append(activePartId);
		buffer.append(',');
		buffer.append(getWindow());
		buffer.append(')');
		return buffer.toString();
	}

}
