/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.expressions;


import java.util.Iterator;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Convert one or more expressions to the equivalent watch expressions.
 * Refresh and re-evaluate the expressions if possible.
 */
public class ConvertToWatchExpressionAction extends WatchExpressionAction {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		IExpressionManager expressionManager= DebugPlugin.getDefault().getExpressionManager();
		for (Iterator iter= selection.iterator(); iter.hasNext();) {
			IExpression expression= (IExpression) iter.next();
			// create the new watch expression
			IWatchExpression watchExpression= expressionManager.newWatchExpression(expression.getExpressionText());
			expressionManager.removeExpression(expression);
			expressionManager.addExpression(watchExpression);
			// refresh and re-evaluate
			watchExpression.setExpressionContext(getContext());
		}
	}

}
