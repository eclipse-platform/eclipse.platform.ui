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
package org.eclipse.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * 
 */
public class WatchAction implements IObjectActionDelegate {

	private IStructuredSelection fSelection;

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
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			IVariable variable = (IVariable) iter.next();
			createExpression(variable);
		}
	}

	private void showExpressionsView() {
		IWorkbenchPage page = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
		if (part == null) {
			try {
				page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
			} catch (PartInitException e) {
			}
		} else {
			page.bringToTop(part);
		}

	}

	private void createExpression(IVariable variable) {
		IWatchExpression expression;
		try {
			expression = DebugPlugin.getDefault().getExpressionManager().newWatchExpression(variable.getName());
		} catch (DebugException e) {
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.WatchAction_0, ActionMessages.WatchAction_1, e); // 
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
		showExpressionsView();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
        fSelection = null;
        int enabled = 0;
        int size = -1;
        if (selection instanceof IStructuredSelection) {
            fSelection = (IStructuredSelection) selection;
            size = fSelection.size();
            IExpressionManager manager = DebugPlugin.getDefault().getExpressionManager();
            Iterator iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                IVariable variable = (IVariable) iterator.next();
                if (manager.hasWatchExpressionDelegate(variable.getModelIdentifier())) {
                    enabled++;
                } else {
                    break;
                }
            }
        }
        action.setEnabled(enabled == size);
	}

}
