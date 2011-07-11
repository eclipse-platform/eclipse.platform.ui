/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Generic abstract class for the actions associated to the java watch
 * expressions.
 */
public abstract class WatchExpressionAction implements IViewActionDelegate {
	IWorkbenchPart fPart = null;

	private static IWatchExpression[] EMPTY_EXPRESSION_ARRAY = new IWatchExpression[0]; 
	
	public void init(IViewPart view) {
	    fPart = view;
	}

    /**
     * Finds the currently selected context in the UI.
     * @return the current debug context
     */
    protected IDebugElement getContext() {
        IAdaptable object = DebugUITools.getDebugContext();
        IDebugElement context = null;
        if (object instanceof IDebugElement) {
            context = (IDebugElement) object;
        } else if (object instanceof ILaunch) {
            context = ((ILaunch) object).getDebugTarget();
        }
        return context;
    }

	protected IWatchExpression[] getSelectedExpressions() {
	    List list = new LinkedList(); 
	    IStructuredSelection currentSelection = getCurrentSelection();
	    if (currentSelection == null) {
	        return EMPTY_EXPRESSION_ARRAY;
	    }
	    
        for (Iterator iter= currentSelection.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element instanceof IWatchExpression) {
                list.add(element);
            } else if (element instanceof IAdaptable) {
                IWatchExpression expr = (IWatchExpression)((IAdaptable)element).getAdapter(IWatchExpression.class);
                if (expr != null) {
                    list.add(expr);
                } else {
                    return EMPTY_EXPRESSION_ARRAY;
                }
            } else {
                return EMPTY_EXPRESSION_ARRAY;
            }
        }
        
        return (IWatchExpression[])list.toArray(new IWatchExpression[list.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
	}

	protected IStructuredSelection getCurrentSelection() {
		IWorkbenchPage page = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return null;
	}

	/**
		* Displays the given error message in the status line.
		* 
		* @param message the message to display
		*/
	protected void showErrorMessage(String message) {
		if (fPart instanceof IViewPart) {
			IViewSite viewSite = ((IViewPart) fPart).getViewSite();
			IStatusLineManager manager = viewSite.getActionBars().getStatusLineManager();
			manager.setErrorMessage(message);
			Display.getCurrent().beep();
		}
	}
}
