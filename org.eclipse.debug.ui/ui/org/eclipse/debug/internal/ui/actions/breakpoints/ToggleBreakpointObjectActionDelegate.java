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
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A toggle breakpoint action that can be contributed to an object. The action
 * will perform a toggle breakpoint operation for a selected object.
 * <p>
 * EXPERIMENTAL
 * </p>
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.0
 */
public abstract class ToggleBreakpointObjectActionDelegate implements IObjectActionDelegate, IActionDelegate2 {
	
	private IWorkbenchPart fPart;
	private IStructuredSelection fSelection;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IAdaptable adaptable = (IAdaptable) fSelection.getFirstElement();
		IToggleBreakpointsTarget target = (IToggleBreakpointsTarget) adaptable.getAdapter(IToggleBreakpointsTarget.class);
		if (target == null) {
			IAdapterManager adapterManager = Platform.getAdapterManager();
			target = (IToggleBreakpointsTarget) adapterManager.loadAdapter(adaptable, IToggleBreakpointsTarget.class.getName());
		}
		if (target != null) {
			try {
				performAction(target, fPart, fSelection);
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}
	}
	
	/**
	 * Performs the operation specific to this action.
	 *  
	 * @param target adapter to toggle breakpoints
	 * @param part the active part
	 * @param selection the seleciton in the active part
	 * @exception CoreException if an exception occurrs
	 */
	protected abstract void performAction(IToggleBreakpointsTarget target, IWorkbenchPart part, ISelection selection) throws CoreException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled = false;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			this.fSelection = ss;
			if (!ss.isEmpty()) {
				Object object = ss.getFirstElement();
				if (object instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable) object;
					IToggleBreakpointsTarget target = (IToggleBreakpointsTarget) adaptable.getAdapter(IToggleBreakpointsTarget.class);
					if (target == null) {
						IAdapterManager adapterManager = Platform.getAdapterManager();
						enabled = adapterManager.hasAdapter(adaptable, IToggleBreakpointsTarget.class.getName());
					} else {
						enabled = true;
					}
				}
			}
		}
		action.setEnabled(enabled);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
		fSelection = null;
		fPart = null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}
}
