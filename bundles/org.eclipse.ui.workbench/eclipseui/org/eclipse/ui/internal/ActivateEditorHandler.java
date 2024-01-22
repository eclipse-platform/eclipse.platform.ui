/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Activates the most recently used editor in the current window.
 * <p>
 * Replacement for: ActivateEditorAction
 * </p>
 *
 * @since 3.3
 */
public class ActivateEditorHandler extends AbstractEvaluationHandler {

	private Expression enabledWhen;

	public ActivateEditorHandler() {
		registerEnablement();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IEditorPart part = HandlerUtil.getActiveEditor(event);
			if (part != null) {
				page.activate(part);
			} else {
				IWorkbenchPartReference ref = page.getActivePartReference();
				if (ref instanceof IViewReference) {
					// if (((WorkbenchPage) page).isFastView((IViewReference)
					// ref)) {
					// ((WorkbenchPage) page)
					// .toggleFastView((IViewReference) ref);
					// }
				}
			}
		}
		return null;
	}

	@Override
	protected Expression getEnabledWhenExpression() {
		if (enabledWhen == null) {
			enabledWhen = new Expression() {
				@Override
				public EvaluationResult evaluate(IEvaluationContext context) {
					IWorkbenchWindow window = InternalHandlerUtil.getActiveWorkbenchWindow(context);
					if (window != null) {
						if (window.getActivePage() != null) {
							return EvaluationResult.TRUE;
						}
					}
					return EvaluationResult.FALSE;
				}

				@Override
				public void collectExpressionInfo(ExpressionInfo info) {
					info.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
				}
			};
		}
		return enabledWhen;
	}
}
