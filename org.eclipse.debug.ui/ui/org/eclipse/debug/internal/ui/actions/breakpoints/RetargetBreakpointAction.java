/*******************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.actions.IToggleBreakpointsTargetManagerListener;
import org.eclipse.debug.internal.ui.actions.RetargetAction;
import org.eclipse.debug.internal.ui.actions.ToggleBreakpointsTargetManager;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
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
        ToggleBreakpointsTargetManager manager = ToggleBreakpointsTargetManager.getDefault();
        IPart activePart = getActivePart();
        if (activePart != null) {
            return manager.getToggleBreakpointsTarget(getActivePart(), getTargetSelection());
        }
        return null;
    }

    public void init(IWorkbenchWindow window) {
        super.init(window);
        ToggleBreakpointsTargetManager.getDefault().addChangedListener(this);
    }
    
    public void init(IAction action) {
        super.init(action);
        ToggleBreakpointsTargetManager.getDefault().addChangedListener(this);
    }
    
    public void dispose() {
        ToggleBreakpointsTargetManager.getDefault().removeChangedListener(this);
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
