/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
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
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Closes all active editors
 * <p>
 * Replacement for CloseAllAction
 * </p>
 * 
 * @since 3.3
 * 
 */
public class CloseAllHandler extends AbstractEvaluationHandler {
	private Expression enabledWhen;

	public CloseAllHandler() {
		registerEnablement();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			page.closeAllEditors(true);
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
					IWorkbenchPart part = InternalHandlerUtil.getActivePart(context);
					Object perspective = InternalHandlerUtil.getVariable(context,
							ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
					if (part != null && perspective != null) {
						IWorkbenchPage page = part.getSite().getPage();
						if (page != null) {
							IEditorReference[] refArray = page.getEditorReferences();
							if (refArray != null && refArray.length > 0) {
								return EvaluationResult.TRUE;
							}
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
					info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
					info.addVariableNameAccess(ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME);
				}
			};
		}
		return enabledWhen;
	}
}
