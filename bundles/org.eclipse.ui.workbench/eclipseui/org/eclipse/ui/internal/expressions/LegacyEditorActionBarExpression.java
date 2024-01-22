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
 ******************************************************************************/

package org.eclipse.ui.internal.expressions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.services.SourcePriorityNameMapping;

/**
 * <p>
 * An expression representing the <code>part id</code> of the legacy editor
 * action bar contribution.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public class LegacyEditorActionBarExpression extends Expression {
	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = LegacyEditorActionBarExpression.class.getName().hashCode();

	/**
	 * The identifier for the editor that must be active for this expression to
	 * evaluate to <code>true</code>. This value is never <code>null</code>.
	 */
	private final String activeEditorId;

	/**
	 * Constructs a new instance of <code>LegacyEditorActionBarExpression</code>
	 *
	 * @param editorId The identifier of the editor to match with the active editor;
	 *                 must not be <code>null</code>
	 */
	public LegacyEditorActionBarExpression(final String editorId) {

		if (editorId == null) {
			throw new IllegalArgumentException("The targetId for an editor contribution must not be null"); //$NON-NLS-1$
		}
		activeEditorId = editorId;
	}

	@Override
	public final void collectExpressionInfo(final ExpressionInfo info) {
		info.addVariableNameAccess(ISources.ACTIVE_PART_ID_NAME);
		info.addVariableNameAccess(SourcePriorityNameMapping.LEGACY_LEGACY_NAME);
	}

	@Override
	protected final int computeHashCode() {
		return HASH_INITIAL * HASH_FACTOR + hashCode(activeEditorId);
	}

	@Override
	public final boolean equals(final Object object) {
		if (object instanceof LegacyEditorActionBarExpression) {
			final LegacyEditorActionBarExpression that = (LegacyEditorActionBarExpression) object;
			return activeEditorId.equals(that.activeEditorId);
		}

		return false;
	}

	@Override
	public final EvaluationResult evaluate(final IEvaluationContext context) {
		final Object variable = context.getVariable(ISources.ACTIVE_PART_ID_NAME);
		if (equals(activeEditorId, variable)) {
			return EvaluationResult.TRUE;
		}
		return EvaluationResult.FALSE;
	}

	@Override
	public final String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("LegacyEditorActionBarExpression("); //$NON-NLS-1$
		buffer.append(activeEditorId);
		buffer.append(')');
		return buffer.toString();
	}
}
