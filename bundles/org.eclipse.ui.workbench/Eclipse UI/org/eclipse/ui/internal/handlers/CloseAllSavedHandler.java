/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.AbstractEvaluationHandler;
import org.eclipse.ui.internal.InternalHandlerUtil;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.util.Util;


/**
 * 
 * @author Prakash G.R.
 * @since 3.7
 * 
 */
public class CloseAllSavedHandler extends AbstractEvaluationHandler {

	private Expression enabledWhen;
	private IWorkbenchPage page;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.AbstractEvaluationHandler#getEnabledWhenExpression
	 * ()
	 */
	protected Expression getEnabledWhenExpression() {
		if (enabledWhen == null) {
			enabledWhen = new Expression() {
				public EvaluationResult evaluate(IEvaluationContext context) {
					return CloseAllSavedHandler.this.evaluate(context);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.eclipse.core.expressions.Expression#collectExpressionInfo
				 * (org.eclipse.core.expressions.ExpressionInfo)
				 */
				public void collectExpressionInfo(ExpressionInfo info) {
					// We use active part, so that we get evaluated for the part
					// events
					info.addVariableNameAccess(ISources.ACTIVE_PART_NAME);
				}
			};
		}
		return enabledWhen;
	}

	private EvaluationResult evaluate(IEvaluationContext context) {

		IWorkbenchWindow window = InternalHandlerUtil.getActiveWorkbenchWindow(context);

		setWindow(window);

		return window != null && window.getActivePage() != null ? EvaluationResult.TRUE
				: EvaluationResult.FALSE;
	}

	/**
	 * @param window
	 */
	private void setWindow(IWorkbenchWindow window) {

		if (Util.equals(page, window.getActivePage()))
			return;

		page = window.getActivePage();

		update();
	}

	/**
	 * @return Returns the page.
	 */
	public IWorkbenchPage getActivePage() {
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			((WorkbenchPage) page).closeAllSavedEditors();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.handlers.AbstractPageEventHandler#update()
	 */
	protected void update() {
		IWorkbenchPage page = getActivePage();
		if (page == null) {
			setEnabled(false);
			return;
		}
		IEditorReference editors[] = page.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			if (!editors[i].isDirty()) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}

}
