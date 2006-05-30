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

package org.eclipse.ui.internal.expressions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * <p>
 * An expression representing the <code>targetId</code> of the legacy editor
 * contributions.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class LegacyEditorContributionExpression extends
		WorkbenchWindowExpression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = LegacyEditorContributionExpression.class
			.getName().hashCode();

	/**
	 * The identifier for the editor that must be active for this expression to
	 * evaluate to <code>true</code>. This value is never <code>null</code>.
	 */
	private final String activeEditorId;

	/**
	 * Constructs a new instance of
	 * <code>LegacyEditorContributionExpression</code>
	 * 
	 * @param activeEditorId
	 *            The identifier of the editor to match with the active editor;
	 *            may be <code>null</code>
	 * @param window
	 *            The workbench window in which this handler should be active.
	 *            This value is never <code>null</code>.
	 */
	public LegacyEditorContributionExpression(final String activeEditorId,
			final IWorkbenchWindow window) {
		super(window);

		if (activeEditorId == null) {
			throw new NullPointerException(
					"The targetId for an editor contribution must not be null"); //$NON-NLS-1$
		}
		this.activeEditorId = activeEditorId;
	}

	public final void collectExpressionInfo(final ExpressionInfo info) {
		super.collectExpressionInfo(info);
		info.addVariableNameAccess(ISources.ACTIVE_PART_ID_NAME);
	}

	protected final int computeHashCode() {
		int hashCode = HASH_INITIAL * HASH_FACTOR + hashCode(getWindow());
		hashCode = hashCode * HASH_FACTOR + hashCode(activeEditorId);
		return hashCode;
	}

	public final boolean equals(final Object object) {
		if (object instanceof LegacyEditorContributionExpression) {
			final LegacyEditorContributionExpression that = (LegacyEditorContributionExpression) object;
			return equals(this.activeEditorId, that.activeEditorId)
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

		final Object variable = context
				.getVariable(ISources.ACTIVE_PART_ID_NAME);
		if (equals(activeEditorId, variable)) {
			return EvaluationResult.TRUE;
		}
		return EvaluationResult.FALSE;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("LegacyEditorContributionExpression("); //$NON-NLS-1$
		buffer.append(activeEditorId);
		buffer.append(',');
		buffer.append(getWindow());
		buffer.append(')');
		return buffer.toString();
	}

}
