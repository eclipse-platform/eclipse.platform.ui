/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;

/**
 * CutBreakpointsAction
 */
public class CutBreakpointsAction extends CopyBreakpointsAction {

    /**
     * Constructs a cut operation for breakpoints. Performs a move
     * on paste.
     * 
     * @param view
     * @param clipboard
     * @param pasteAction
     */
    public CutBreakpointsAction(BreakpointsView view, Clipboard clipboard, PasteBreakpointsAction pasteAction) {
        super(view, clipboard, pasteAction);
        setText(BreakpointGroupMessages.getString("CutBreakpointsAction.0")); //$NON-NLS-1$
        setToolTipText(BreakpointGroupMessages.getString("CutBreakpointsAction.1")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        super.run();
        IStructuredSelection selection = getStructuredSelection();
        // only cut from selected containers, not all
        Object[] objects = selection.toArray();
        List removeFromManager = new ArrayList();
        Map removeFromContainers = new HashMap();
        for (int i = 0; i < objects.length; i++) {
            IBreakpoint breakpoint = (IBreakpoint) objects[i];
            BreakpointContainer[] containers = breakpointsView.getMovedFromContainers(breakpoint);
            BreakpointContainer[] roots = breakpointsView.getRoots(breakpoint);
            if (roots != null && containers != null && roots.length == 1 && containers.length == 1) {
                removeFromManager.add(breakpoint);
            } else {
                removeFromContainers.put(breakpoint, containers);
            }
        }
        Iterator iterator = removeFromManager.iterator();
        while (iterator.hasNext()) {
            IBreakpoint breakpoint = (IBreakpoint) iterator.next();
            // TODO: dispose of orphaned breakpoints
            try {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, false);
            } catch (CoreException e) {
                DebugUIPlugin.log(e);
            }
        }
        iterator = removeFromContainers.keySet().iterator();
        while (iterator.hasNext()) {
            IBreakpoint breakpoint = (IBreakpoint) iterator.next();
            BreakpointContainer[] containers = (BreakpointContainer[]) removeFromContainers.get(breakpoint);
            breakpointsView.performRemove(containers, new StructuredSelection(breakpoint));
        }
    }
    
}
