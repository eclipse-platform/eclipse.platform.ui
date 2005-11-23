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

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

/**
 * Ask to re-evaluate one or more watch expressions in the context of the
 * currently selected thread.
 */
public class ReevaluateWatchExpressionAction extends WatchExpressionAction {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IDebugElement context = getContext();
		for (Iterator iter= getCurrentSelection().iterator(); iter.hasNext();) {
			IWatchExpression expression= (IWatchExpression) iter.next();
			expression.setExpressionContext(context);
			if (!expression.isEnabled()) {
				// Force a reevaluation
				expression.evaluate();
			}
		}
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		IDebugElement debugElement = getContext();
		if (debugElement == null) {
			action.setEnabled(false);
		} else {
			action.setEnabled(true);
		}
	}

}
