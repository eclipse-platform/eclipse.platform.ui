/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 */
public class WatchAction implements IObjectActionDelegate {

	private ISelection fSelection;

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		if (fSelection instanceof IStructuredSelection) {
			Iterator iter = ((IStructuredSelection) fSelection).iterator();
			while (iter.hasNext()) {
				IVariable variable = (IVariable) iter.next();
				createExpression(variable);
			}
		} else if (fSelection instanceof IVariable) {
			createExpression((IVariable) fSelection);
		}
	}

	private void createExpression(IVariable variable) {
		IWatchExpression expression;
		try {
			expression = DebugPlugin.getDefault().getExpressionManager().newWatchExpression(variable.getName());
		} catch (DebugException e) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.getString("WatchAction.0"), ActionMessages.getString("WatchAction.1"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		DebugPlugin.getDefault().getExpressionManager().addExpression(expression);
		IAdaptable object = DebugUITools.getDebugContext();
		IDebugElement context = null;
		if (object instanceof IDebugElement) {
			context = (IDebugElement) object;
		} else if (object instanceof ILaunch) {
			context = ((ILaunch) object).getDebugTarget();
		}
		expression.setExpressionContext(context);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

}
