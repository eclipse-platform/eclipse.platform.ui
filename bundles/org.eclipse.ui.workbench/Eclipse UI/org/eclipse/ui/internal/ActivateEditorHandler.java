/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.runtime.CoreException;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IEditorPart part = HandlerUtil.getActiveEditor(event);
			if (part != null) {
				page.activate(part);
				part.setFocus();
			} else {
				IWorkbenchPartReference ref = page.getActivePartReference();
				if (ref instanceof IViewReference) {
					if (((WorkbenchPage) page).isFastView((IViewReference) ref)) {
						((WorkbenchPage) page)
								.toggleFastView((IViewReference) ref);
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.AbstractEvaluationHandler#getEnabledWhenExpression()
	 */
	protected Expression getEnabledWhenExpression() {
		if (enabledWhen == null) {
			enabledWhen = new Expression() {
				public EvaluationResult evaluate(IEvaluationContext context)
						throws CoreException {
					IWorkbenchWindow window = InternalHandlerUtil
							.getActiveWorkbenchWindow(context);
					if (window != null) {
						if (window.getActivePage() != null) {
							return EvaluationResult.TRUE;
						}
					}
					return EvaluationResult.FALSE;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.expressions.Expression#collectExpressionInfo(org.eclipse.core.expressions.ExpressionInfo)
				 */
				public void collectExpressionInfo(ExpressionInfo info) {
					info
							.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
				}
			};
		}
		return enabledWhen;
	}
}
