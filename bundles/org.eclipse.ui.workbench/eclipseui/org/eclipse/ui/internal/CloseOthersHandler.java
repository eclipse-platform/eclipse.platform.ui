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
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Closes all editors except the one that is active.
 * <p>
 * Replacement for CloseOthersHandler
 * </p>
 *
 * @since 3.3
 */
public class CloseOthersHandler extends AbstractEvaluationHandler {
	private Expression enabledWhen;

	public CloseOthersHandler() {
		registerEnablement();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IEditorReference[] refArray = page.getEditorReferences();
			if (refArray != null && refArray.length > 1) {
				IEditorReference[] otherEditors = new IEditorReference[refArray.length - 1];
				IEditorReference activeEditor = (IEditorReference) page.getReference(page.getActiveEditor());
				for (int i = 0; i < refArray.length; i++) {
					if (refArray[i] != activeEditor)
						continue;
					System.arraycopy(refArray, 0, otherEditors, 0, i);
					System.arraycopy(refArray, i + 1, otherEditors, i, refArray.length - 1 - i);
					break;
				}
				page.closeEditors(otherEditors, true);
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
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							IEditorReference[] refArray = page.getEditorReferences();
							if (refArray != null && refArray.length > 1) {
								return EvaluationResult.TRUE;

							}
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
