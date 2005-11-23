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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 */
public class EnableWatchExpressionAction implements IObjectActionDelegate {

	private ISelection fSelection;
	protected boolean fEnable= true;

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection instanceof IStructuredSelection) {
			Iterator iter= ((IStructuredSelection) fSelection).iterator();
			IWatchExpression expression;
			while (iter.hasNext()) {
				expression= ((IWatchExpression) iter.next()); 
				expression.setEnabled(fEnable);
				fireWatchExpressionChanged(expression);
			}
		} else if (fSelection instanceof IWatchExpression) {
			IWatchExpression expression= ((IWatchExpression) fSelection);
			expression.setEnabled(fEnable);
			fireWatchExpressionChanged(expression);
		}
	}

	/**
	 * @param expression
	 */
	private void fireWatchExpressionChanged(IWatchExpression expression) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {new DebugEvent(expression, DebugEvent.CHANGE)});
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
		if (fSelection instanceof IStructuredSelection) {
			boolean enabled= false;
			Iterator iter= ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				IWatchExpression expression = (IWatchExpression) iter.next();
				if (expression.isEnabled() != fEnable) {
					enabled= true;
					break;
				}
			}
			action.setEnabled(enabled);
		} else if (fSelection instanceof IWatchExpression) {
			action.setEnabled(((IWatchExpression) fSelection).isEnabled() != fEnable);
		} else {
			action.setEnabled(false);
		}
	}

}
