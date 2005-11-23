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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

/**
 * An action which toggles the breakpoint manager's enablement.
 * This causes debug targets which honor the manager's enablement
 * to skip (not suspend for) all breakpoints. 
 * 
 * This class also implements the window action delegate for the action presented as
 * part of the "Breakpoints" group for the "Run" menu.
 */
public class SkipAllBreakpointsAction extends Action implements IWorkbenchWindowActionDelegate, IBreakpointManagerListener {
	
	//The real action if this is an action delegate
	private IAction fAction;
	
	public SkipAllBreakpointsAction() {
		super(ActionMessages.SkipAllBreakpointsAction_0); 
		setToolTipText(ActionMessages.SkipAllBreakpointsAction_0); 
		setDescription(ActionMessages.SkipAllBreakpointsAction_2); 
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IDebugUIConstants.IMG_SKIP_BREAKPOINTS));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.SKIP_ALL_BREAKPOINT_ACTION);
		updateActionCheckedState();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run(){
		final IBreakpointManager manager = getBreakpointManager();
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                if(!monitor.isCanceled()) {
                    manager.setEnabled(!manager.isEnabled());
                } 
            }
        };
        
        try {
            DebugUIPlugin.getDefault().getWorkbench().getProgressService().busyCursorWhile(runnable);
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        }
	}
	
	/**
	 * Updates the action's checked state to be opposite the enabled
	 * state of the breakpoint manager.
	 */
	public void updateActionCheckedState() {
		if (fAction != null) {
			fAction.setChecked(!getBreakpointManager().isEnabled());
		} else {
			setChecked(!getBreakpointManager().isEnabled());
		}
	}
	
	/**
	 * Returns the global breakpoint manager.
	 * 
	 * @return the global breakpoint manager
	 */
	public static IBreakpointManager getBreakpointManager() {
		return DebugPlugin.getDefault().getBreakpointManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		getBreakpointManager().removeBreakpointManagerListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		updateActionCheckedState();
		getBreakpointManager().addBreakpointManagerListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction= action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		if (fAction != null) {
			fAction.setChecked(!enabled);
		}
	}
}
