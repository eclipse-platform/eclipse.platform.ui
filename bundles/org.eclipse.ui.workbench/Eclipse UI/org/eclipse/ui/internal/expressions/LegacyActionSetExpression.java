/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.expressions;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * <p>
 * An expression that evaluates whether a particular action set is active.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class LegacyActionSetExpression extends WorkbenchWindowExpression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = LegacyActionSetExpression.class
			.getName().hashCode();

	/**
	 * The identifier of the action set that must be active for this expression
	 * to evaluate to <code>true</code>. This value is never
	 * <code>null</code>.
	 */
	private final String actionSetId;

	/**
	 * Constructs a new instance of {@link LegacyActionSetExpression}.
	 * 
	 * @param actionSetId
	 *            The identifier of the action set that must be active for this
	 *            expression to evaluate to <code>true</code>; must not be
	 *            <code>null</code>.
	 * @param window
	 *            The workbench window in which this handler should be active.
	 *            This avoids conflicts between handlers from different windows.
	 *            This should not be <code>null</code>.
	 */
	public LegacyActionSetExpression(final String actionSetId,
			final IWorkbenchWindow window) {
		super(window);
		if (actionSetId == null) {
			throw new NullPointerException(
					"The action set identifier cannot be null"); //$NON-NLS-1$
		}
		this.actionSetId = actionSetId;
	}

	public final void collectExpressionInfo(final ExpressionInfo info) {
		super.collectExpressionInfo(info);
		info.addVariableNameAccess(ISources.ACTIVE_ACTION_SETS_NAME);
	}

	protected final int computeHhashCode() {
		int hashCode = HASH_INITIAL * HASH_FACTOR + hashCode(getWindow());
		hashCode = hashCode * HASH_FACTOR + hashCode(actionSetId);
		return hashCode;
	}

	public final boolean equals(final Object object) {
		if (object instanceof LegacyActionSetExpression) {
			final LegacyActionSetExpression that = (LegacyActionSetExpression) object;
			return equals(this.actionSetId, that.actionSetId)
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

		// Check the action sets.
		final Object variable = context
				.getVariable(ISources.ACTIVE_ACTION_SETS_NAME);
		if (variable instanceof IActionSetDescriptor[]) {
			final IActionSetDescriptor[] descriptors = (IActionSetDescriptor[]) variable;
			for (int i = 0; i < descriptors.length; i++) {
				final IActionSetDescriptor descriptor = descriptors[i];
				final String currentId = descriptor.getId();
				if (actionSetId.equals(currentId)) {
					return EvaluationResult.TRUE;
				}
			}
		}

		return EvaluationResult.FALSE;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ActionSetExpression("); //$NON-NLS-1$
		buffer.append(actionSetId);
		buffer.append(',');
		buffer.append(getWindow());
		buffer.append(')');
		return buffer.toString();
	}
}
