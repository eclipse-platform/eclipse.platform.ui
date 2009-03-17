/*******************************************************************************
 * Copyright (c) 2009 Adobe Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adobe Systems, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;

import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.views.expression.ExpressionView;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Paste a watch expression into the expressions view.
 */
public class PasteWatchExpressionsAction extends SelectionListenerAction {

	private final ExpressionView fExpressionView;

	public PasteWatchExpressionsAction(ExpressionView expressionView) {
		super(ActionMessages.PasteWatchExpressionsAction_0);
		fExpressionView = expressionView;
//        setToolTipText(BreakpointGroupMessages.PasteWatchExpressionsAction_1);
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.PASTE_WATCH_EXPRESSIONS_ACTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run() {
		if (fExpressionView.canPaste()) {
			fExpressionView.performPaste();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	public boolean isEnabled() {
		return fExpressionView.canPaste();
	}

	
	
}
