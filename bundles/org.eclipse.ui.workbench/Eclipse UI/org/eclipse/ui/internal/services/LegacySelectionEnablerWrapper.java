/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.SelectionEnabler;

/**
 * <p>
 * An expression wrapper for the legacy {@link SelectionEnabler}. This emulates
 * an {@link Expression} using an instance of <code>SelectionEnabler</code>.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public final class LegacySelectionEnablerWrapper extends Expression {

	/**
	 * The enabler for this expression; never <code>null</code>.
	 */
	private final SelectionEnabler enabler;

	/**
	 * Constructs a new instance of <code>SelectionEnablerExpression</code>.
	 * 
	 * @param enabler
	 *            The enabler; must not be <code>null</code>.
	 */
	public LegacySelectionEnablerWrapper(final SelectionEnabler enabler) {
		if (enabler == null) {
			throw new NullPointerException("There is no enabler"); //$NON-NLS-1$
		}
		this.enabler = enabler;
	}

	public final void collectExpressionInfo(final ExpressionInfo info) {
		info.markDefaultVariableAccessed();
	}

	public final EvaluationResult evaluate(final IEvaluationContext context) {
		final Object defaultVariable = context.getDefaultVariable();
		if (defaultVariable instanceof ISelection) {
			final ISelection selection = (ISelection) defaultVariable;
			if (enabler.isEnabledForSelection(selection)) {
				return EvaluationResult.TRUE;
			}
		}

		return EvaluationResult.FALSE;
	}
}
