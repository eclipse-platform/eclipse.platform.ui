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

package org.eclipse.ui.handlers;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * <p>
 * An expression encapsulating all of the information from legacy handler
 * submissions.
 * </p>
 * 
 * @since 3.1
 */
public final class LegacyHandlerSubmissionExpression extends Expression {

	/**
	 * The identifier for the part that must be active for this expression to
	 * evaluate to <code>true</code>. If this value is <code>null</code>,
	 * then any part may be active.
	 */
	private final String activePartId;

	/**
	 * The shell that must be active for this expression to evaluate to
	 * <code>true</code>. If this value is <code>null</code>, then any
	 * shell may be active.
	 */
	private final Shell activeShell;

	/**
	 * The site that must be active for this expression to evaluate to
	 * <code>true</code>. If this value is <code>null</code>, then any
	 * site may be active.
	 */
	private final IWorkbenchPartSite activeSite;

	/**
	 * Constructs a new instance of
	 * <code>LegacyHandlerSubmissionExpression</code>
	 * 
	 * @param activePartId
	 *            The part identifier to match with the active part;
	 *            <code>null</code> if it will match any active part.
	 * @param activeShell
	 *            The shell to match with the active shell; <code>null</code>
	 *            if it will match any active shell.
	 * @param activeSite
	 *            The site to match with the active site; <code>null</code> if
	 *            it will match any active site.
	 */
	public LegacyHandlerSubmissionExpression(final String activePartId,
			final Shell activeShell, final IWorkbenchPartSite activeSite) {

		this.activePartId = activePartId;
		this.activeShell = activeShell;
		this.activeSite = activeSite;
	}

	public final EvaluationResult evaluate(final IEvaluationContext context) {
		if (activePartId != null) {
			final Object value = context.getVariable(ISources.ACTIVE_PART_NAME);
			if (!activePartId.equals(value)) {
				return EvaluationResult.FALSE;
			}
		}

		if (activeShell != null) {
			Object value = context.getVariable(ISources.ACTIVE_SHELL_NAME);
			if (!activeShell.equals(value)) {
				value = context
						.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
				if (!activeShell.equals(value)) {
					return EvaluationResult.FALSE;
				}
			}
		}

		if (activeSite != null) {
			final Object value = context.getVariable(ISources.ACTIVE_SITE_NAME);
			if (!activeSite.equals(value)) {
				return EvaluationResult.FALSE;
			}
		}

		return EvaluationResult.TRUE;
	}
}
