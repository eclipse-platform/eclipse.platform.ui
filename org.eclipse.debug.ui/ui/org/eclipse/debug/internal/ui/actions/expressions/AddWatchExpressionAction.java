/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Open a watch expression dialog and add the created watch expression to the
 * expression view.
 */
public class AddWatchExpressionAction extends WatchExpressionAction implements IViewActionDelegate {

	/**
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// create a watch expression
		IWatchExpression watchExpression= DebugPlugin.getDefault().getExpressionManager().newWatchExpression(IInternalDebugCoreConstants.EMPTY_STRING);
		// open the watch expression dialog
		if (new WatchExpressionDialog(DebugUIPlugin.getShell(), watchExpression, false).open() == Window.OK) {
			// if OK is selected, add the expression to the expression view and try to evaluate the expression.
			DebugPlugin.getDefault().getExpressionManager().addExpression(watchExpression);
			watchExpression.setExpressionContext(getContext());
		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
