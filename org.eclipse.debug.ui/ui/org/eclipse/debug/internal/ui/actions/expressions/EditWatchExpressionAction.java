/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - integration with non-standard debug models (Bug 209883)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;


import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * Open the watch expression dialog for the select watch expression.
 * Re-evaluate and refresh the watch expression is necessary.
 */
public class EditWatchExpressionAction extends WatchExpressionAction {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IWatchExpression watchExpression= getSelectedExpressions()[0];
		// display the watch expression dialog for the currently selected watch expression
		new WatchExpressionDialog(DebugUIPlugin.getShell(), watchExpression, true).open();
	}

	public void selectionChanged(IAction action, ISelection sel) {
	    action.setEnabled(getSelectedExpressions().length == 1);
	}
}
