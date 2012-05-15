/*******************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - added support for IToggleBreakpointsTargetFactory
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpoints;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.actions.RetargetAction;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManager;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetManagerListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * Retargettable breakpoint action.
 * 
 * @since 3.0
 */
public abstract class RetargetBreakpointAction extends RetargetAction implements IToggleBreakpointsTargetManagerListener {
	
    private IAction fAction;
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.RetargetAction#getAdapterClass()
	 */
	protected Class getAdapterClass() {
		return IToggleBreakpointsTarget.class;
	}
	
    protected Object getAdapter(IAdaptable adaptable) {
        IToggleBreakpointsTargetManager manager = DebugUITools.getToggleBreakpointsTargetManager();
		IWorkbenchPart activePart = getActivePart();
        if (activePart != null) {
			return manager.getToggleBreakpointsTarget(activePart, getTargetSelection());
        }
        return null;
    }

    public void init(IWorkbenchWindow window) {
        super.init(window);
        DebugUITools.getToggleBreakpointsTargetManager().addChangedListener(this);
    }
    
    public void init(IAction action) {
        super.init(action);
        DebugUITools.getToggleBreakpointsTargetManager().addChangedListener(this);
    }
    
    public void dispose() {
        DebugUITools.getToggleBreakpointsTargetManager().removeChangedListener(this);
        super.dispose();
    }
    
    public void selectionChanged(IAction action, ISelection selection) {
        fAction = action;
        super.selectionChanged(action, selection);
    }
    
    public void preferredTargetsChanged() {
        if (fAction != null) {
            IWorkbenchPart activePart = getActivePart();
            if (activePart != null) {
                ISelectionProvider provider = activePart.getSite().getSelectionProvider();
                if (provider != null) {
                    ISelection selection = provider.getSelection();
                        // Force the toggle target to be refreshed.
                        super.clearPart(activePart);
                        super.partActivated(activePart);
                        super.selectionChanged(fAction, selection);
                }
            }
        }
    }
}
