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
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SelectionEnabler;

/**
 * <p>
 * An expression wrapper for the legacy {@link SelectionEnabler}. This emulates
 * an {@link Expression} using an instance of <code>SelectionEnabler</code>.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 */
public final class LegacySelectionEnablerWrapper extends WorkbenchWindowExpression {

	/**
	 * The seed for the hash code for all schemes.
	 */
	private static final int HASH_INITIAL = LegacySelectionEnablerWrapper.class.getName().hashCode();

	/**
	 * The enabler for this expression; never <code>null</code>.
	 */
	private final SelectionEnabler enabler;

	/**
	 * Constructs a new instance of <code>SelectionEnablerExpression</code>.
	 *
	 * @param enabler The enabler; must not be <code>null</code>.
	 * @param window  The workbench window which must be active for this expression
	 *                to evaluate to <code>true</code>; may be <code>null</code> if
	 *                the window should be disregarded.
	 */
	public LegacySelectionEnablerWrapper(final SelectionEnabler enabler, final IWorkbenchWindow window) {
		super(window);

		if (enabler == null) {
			throw new NullPointerException("There is no enabler"); //$NON-NLS-1$
		}
		this.enabler = enabler;
	}

	@Override
	public void collectExpressionInfo(final ExpressionInfo info) {
		super.collectExpressionInfo(info);
		info.markDefaultVariableAccessed();
	}

	@Override
	protected int computeHashCode() {
		int hashCode = HASH_INITIAL * HASH_FACTOR + hashCode(getWindow());
		return hashCode * HASH_FACTOR + hashCode(enabler);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof LegacySelectionEnablerWrapper) {
			final LegacySelectionEnablerWrapper that = (LegacySelectionEnablerWrapper) object;
			return equals(this.enabler, that.enabler) && equals(this.getWindow(), that.getWindow());
		}

		return false;
	}

	@Override
	public EvaluationResult evaluate(final IEvaluationContext context) throws CoreException {
		final EvaluationResult result = super.evaluate(context);
		if (result == EvaluationResult.FALSE) {
			return result;
		}

		final Object defaultVariable = context.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (defaultVariable instanceof ISelection) {
			final ISelection selection = (ISelection) defaultVariable;
			if (enabler.isEnabledForSelection(selection)) {
				return EvaluationResult.TRUE;
			}
		}

		return EvaluationResult.FALSE;
	}

	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("LegacySelectionEnablerWrapper("); //$NON-NLS-1$
		buffer.append(enabler);
		buffer.append(',');
		buffer.append(getWindow());
		buffer.append(')');
		return buffer.toString();
	}
}
