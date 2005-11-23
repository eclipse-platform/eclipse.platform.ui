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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Generic abstract class for the actions associated to the java watch
 * expressions.
 */
public abstract class WatchExpressionAction implements IObjectActionDelegate, IActionDelegate2 {
	IWorkbenchPart fPart = null;
	/**
	 * Finds the currently selected context in the UI.
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
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}

	/**
		* @see IActionDelegate#selectionChanged(IAction, ISelection)
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
		* @param message
		*/
	protected void showErrorMessage(String message) {
		if (fPart instanceof IViewPart) {
			IViewSite viewSite = ((IViewPart) fPart).getViewSite();
			IStatusLineManager manager = viewSite.getActionBars().getStatusLineManager();
			manager.setErrorMessage(message);
			Display.getCurrent().beep();
		}
	}

	/* (non-Javadoc)
		* @see org.eclipse.ui.IActionDelegate2#dispose()
		*/
	public void dispose() {
		fPart = null;
	}

	/* (non-Javadoc)
		* @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
		*/
	public void init(IAction action) {
	}

	/* (non-Javadoc)
		* @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
		*/
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
}
